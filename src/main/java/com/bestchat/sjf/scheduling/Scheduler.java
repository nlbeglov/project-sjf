package com.bestchat.sjf.scheduling;

import com.bestchat.sjf.model.Process;

import java.util.List;
import java.util.Optional;

/**
 * Common contract for CPU schedulers used by the simulation engine.
 */
public interface Scheduler {
    void setProcesses(List<Process> processes);

    void reset();

    Optional<Process> step();

    boolean isFinished();

    int getCurrentTime();

    List<Process> snapshot();

    List<String> consumeEvents();

    List<Process> readyQueueSnapshot();
}
