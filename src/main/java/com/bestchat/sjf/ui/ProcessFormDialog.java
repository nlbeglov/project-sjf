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
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(addButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("ID:"), 0, 0);
        idField.setPromptText("Например, P1");
        grid.add(idField, 1, 0);
        grid.add(new Label("Время появления:"), 0, 1);
        arrivalField.setPromptText("0, 1, 2...");
        grid.add(arrivalField, 1, 1);
        grid.add(new Label("Длительность выполнения:"), 0, 2);
        burstField.setPromptText("Положительное число");
        grid.add(burstField, 1, 2);
        grid.add(new Label("Приоритет (1 = высокий):"), 0, 3);
        priorityField.setPromptText("1, 2, 3...");
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
            showError("Введите корректные числовые значения (появление >= 0, длительность > 0, приоритет > 0)");
            return Optional.empty();
        }
    }

    private void showError(String message) {
        ButtonType ok = new ButtonType("Понятно", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ok);
        alert.getButtonTypes().setAll(ok);
        alert.setHeaderText("Некорректный ввод");
        alert.showAndWait();
    }
}
