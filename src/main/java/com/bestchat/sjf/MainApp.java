package com.bestchat.sjf;

import com.bestchat.sjf.io.CsvIoService;
import com.bestchat.sjf.model.Process;
import com.bestchat.sjf.model.ProcessState;
import com.bestchat.sjf.scheduling.SJFWithPriorityScheduler;
import com.bestchat.sjf.scheduling.Scheduler;
import com.bestchat.sjf.scheduling.SchedulingMode;
import com.bestchat.sjf.simulation.SimulationEngine;
import com.bestchat.sjf.simulation.SimulationListener;
import com.bestchat.sjf.ui.GanttChart;
import com.bestchat.sjf.ui.ProcessFormDialog;
import com.bestchat.sjf.ui.ProcessViewModel;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Main JavaFX application that wires together the scheduler, simulation engine, and UI.
 */
public class MainApp extends Application implements SimulationListener {

    private final ObservableList<ProcessViewModel> tableData = FXCollections.observableArrayList();
    private final Map<String, ProcessViewModel> viewModelIndex = new HashMap<>();
    private final ListView<String> eventLog = new ListView<>();
    private final ListView<String> readyQueueView = new ListView<>();
    private final Label timeLabel = new Label("t=0");
    private final GanttChart ganttChart = new GanttChart();

    private final CsvIoService csvIoService = new CsvIoService();
    private final List<Process> processDefinitions = new ArrayList<>();

    private Scheduler scheduler;
    private SimulationEngine engine;

    private ComboBox<SchedulingMode> modeCombo;
    private CheckBox agingCheckBox;
    private Slider speedSlider;

    private int lastTimeMark;
    private String activeProcessId;

    @Override
    public void start(Stage stage) {
        modeCombo = new ComboBox<>(FXCollections.observableArrayList(SchedulingMode.values()));
        modeCombo.setValue(SchedulingMode.PREEMPTIVE);
        agingCheckBox = new CheckBox("Старение приоритетов");
        agingCheckBox.setSelected(true);
        speedSlider = new Slider(0.25, 4, 1);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.75);
        speedSlider.setMinorTickCount(2);
        speedSlider.setShowTickLabels(true);
        speedSlider.valueProperty().addListener((obs, o, n) -> updateSpeed());

        buildEngine();
        seedInitialProcesses();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setTop(buildTopControls());
        root.setCenter(buildMainTabs());
        root.setRight(buildSidePanel());
        root.setBottom(buildBottomControls());

