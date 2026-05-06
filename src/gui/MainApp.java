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

        inputGrid.addRow(0, new Label("Process ID:"), idField,
                new Label("Arrival Time:"), arrivalField);
        inputGrid.addRow(1, new Label("Burst Time:"), burstField,
                new Label("Priority:"), priorityField);
        inputGrid.addRow(2, new Label("Quantum (Round Robin):"), quantumField,
                addBtn, runBtn);
        inputGrid.add(resetBtn, 4, 2);

        Label priorityRule = new Label(
                "Priority Rule: Lower number = Higher priority (Preemptive Scheduling)"
        );
        priorityRule.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

        Label tieBreakerRule = new Label(
                "Tie-Breaking Rule: Equal-priority processes are served in arrival order (earliest first)."
        );
        tieBreakerRule.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

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

                int arrival, burst, priority, quantum;
                try { arrival = Integer.parseInt(arrivalField.getText().trim()); }
                catch (NumberFormatException ex) { throw new RuntimeException("Arrival Time must be a valid integer."); }
                try { burst = Integer.parseInt(burstField.getText().trim()); }
                catch (NumberFormatException ex) { throw new RuntimeException("Burst Time must be a valid integer."); }
                try { priority = Integer.parseInt(priorityField.getText().trim()); }
                catch (NumberFormatException ex) { throw new RuntimeException("Priority must be a valid integer."); }
                try { quantum = Integer.parseInt(quantumField.getText().trim()); }
                catch (NumberFormatException ex) { throw new RuntimeException("Quantum must be a valid integer."); }

                validateInput(id, arrival, burst, priority, quantum);

                processList.add(new Process(id, arrival, burst, priority));
                clearInputFields();

            } catch (Exception ex) {
                showError(ex.getMessage());
            }
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

    private void runSimulation() {

        if (processList.isEmpty()) {
            showError("Add processes first.");
            return;
        }

        if (quantumField.getText().trim().isEmpty()) {
            showError("Quantum cannot be empty.");
            return;
        }

        int quantum;
        try {
            quantum = Integer.parseInt(quantumField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Quantum must be a valid integer.");
            return;
        }

        if (quantum <= 0) {
            showError("Quantum must be greater than 0.");
            return;
        }

        List<Process> rrList = cloneList(processList);
        List<Process> prioList = cloneList(processList);

        var rrTimeline = RoundRobinScheduler.simulate(rrList, quantum);
        var prioTimeline = PriorityScheduler.simulate(prioList);

        resultsArea.getChildren().clear();

        resultsArea.getChildren().addAll(
                createResultSection("Round Robin", rrList, rrTimeline),
                createResultSection("Preemptive Priority", prioList, prioTimeline),
                createComparisonSection(rrList, prioList)
        );
    }

    private VBox createComparisonSection(List<Process> rr, List<Process> prio) {

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-border-color: #34495e; -fx-border-width: 2;");

        double rrWT   = rr.stream().mapToDouble(Process::getWaitingTime).average().orElse(0);
        double prioWT = prio.stream().mapToDouble(Process::getWaitingTime).average().orElse(0);
        double rrRT   = rr.stream().mapToDouble(Process::getResponseTime).average().orElse(0);
        double prioRT = prio.stream().mapToDouble(Process::getResponseTime).average().orElse(0);

        double rrStd   = calculateStdDev(rr);
        double prioStd = calculateStdDev(prio);

        boolean fairnessIssue  = prioStd > rrStd;
        boolean starvationRisk = detectStarvationRisk(prio);

        String betterWT;
        if (Math.abs(rrWT - prioWT) < 0.01)
            betterWT = String.format("Both algorithms gave equal average Waiting Time (%.2f).", rrWT);
        else if (rrWT < prioWT)
            betterWT = String.format("Round Robin gave better average Waiting Time (%.2f vs %.2f).", rrWT, prioWT);
        else
            betterWT = String.format("Priority Scheduling gave better average Waiting Time (%.2f vs %.2f).", prioWT, rrWT);

        String betterRT;
        if (Math.abs(rrRT - prioRT) < 0.01)
            betterRT = String.format("Both algorithms gave equal average Response Time (%.2f).", rrRT);
        else if (rrRT < prioRT)
            betterRT = String.format("Round Robin gave better average Response Time (%.2f vs %.2f).", rrRT, prioRT);
        else
            betterRT = String.format("Priority Scheduling gave better average Response Time (%.2f vs %.2f).", prioRT, rrRT);

        Process highestPrio = prio.stream().min(Comparator.comparingInt(Process::getPriority)).orElse(null);
        Process lowestPrio  = prio.stream().max(Comparator.comparingInt(Process::getPriority)).orElse(null);
        String priorityAdvantage;
        if (highestPrio != null && lowestPrio != null && !highestPrio.getId().equals(lowestPrio.getId())) {
            int wtDiff = lowestPrio.getWaitingTime() - highestPrio.getWaitingTime();
            double avgWT2 = prio.stream().mapToDouble(Process::getWaitingTime).average().orElse(0);
            if (wtDiff > avgWT2)
                priorityAdvantage = String.format(
                        "High-priority process %s gained a significant advantage (WT: %d vs %d for %s).",
                        highestPrio.getId(), highestPrio.getWaitingTime(),
                        lowestPrio.getWaitingTime(), lowestPrio.getId());
            else
                priorityAdvantage = "High-priority processes had some advantage but service was relatively balanced.";
        } else {
            priorityAdvantage = "All processes share the same priority — no priority advantage observed.";
        }

        String balanceObservation = fairnessIssue
                ? "Round Robin provided more balanced service as it distributed CPU time fairly across all processes."
                : "Both algorithms provided similar levels of fairness across processes in this workload.";

        String starvationObservation = starvationRisk
                ? "Starvation was detected: a low-priority process waited significantly longer than others in Priority Scheduling."
                : "No starvation was detected in this run under Priority Scheduling.";

        String performanceRecommendation;
        if (Math.abs(rrWT - prioWT) < 0.01)
            performanceRecommendation = "Both algorithms performed equally on this workload.";
        else if (rrWT < prioWT)
            performanceRecommendation = String.format(
                    "Round Robin is recommended as it achieved a lower average Waiting Time (%.2f vs %.2f).", rrWT, prioWT);
        else
            performanceRecommendation = String.format(
                    "Priority Scheduling is recommended as it achieved a lower average Waiting Time (%.2f vs %.2f).", prioWT, rrWT);

        String fairnessConclusion = fairnessIssue
                ? (rrWT <= prioWT
                    ? "Round Robin improved fairness as it distributed CPU time more evenly across all processes."
                    : "Round Robin improved fairness with more even CPU distribution, even though its average WT was not lower.")
                : "Both algorithms showed similar CPU time distribution — no significant fairness difference.";

        String starvationConclusion = starvationRisk
                ? "Starvation risk appeared in Priority Scheduling."
                : "No starvation risk appeared in this run.";

        Label comparisonHeader = new Label("Comparison Summary");
        comparisonHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label conclusionHeader = new Label("Conclusion");
        conclusionHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        box.getChildren().addAll(
                comparisonHeader,
                new Separator(),
                new Label("• " + betterWT),
                new Label("• " + betterRT),
                new Label("• " + priorityAdvantage),
                new Label("• " + balanceObservation),
                new Label("• " + starvationObservation),
                new Separator(),
                conclusionHeader,
                new Label("• " + performanceRecommendation),
                new Label("• " + fairnessConclusion),
                new Label("• " + starvationConclusion)
        );

        return box;
    }

    private double calculateStdDev(List<Process> list) {
        double mean = list.stream().mapToDouble(Process::getWaitingTime).average().orElse(0);
        double variance = list.stream()
                .mapToDouble(p -> Math.pow(p.getWaitingTime() - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    private boolean detectStarvationRisk(List<Process> prioList) {

        if (prioList.size() < 2)
            return false;

        Process mostWaited = prioList.stream()
                .max(Comparator.comparingInt(Process::getWaitingTime))
                .orElse(null);

        if (mostWaited == null)
            return false;

        int maxPriorityValue = prioList.stream()
                .mapToInt(Process::getPriority)
                .max()
                .orElse(mostWaited.getPriority());

        boolean isLowestPriority =
                mostWaited.getPriority() == maxPriorityValue;

        long higherPriorityCount = prioList.stream()
                .filter(p -> p.getPriority() < mostWaited.getPriority())
                .count();

        boolean blockedByMany = higherPriorityCount >= 2;

        int totalHigherPriorityBurst = prioList.stream()
                .filter(p -> p.getPriority() < mostWaited.getPriority())
                .mapToInt(Process::getBurstTime)
                .sum();

        boolean delayedBeyondJustification =
                mostWaited.getWaitingTime() >= totalHigherPriorityBurst;

        return isLowestPriority && blockedByMany && delayedBeyondJustification;
    }

    private VBox createResultSection(String title,
                                     List<Process> results,
                                     List<model.TimeSlot> timeline) {

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-border-color: #34495e; -fx-border-width: 2;");

        TableView<Process> table = new TableView<>();
        setupResultTable(table, results);

        double avgWT  = results.stream().mapToDouble(Process::getWaitingTime).average().orElse(0);
        double avgTAT = results.stream().mapToDouble(Process::getTurnaroundTime).average().orElse(0);
        double avgRT  = results.stream().mapToDouble(Process::getResponseTime).average().orElse(0);

        Label avgLabel = new Label(String.format(
                "Average WT: %.2f  |  Average TAT: %.2f  |  Average RT: %.2f",
                avgWT, avgTAT, avgRT));

        ScrollPane ganttPane = new ScrollPane(drawGantt(timeline));
        ganttPane.setPrefHeight(160);

        box.getChildren().addAll(
                new Label("Algorithm: " + title),
                table,
                avgLabel,
                new Label("Gantt Chart:"),
                ganttPane
        );

        return box;
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

            colorMap.putIfAbsent(slot.processId,
                    Color.color(Math.random() * 0.4 + 0.5,
                            Math.random() * 0.4 + 0.5,
                            Math.random() * 0.4 + 0.5));

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

    private List<Process> cloneList(List<Process> list) {
        List<Process> clone = new ArrayList<>();
        for (Process p : list)
            clone.add(new Process(
                    p.getId(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getPriority()));
        return clone;
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

    public static void main(String[] args) {
        launch(args);
    }
}