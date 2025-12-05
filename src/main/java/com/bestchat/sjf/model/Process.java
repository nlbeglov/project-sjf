package com.bestchat.sjf.model;

import java.util.Objects;

/**
 * Represents a process definition and its runtime attributes during scheduling simulation.
 * Immutable identity fields capture the original problem, while mutable fields reflect runtime state.
 */
public class Process {
    private final String id;
    private final int arrivalTime;
    private final int burstTime;
    private final int basePriority;

    private int priority;
    private int remainingTime;
    private Integer startTime;
    private Integer finishTime;
    private int waitingTime;
    private ProcessState state;

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id = Objects.requireNonNull(id, "id");
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.basePriority = priority;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.state = ProcessState.NEW;
    }

    /**
     * Creates a deep copy with runtime attributes reset to their initial values.
     */
    public Process copyForRestart() {
        Process copy = new Process(id, arrivalTime, burstTime, basePriority);
        copy.priority = basePriority;
        copy.remainingTime = burstTime;
        copy.startTime = null;
        copy.finishTime = null;
        copy.waitingTime = 0;
        copy.state = ProcessState.NEW;
        return copy;
    }

    public String getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getBasePriority() {
        return basePriority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Integer finishTime) {
        this.finishTime = finishTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void incrementWaitingTime() {
        this.waitingTime++;
    }

    public void resetWaitingTime() {
        this.waitingTime = 0;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public int getTurnaroundTime() {
        if (finishTime == null) {
            return 0;
        }
        return finishTime - arrivalTime;
    }
}
