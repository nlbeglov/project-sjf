package com.bestchat.sjf.simulation;

import com.bestchat.sjf.model.Process;

/**
 * Listener callbacks for UI updates during simulation.
 */
public interface SimulationListener {
    void onTimeAdvanced(int time);

    void onProcessStateChanged(Process process);

    void onSchedulingEvent(String message);

    void onReadyQueueUpdated(java.util.List<Process> ready);
}
