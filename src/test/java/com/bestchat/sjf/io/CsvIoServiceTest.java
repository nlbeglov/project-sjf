package com.bestchat.sjf.io;

import com.bestchat.sjf.model.Process;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CsvIoServiceTest {

    @Test
    void savesAndLoadsProcesses() throws Exception {
        CsvIoService service = new CsvIoService();
        List<Process> processes = List.of(
                new Process("X1", 0, 3, 2),
                new Process("X2", 1, 5, 3)
        );
        processes.get(0).setStartTime(0);
        processes.get(0).setFinishTime(3);

        Path tempFile = Files.createTempFile("processes", ".csv");
        service.saveResults(tempFile, processes);

        List<Process> loaded = service.load(tempFile);

        assertFalse(loaded.isEmpty());
        assertEquals(processes.get(0).getId(), loaded.get(0).getId());
        assertEquals(processes.get(1).getArrivalTime(), loaded.get(1).getArrivalTime());
    }
}
