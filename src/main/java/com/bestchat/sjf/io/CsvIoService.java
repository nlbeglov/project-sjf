package com.bestchat.sjf.io;

import com.bestchat.sjf.model.Process;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for importing and exporting process definitions and results.
 */
public class CsvIoService {
    public List<Process> load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path); CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreEmptyLines(true)
                .parse(reader)) {
            List<Process> processes = new ArrayList<>();
            for (CSVRecord record : parser) {
                String id = record.get(0).trim();
                int arrival = Integer.parseInt(record.get(1).trim());
                int burst = Integer.parseInt(record.get(2).trim());
                int priority = Integer.parseInt(record.get(3).trim());
                processes.add(new Process(id, arrival, burst, priority));
            }
            return processes;
        }
    }

    public void saveResults(Path path, List<Process> processes) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path); CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("id", "arrivalTime", "burstTime", "priority", "startTime", "finishTime", "waitingTime", "turnaroundTime"))) {
            for (Process process : processes) {
                printer.printRecord(
                        process.getId(),
                        process.getArrivalTime(),
                        process.getBurstTime(),
                        process.getPriority(),
                        valueOrEmpty(process.getStartTime()),
                        valueOrEmpty(process.getFinishTime()),
                        process.getWaitingTime(),
                        process.getTurnaroundTime()
                );
            }
        }
    }

    private Object valueOrEmpty(Integer value) {
        return value == null ? "" : value;
    }
}
