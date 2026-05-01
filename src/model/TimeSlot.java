package model;

public class TimeSlot {
    public String processId;
    public int startTime;
    public int endTime;
    public int duration;

    public TimeSlot(String id, int start, int end) {
        this.processId = id;
        this.startTime = start;
        this.endTime = end;
        this.duration = end - start;
    }
}