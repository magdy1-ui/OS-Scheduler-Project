package gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.Process;
import scheduler.PriorityScheduler;
import scheduler.RoundRobinScheduler;

import java.util.*;

public class MainApp extends Application {

    private ObservableList<Process> processList = FXCollections.observableArrayList();
    private TableView<Process> inputTable = new TableView<>();
    private VBox resultsArea = new VBox(25);

    private TextField idField = new TextField();
    private TextField arrivalField = new TextField();
    private TextField burstField = new TextField();
    private TextField priorityField = new TextField();
    private TextField quantumField = new TextField("2");

    @Override
    public void start(Stage stage) {
        stage.setTitle("CPU Scheduling Simulator - Round Robin vs Preemptive Priority");

        GridPane inputGrid = new GridPane();
        inputGrid.setPadding(new Insets(20));
        inputGrid.setHgap(15);
        inputGrid.setVgap(12);

        Button addBtn = new Button("Add Process");
        Button runBtn = new Button("Run Simulation");
        Button resetBtn = new Button("Reset");

        addBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        runBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        resetBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        inputGrid.addRow(0, new Label("Process ID:"), idField, new Label("Arrival Time:"), arrivalField);
        inputGrid.addRow(1, new Label("Burst Time:"), burstField, new Label("Priority:"), priorityField);
        inputGrid.addRow(2, new Label("Quantum (Round Robin):"), quantumField, addBtn, runBtn);
        inputGrid.add(resetBtn, 4, 2);

        setupInputTable();

        addBtn.setOnAction(e -> {
            try {
                String id = idField.getText().trim().toUpperCase();
                int arrival = Integer.parseInt(arrivalField.getText().trim());
                int burst = Integer.parseInt(burstField.getText().trim());
                int priority = Integer.parseInt(priorityField.getText().trim());
                int quantum = Integer.parseInt(quantumField.getText().trim());

                validateInput(id, arrival, burst, priority, quantum);
                processList.add(new Process(id, arrival, burst, priority));
                clearInputFields();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        runBtn.setOnAction(e -> runSimulation());

        resetBtn.setOnAction(e -> {
            processList.clear();
            resultsArea.getChildren().clear();
            clearInputFields();
            quantumField.clear();
        });

        ScrollPane scrollPane = new ScrollPane(resultsArea);
        scrollPane.setFitToWidth(true);

        VBox layout = new VBox(10, new Label("Input Section"), inputGrid, new Label("Process Table"), inputTable, scrollPane);
        layout.setPadding(new Insets(20));
        stage.setScene(new Scene(layout, 1200, 850));
        stage.show();
    }

    private void runSimulation() {
        if (processList.isEmpty()) { showError("Add processes first."); return; }
        
        int quantum = Integer.parseInt(quantumField.getText().trim());
        List<Process> rrList = cloneList(processList);
        List<Process> prioList = cloneList(processList);

        var rrTimeline = RoundRobinScheduler.simulate(rrList, quantum);
        var prioTimeline = PriorityScheduler.simulate(prioList);

        resultsArea.getChildren().clear();
        resultsArea.getChildren().addAll(
                createResultSection("Round Robin", rrList, rrTimeline),
                createResultSection("Preemptive Priority", prioList, prioTimeline)
        );
    }

    private VBox createResultSection(String title, List<Process> results, List<model.TimeSlot> timeline) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-border-color: #34495e; -fx-border-width: 2;");

        TableView<Process> table = new TableView<>();
        setupResultTable(table, results);

        double avgWT = results.stream().mapToDouble(Process::getWaitingTime).average().orElse(0);
        double avgTAT = results.stream().mapToDouble(Process::getTurnaroundTime).average().orElse(0);
        double avgRT = results.stream().mapToDouble(Process::getResponseTime).average().orElse(0);

        Label avgLabel = new Label(String.format(
                "Average WT: %.2f  |  Average TAT: %.2f  |  Average RT: %.2f",
                avgWT, avgTAT, avgRT));

        ScrollPane ganttPane = new ScrollPane(drawGantt(timeline));
        ganttPane.setPrefHeight(160);

        box.getChildren().addAll(new Label("Algorithm: " + title), table, avgLabel, new Label("Gantt Chart:"), ganttPane);
        return box;
    }

    private Pane drawGantt(List<model.TimeSlot> timeline) {
        Pane pane = new Pane();
        double unit = 45;
        double leftMargin = 40;
        double top = 30;
        double height = 50;

        Map<String, Color> colorMap = new HashMap<>();

        for (model.TimeSlot slot : timeline) {
            double startX = leftMargin + slot.startTime * unit;
            double width = slot.duration * unit;

            colorMap.putIfAbsent(slot.processId, Color.color(Math.random() * 0.4 + 0.5, Math.random() * 0.4 + 0.5, Math.random() * 0.4 + 0.5));

            Rectangle rect = new Rectangle(startX, top, width, height);
            rect.setFill(colorMap.get(slot.processId));
            rect.setStroke(Color.BLACK);

            Label name = new Label(slot.processId);
            name.setLayoutX(startX + width / 2 - 12);
            name.setLayoutY(top + 15);

            Line border = new Line(startX, top, startX, top + height);
            Label time = new Label(String.valueOf(slot.startTime));
            time.setLayoutX(startX - 5);
            time.setLayoutY(top + height + 5);

            pane.getChildren().addAll(rect, name, border, time);
        }
        pane.setPrefWidth(1200);
        pane.setPrefHeight(120);
        return pane;
    }

    @SuppressWarnings("unchecked")
    private void setupResultTable(TableView<Process> table, List<Process> results) {
        TableColumn<Process, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Process, Integer> compCol = new TableColumn<>("Completion");
        compCol.setCellValueFactory(new PropertyValueFactory<>("completionTime"));
        TableColumn<Process, Integer> waitCol = new TableColumn<>("Waiting");
        waitCol.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));
        TableColumn<Process, Integer> tatCol = new TableColumn<>("Turnaround");
        tatCol.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));
        TableColumn<Process, Integer> respCol = new TableColumn<>("Response");
        respCol.setCellValueFactory(new PropertyValueFactory<>("responseTime"));

        table.getColumns().setAll(idCol, compCol, waitCol, tatCol, respCol);
        table.setItems(FXCollections.observableArrayList(results));
    }

    private List<Process> cloneList(List<Process> list) {
        List<Process> clone = new ArrayList<>();
        for (Process p : list) clone.add(new Process(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getPriority()));
        return clone;
    }

    private void validateInput(String id, int arrival, int burst, int priority, int quantum) {
        if (id.isEmpty()) throw new RuntimeException("Empty ID.");
        if (arrival < 0 || burst <= 0 || priority < 0 || quantum <= 0) throw new RuntimeException("Invalid value.");
    }

    private void clearInputFields() { idField.clear(); arrivalField.clear(); burstField.clear(); priorityField.clear(); }
    private void showError(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).show(); }

    @SuppressWarnings("unchecked")
    private void setupInputTable() {
        TableColumn<Process, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Process, Integer> arrivalCol = new TableColumn<>("Arrival");
        arrivalCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        TableColumn<Process, Integer> burstCol = new TableColumn<>("Burst");
        burstCol.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        TableColumn<Process, Integer> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        inputTable.getColumns().setAll(idCol, arrivalCol, burstCol, priorityCol);
        inputTable.setItems(processList);
    }

    public static void main(String[] args) { launch(args); }
}
