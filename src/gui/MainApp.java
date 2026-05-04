package gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Process;

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

                if (arrivalField.getText().trim().isEmpty())
                    throw new RuntimeException("Arrival Time cannot be empty.");
                if (burstField.getText().trim().isEmpty())
                    throw new RuntimeException("Burst Time cannot be empty.");
                if (priorityField.getText().trim().isEmpty())
                    throw new RuntimeException("Priority cannot be empty.");
                if (quantumField.getText().trim().isEmpty())
                    throw new RuntimeException("Quantum cannot be empty.");

                int arrival = Integer.parseInt(arrivalField.getText().trim());
                int burst = Integer.parseInt(burstField.getText().trim());
                int priority = Integer.parseInt(priorityField.getText().trim());
                int quantum = Integer.parseInt(quantumField.getText().trim());

                validateInput(id, arrival, burst, priority, quantum);

                processList.add(new Process(id, arrival, burst, priority));
                clearInputFields();

            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        VBox layout = new VBox(10, new Label("Input Section"), inputGrid, new Label("Process Table"), inputTable, resultsArea);
        layout.setPadding(new Insets(20));
        stage.setScene(new Scene(layout, 1200, 850));
        stage.show();
    }

    private void validateInput(String id, int arrival, int burst, int priority, int quantum) {
        if (id.isEmpty())
            throw new RuntimeException("Process ID cannot be empty.");

        for (Process p : processList)
            if (p.getId().equalsIgnoreCase(id))
                throw new RuntimeException("Duplicate Process ID not allowed.");

        if (arrival < 0)
            throw new RuntimeException("Arrival time cannot be negative.");
        if (burst <= 0)
            throw new RuntimeException("Burst time must be greater than 0.");
        if (priority < 0)
            throw new RuntimeException("Priority must be >= 0.");
        if (quantum <= 0)
            throw new RuntimeException("Quantum must be greater than 0.");
    }

    private void clearInputFields() {
        idField.clear();
        arrivalField.clear();
        burstField.clear();
        priorityField.clear();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }

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