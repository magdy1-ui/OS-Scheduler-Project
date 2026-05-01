package model;

public class Process {
    private String id;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    public int remainingTime;
    public int completionTime;
    public int waitingTime;
    public int turnaroundTime;
    public int responseTime = -1;

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
    }

    public String getId() { return id; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
    public int getCompletionTime() { return completionTime; }
    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getResponseTime() { return responseTime; }
}