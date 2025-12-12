package com.bestchat.sjf.scheduling;

import com.bestchat.sjf.model.Process;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SJFWithPrioritySchedulerTest {

    @Test
    void preemptiveOrdersByPriorityAndBurst() {
        SJFWithPriorityScheduler scheduler = new SJFWithPriorityScheduler(SchedulingMode.PREEMPTIVE, false, 3);
        scheduler.setProcesses(List.of(
                new Process("P1", 0, 5, 3),
                new Process("P2", 1, 2, 1),
                new Process("P3", 2, 1, 2)
        ));

        while (!scheduler.isFinished()) {
            scheduler.step();
            scheduler.consumeEvents();
        }

        List<Process> result = scheduler.snapshot();
        Process p1 = findProcess(result, "P1");
        Process p2 = findProcess(result, "P2");
        Process p3 = findProcess(result, "P3");

        assertEquals(3, p2.getFinishTime());
        assertEquals(4, p3.getFinishTime());
        assertEquals(8, p1.getFinishTime());
        assertEquals(1, p2.getStartTime());
        assertEquals(3, p3.getStartTime());
    }

    @Test
    void agingRaisesWaitingProcessPriority() {
        SJFWithPriorityScheduler scheduler = new SJFWithPriorityScheduler(SchedulingMode.PREEMPTIVE, true, 2);
        scheduler.setProcesses(List.of(
                new Process("X1", 0, 6, 1),
                new Process("X2", 0, 4, 4)
        ));

        boolean agingTriggered = false;
        while (!scheduler.isFinished()) {
            scheduler.step();
            Optional<String> agingEvent = scheduler.consumeEvents().stream()
                    .filter(event -> event.contains("Старение приоритета"))
                    .findFirst();
            if (agingEvent.isPresent()) {
                agingTriggered = true;
                break;
            }
        }

        Process aged = findProcess(scheduler.snapshot(), "X2");
        assertTrue(agingTriggered, "Событие старения должно быть зафиксировано");
        assertTrue(aged.getPriority() < 4, "Приоритет должен увеличиться (уменьшиться числовое значение)");
    }

    private Process findProcess(List<Process> processes, String id) {
        return processes.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет процесса " + id));
    }
}
