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

        idField.setPromptText("e.g. P1");
        arrivalField.setPromptText("Arrival Time");
        burstField.setPromptText("Burst Time");
        priorityField.setPromptText("Priority Value");

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

        Label priorityRule = new Label("Priority Rule: Lower number = Higher priority");
        priorityRule.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");
        
        Label tieBreakerRule = new Label("Tie-Breaking Rule: FIFO for equal priority");
        tieBreakerRule.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

        setupInputTable();

        ScrollPane scrollPane = new ScrollPane(resultsArea);
        scrollPane.setFitToWidth(true);

        VBox layout = new VBox(10,
                new Label("Input Section"),
                inputGrid,
                priorityRule,
                tieBreakerRule,
                new Label("Process Table"),
                inputTable,
                scrollPane);

        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 1200, 850));
        stage.show();
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

    public static void main(String[] args) {
        launch(args);
    }
}