        stage.setTitle("SJF Priority Simulator");
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }

    private void buildEngine() {
        scheduler = new SJFWithPriorityScheduler(modeCombo.getValue(), agingCheckBox.isSelected(), 3);
        engine = new SimulationEngine(scheduler);
        engine.addListener(this);
    }

    private void seedInitialProcesses() {
        processDefinitions.add(new Process("P1", 0, 6, 2));
        processDefinitions.add(new Process("P2", 1, 4, 1));
        processDefinitions.add(new Process("P3", 2, 5, 3));
        refreshEngineData();
    }

    private Pane buildTopControls() {
        Button addButton = new Button("Добавить процесс");
        addButton.setOnAction(e -> openProcessDialog());

        Button importButton = new Button("Загрузить из CSV");
        importButton.setOnAction(e -> importFromCsv());

        Button exportButton = new Button("Сохранить результаты");
        exportButton.setOnAction(e -> exportResults());

        HBox box = new HBox(10, addButton, importButton, exportButton,
                new Label("Режим:"), modeCombo,
                agingCheckBox,
                new Label("Скорость"), speedSlider);
        box.setPadding(new Insets(10));

        modeCombo.setOnAction(e -> reconfigureScheduler());
        agingCheckBox.setOnAction(e -> reconfigureScheduler());

        return box;
    }

    private void reconfigureScheduler() {
        buildEngine();
        refreshEngineData();
    }

    private Pane buildMainTabs() {
        TabPane tabs = new TabPane();

        BorderPane ganttPane = new BorderPane(ganttChart);
        ganttPane.setPadding(new Insets(10));
        tabs.getTabs().add(new Tab("Диаграмма Ганта", ganttPane));

        TableView<ProcessViewModel> tableView = buildProcessTable();
        tabs.getTabs().add(new Tab("Процессы", tableView));

        tabs.getTabs().forEach(tab -> tab.setClosable(false));
        return tabs;
    }

    private TableView<ProcessViewModel> buildProcessTable() {
        TableView<ProcessViewModel> tableView = new TableView<>(tableData);
        tableView.getColumns().addAll(
                column("ID", ProcessViewModel::idProperty),
                column("Arrival", ProcessViewModel::arrivalProperty),
                column("Burst", ProcessViewModel::burstProperty),
                column("Priority", ProcessViewModel::priorityProperty),
                column("State", ProcessViewModel::stateProperty),
                column("Remaining", ProcessViewModel::remainingProperty),
                column("Start", ProcessViewModel::startProperty),
                column("Finish", ProcessViewModel::finishProperty),
                column("Waiting", ProcessViewModel::waitingProperty),
                column("Turnaround", ProcessViewModel::turnaroundProperty)
        );
        return tableView;
    }

    private <T> TableColumn<ProcessViewModel, T> column(String title, javafx.util.Callback<ProcessViewModel, javafx.beans.value.ObservableValue<T>> mapper) {
        TableColumn<ProcessViewModel, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> mapper.call(cell.getValue()));
        column.setPrefWidth(100);
        return column;
    }

    private Pane buildSidePanel() {
        VBox right = new VBox(10);
        right.setPadding(new Insets(10));
        Label readyLabel = new Label("READY очередь");
        readyQueueView.setPrefHeight(200);
        Label logLabel = new Label("Журнал событий");
        eventLog.setPrefHeight(400);
        right.getChildren().addAll(readyLabel, readyQueueView, logLabel, eventLog);
        return right;
    }

    private Pane buildBottomControls() {
        Button startButton = new Button("Run");
        Button pauseButton = new Button("Pause");
        Button stepButton = new Button("Step");
        Button resetButton = new Button("Reset");

        startButton.setOnAction(e -> engine.startContinuous());
        pauseButton.setOnAction(e -> engine.pause());
        stepButton.setOnAction(e -> engine.step());
        resetButton.setOnAction(e -> {
            engine.reset();
            ganttChart.reset();
            lastTimeMark = 0;
            activeProcessId = null;
            eventLog.getItems().clear();
        });

        HBox controls = new HBox(10, startButton, pauseButton, stepButton, resetButton, new Label("Текущее время:"), timeLabel);
        controls.setPadding(new Insets(10));
        return controls;
    }

    private void openProcessDialog() {
        ProcessFormDialog dialog = new ProcessFormDialog();
        Optional<Process> result = dialog.showAndWait();
        result.ifPresent(process -> {
            if (processDefinitions.stream().anyMatch(p -> p.getId().equals(process.getId()))) {
                alert("Дублирование ID", "Процесс с таким ID уже существует");
                return;
            }
            processDefinitions.add(process);
            processDefinitions.sort(Comparator.comparingInt(Process::getArrivalTime));
            refreshEngineData();
        });
    }

    private void importFromCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите CSV файл процессов");
        File file = chooser.showOpenDialog(null);
        if (file == null) {
            return;
        }
        try {
            List<Process> loaded = csvIoService.load(file.toPath());
            if (loaded.isEmpty()) {
                alert("Импорт", "Файл не содержит процессов");
                return;
            }
            processDefinitions.clear();
            processDefinitions.addAll(loaded);
            refreshEngineData();
            alert("Импорт", "Загружено процессов: " + loaded.size());
        } catch (IOException | RuntimeException ex) {
            alert("Ошибка импорта", ex.getMessage());
        }
    }

    private void exportResults() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить CSV результатов");
        chooser.setInitialFileName("results.csv");
        File file = chooser.showSaveDialog(null);
        if (file == null) {
            return;
        }
        try {
            csvIoService.saveResults(Path.of(file.toURI()), scheduler.snapshot());
            alert("Экспорт", "Результаты сохранены в " + file.getName());
        } catch (IOException ex) {
            alert("Ошибка экспорта", ex.getMessage());
        }
    }

    private void refreshEngineData() {
        viewModelIndex.clear();
        tableData.clear();
        processDefinitions.forEach(p -> {
            ProcessViewModel vm = new ProcessViewModel(p);
            viewModelIndex.put(p.getId(), vm);
            tableData.add(vm);
        });
        engine.loadProcesses(processDefinitions);
        ganttChart.reset();
        lastTimeMark = 0;
        activeProcessId = null;
        readyQueueView.getItems().clear();
    }

    private void updateSpeed() {
        engine.setSpeedMultiplier(speedSlider.getValue());
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    @Override
    public void onTimeAdvanced(int time) {
        if (activeProcessId != null) {
            ganttChart.addSlice(activeProcessId, lastTimeMark, time, colorForProcess(activeProcessId));
        }
        lastTimeMark = time;
        timeLabel.setText("t=" + time);
    }

    @Override
    public void onProcessStateChanged(Process process) {
        ProcessViewModel vm = viewModelIndex.computeIfAbsent(process.getId(), id -> {
            ProcessViewModel created = new ProcessViewModel(process);
            tableData.add(created);
            return created;
        });
        vm.updateFromProcess(process);
        if (process.getState() == ProcessState.RUNNING) {
            activeProcessId = process.getId();
        } else if (process.getId().equals(activeProcessId) && process.getState() != ProcessState.RUNNING) {
            activeProcessId = null;
        }
    }

    @Override
    public void onSchedulingEvent(String message) {
        eventLog.getItems().add(message);
        eventLog.scrollTo(eventLog.getItems().size() - 1);
    }

    @Override
    public void onReadyQueueUpdated(List<Process> ready) {
        readyQueueView.getItems().setAll(ready.stream()
                .map(p -> p.getId() + " (pr=" + p.getPriority() + ", remain=" + p.getRemainingTime() + ")")
                .toList());
    }

    private Color colorForProcess(String id) {
        int hash = Math.abs(id.hashCode());
        double hue = (hash % 360);
        return Color.hsb(hue, 0.6, 0.9);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
