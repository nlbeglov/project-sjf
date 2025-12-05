package com.bestchat.sjf.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Simple Gantt chart painter that visualizes CPU bursts.
 */
public class GanttChart extends Region {
    private final Canvas canvas = new Canvas(800, 200);
    private final List<Segment> segments = new ArrayList<>();

    public GanttChart() {
        getChildren().add(canvas);
        widthProperty().addListener((obs, oldV, newV) -> redraw());
        heightProperty().addListener((obs, oldV, newV) -> redraw());
    }

    public void reset() {
        segments.clear();
        redraw();
    }

    public void addSlice(String processId, int startTime, int endTime, Color color) {
        if (!segments.isEmpty()) {
            Segment last = segments.get(segments.size() - 1);
            if (Objects.equals(last.processId, processId) && last.end == startTime) {
                last.end = endTime;
                redraw();
                return;
            }
        }
        segments.add(new Segment(processId, startTime, endTime, color));
        redraw();
    }

    private void redraw() {
        double width = getWidth() <= 0 ? canvas.getWidth() : getWidth();
        double height = getHeight() <= 0 ? canvas.getHeight() : getHeight();
        canvas.setWidth(width);
        canvas.setHeight(height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        if (segments.isEmpty()) {
            return;
        }
        double maxTime = segments.get(segments.size() - 1).end;
        double scale = width / Math.max(1, maxTime);
        double barHeight = height * 0.6;
        double y = (height - barHeight) / 2;
        for (Segment segment : segments) {
            double x = segment.start * scale;
            double w = (segment.end - segment.start) * scale;
            gc.setFill(segment.color);
            gc.fillRect(x, y, w, barHeight);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x, y, w, barHeight);
            gc.setFill(Color.BLACK);
            gc.fillText(segment.processId, x + 5, y + barHeight / 2);
            gc.fillText(String.valueOf(segment.start), x, height - 5);
        }
        gc.fillText(String.valueOf((int) maxTime), maxTime * scale, height - 5);
    }

    private static class Segment {
        private final String processId;
        private final int start;
        private int end;
        private final Color color;

        Segment(String processId, int start, int end, Color color) {
            this.processId = processId;
            this.start = start;
            this.end = end;
            this.color = color;
        }
    }
}
