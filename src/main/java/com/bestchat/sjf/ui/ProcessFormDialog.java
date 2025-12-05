package com.bestchat.sjf.ui;

import com.bestchat.sjf.model.Process;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * Dialog for creating a new process with validation feedback.
 */
public class ProcessFormDialog extends Dialog<Process> {
    private final TextField idField = new TextField();
    private final TextField arrivalField = new TextField();
    private final TextField burstField = new TextField();
    private final TextField priorityField = new TextField();

    public ProcessFormDialog() {
        setTitle("Добавить процесс");
        setHeaderText("Введите параметры процесса");

        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Arrival time:"), 0, 1);
        grid.add(arrivalField, 1, 1);
        grid.add(new Label("Burst time:"), 0, 2);
        grid.add(burstField, 1, 2);
        grid.add(new Label("Priority (1 = high):"), 0, 3);
        grid.add(priorityField, 1, 3);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return validateAndCreate().orElse(null);
            }
            return null;
        });
    }

    private Optional<Process> validateAndCreate() {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            showError("ID не может быть пустым");
            return Optional.empty();
        }
        try {
            int arrival = Integer.parseInt(arrivalField.getText().trim());
            int burst = Integer.parseInt(burstField.getText().trim());
            int priority = Integer.parseInt(priorityField.getText().trim());
            if (arrival < 0 || burst <= 0 || priority <= 0) {
                throw new NumberFormatException();
            }
            return Optional.of(new Process(id, arrival, burst, priority));
        } catch (NumberFormatException ex) {
            showError("Введите корректные числовые значения (arrival >= 0, burst > 0, priority > 0)");
            return Optional.empty();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Некорректный ввод");
        alert.showAndWait();
    }
}
