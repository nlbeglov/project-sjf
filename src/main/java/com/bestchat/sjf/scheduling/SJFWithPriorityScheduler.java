package com.bestchat.sjf.scheduling;

import com.bestchat.sjf.model.Process;
import com.bestchat.sjf.model.ProcessState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Scheduler implementing SJF with priorities in preemptive and non-preemptive modes.
 */
public class SJFWithPriorityScheduler implements Scheduler {

    private final SchedulingMode mode;
    private final boolean agingEnabled;
    private final int agingThreshold;

    private List<Process> processes = new ArrayList<>();
    private PriorityQueue<Process> readyQueue;
    private final List<String> events = new ArrayList<>();
    private Process current;
    private int arrivalIndex;
    private int time;
    private int completed;

    public SJFWithPriorityScheduler(SchedulingMode mode, boolean agingEnabled, int agingThreshold) {
        this.mode = mode;
        this.agingEnabled = agingEnabled;
        this.agingThreshold = agingThreshold;
        resetQueue();
    }

    @Override
    public void setProcesses(List<Process> processes) {
        this.processes = processes.stream().map(Process::copyForRestart).sorted(Comparator.comparingInt(Process::getArrivalTime)).toList();
        reset();
    }

    @Override
    public void reset() {
        resetQueue();
        this.time = 0;
        this.completed = 0;
        this.arrivalIndex = 0;
        this.current = null;
        for (Process process : processes) {
            process.setRemainingTime(process.getBurstTime());
            process.setFinishTime(null);
            process.setStartTime(null);
            process.setPriority(process.getBasePriority());
            process.setState(ProcessState.NEW);
            process.resetWaitingTime();
        }
        events.clear();
    }

    @Override
    public Optional<Process> step() {
        if (isFinished()) {
            return Optional.empty();
        }

        addArrivals();
        applyAging();

        if (mode == SchedulingMode.PREEMPTIVE) {
            preemptiveDecision();
        } else {
            nonPreemptiveDecision();
        }

        if (current == null) {
            time++;
            return Optional.empty();
        }

        runCurrent();
        time++;

        if (current.getRemainingTime() == 0) {
            current.setFinishTime(time);
            current.setState(ProcessState.FINISHED);
            events.add("t=" + time + ": Процесс " + current.getId() + " завершен");
            completed++;
            current = null;
        }

        return Optional.ofNullable(current);
    }

    private void runCurrent() {
        if (current.getStartTime() == null) {
            current.setStartTime(time);
            events.add("t=" + time + ": Процесс " + current.getId() + " запущен (arrival=" + current.getArrivalTime()
                    + ", burst=" + current.getBurstTime() + ", priority=" + current.getPriority() + ")");
        }
        current.setState(ProcessState.RUNNING);
        current.setRemainingTime(current.getRemainingTime() - 1);
        readyQueue.forEach(Process::incrementWaitingTime);
    }

    private void preemptiveDecision() {
        Process candidate = readyQueue.peek();
        if (candidate != null && shouldPreempt(candidate)) {
            if (current != null) {
                current.setState(ProcessState.READY);
                readyQueue.add(current);
                events.add("t=" + time + ": Процесс " + candidate.getId() + " вытеснил " + current.getId());
            }
            current = readyQueue.poll();
        } else if (current == null && !readyQueue.isEmpty()) {
            current = readyQueue.poll();
        }
    }

    private void nonPreemptiveDecision() {
        if (current == null && !readyQueue.isEmpty()) {
            current = readyQueue.poll();
        }
    }

    private boolean shouldPreempt(Process candidate) {
        if (current == null) {
            return true;
        }
        if (candidate.getPriority() < current.getPriority()) {
            return true;
        }
        if (candidate.getPriority() == current.getPriority()) {
            return candidate.getRemainingTime() < current.getRemainingTime();
        }
        return false;
    }

    private void applyAging() {
        if (!agingEnabled || agingThreshold <= 0) {
            return;
        }
        for (Process ready : readyQueue) {
            if (ready.getWaitingTime() >= agingThreshold && ready.getPriority() > 1) {
                ready.setPriority(ready.getPriority() - 1);
                ready.resetWaitingTime();
                events.add("t=" + time + ": Старение приоритета повысило процесс " + ready.getId());
            }
        }
    }

    private void addArrivals() {
        while (arrivalIndex < processes.size() && processes.get(arrivalIndex).getArrivalTime() <= time) {
            Process process = processes.get(arrivalIndex);
            process.setState(ProcessState.READY);
            readyQueue.add(process);
            events.add("t=" + time + ": Процесс " + process.getId() + " прибыл");
            arrivalIndex++;
        }
    }

    private void resetQueue() {
        readyQueue = new PriorityQueue<>(Comparator
                .comparingInt(Process::getPriority)
                .thenComparingInt(Process::getRemainingTime)
                .thenComparingInt(Process::getArrivalTime));
    }

    @Override
    public boolean isFinished() {
        return completed == processes.size();
    }

    @Override
    public List<Process> snapshot() {
        return new ArrayList<>(processes);
    }

    @Override
    public int getCurrentTime() {
        return time;
    }

    @Override
    public List<String> consumeEvents() {
        List<String> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }

    @Override
    public List<Process> readyQueueSnapshot() {
        return readyQueue.stream().sorted(Comparator
                .comparingInt(Process::getPriority)
                .thenComparingInt(Process::getRemainingTime)
                .thenComparingInt(Process::getArrivalTime)).toList();
    }
}
