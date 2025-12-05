package com.bestchat.sjf.simulation;

import com.bestchat.sjf.model.Process;
import com.bestchat.sjf.scheduling.Scheduler;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Drives the simulation loop and bridges the scheduler with the UI through listeners.
 */
public class SimulationEngine {
    private final Scheduler scheduler;
    private final List<SimulationListener> listeners = new ArrayList<>();
    private Timeline timeline;
    private double speedMultiplier = 1.0;
    private List<Process> originalDefinition = new ArrayList<>();

    public SimulationEngine(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void addListener(SimulationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }

    public void loadProcesses(List<Process> processes) {
        this.originalDefinition = processes;
        scheduler.setProcesses(processes);
        notifyAllProcesses();
        notifyTime();
    }

    public void reset() {
        scheduler.setProcesses(originalDefinition);
        notifyAllProcesses();
        notifyTime();
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
        if (timeline != null) {
            startContinuous();
        }
    }

    public void startContinuous() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.millis(500 / speedMultiplier), e -> step()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void pause() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    public void step() {
        Optional<Process> running = scheduler.step();
        for (Process process : scheduler.snapshot()) {
            notifyProcess(process);
        }
        scheduler.consumeEvents().forEach(this::notifyEvent);
        notifyReadyQueue();
        notifyTime();
        running.ifPresent(this::notifyProcess);
        if (scheduler.isFinished()) {
            pause();
        }
    }

    private void notifyProcess(Process process) {
        listeners.forEach(l -> l.onProcessStateChanged(process));
    }

    private void notifyEvent(String event) {
        listeners.forEach(l -> l.onSchedulingEvent(event));
    }

    private void notifyTime() {
        listeners.forEach(l -> l.onTimeAdvanced(scheduler.getCurrentTime()));
    }

    private void notifyReadyQueue() {
        listeners.forEach(l -> l.onReadyQueueUpdated(scheduler.readyQueueSnapshot()));
    }

    private void notifyAllProcesses() {
        for (Process process : scheduler.snapshot()) {
            notifyProcess(process);
        }
    }
}
