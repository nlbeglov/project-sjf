package com.bestchat.sjf.ui;

import com.bestchat.sjf.model.Process;
import com.bestchat.sjf.model.ProcessState;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JavaFX-friendly wrapper around {@link Process} for live table updates.
 */
public class ProcessViewModel {
    private final StringProperty id = new SimpleStringProperty();
    private final IntegerProperty arrival = new SimpleIntegerProperty();
    private final IntegerProperty burst = new SimpleIntegerProperty();
    private final IntegerProperty priority = new SimpleIntegerProperty();
    private final IntegerProperty remaining = new SimpleIntegerProperty();
    private final IntegerProperty start = new SimpleIntegerProperty();
    private final IntegerProperty finish = new SimpleIntegerProperty();
    private final IntegerProperty waiting = new SimpleIntegerProperty();
    private final IntegerProperty turnaround = new SimpleIntegerProperty();
    private final StringProperty state = new SimpleStringProperty(ProcessState.NEW.name());

    public ProcessViewModel(Process process) {
        updateFromProcess(process);
    }

    public void updateFromProcess(Process process) {
        id.set(process.getId());
        arrival.set(process.getArrivalTime());
        burst.set(process.getBurstTime());
        priority.set(process.getPriority());
        remaining.set(process.getRemainingTime());
        start.set(process.getStartTime() == null ? -1 : process.getStartTime());
        finish.set(process.getFinishTime() == null ? -1 : process.getFinishTime());
        waiting.set(process.getWaitingTime());
        turnaround.set(process.getTurnaroundTime());
        state.set(process.getState().name());
    }

    public StringProperty idProperty() {
        return id;
    }

    public IntegerProperty arrivalProperty() {
        return arrival;
    }

    public IntegerProperty burstProperty() {
        return burst;
    }

    public IntegerProperty priorityProperty() {
        return priority;
    }

    public IntegerProperty remainingProperty() {
        return remaining;
    }

    public IntegerProperty startProperty() {
        return start;
    }

    public IntegerProperty finishProperty() {
        return finish;
    }

    public IntegerProperty waitingProperty() {
        return waiting;
    }

    public IntegerProperty turnaroundProperty() {
        return turnaround;
    }

    public StringProperty stateProperty() {
        return state;
    }
}
