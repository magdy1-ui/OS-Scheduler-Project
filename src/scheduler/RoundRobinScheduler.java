package scheduler;

import java.util.*;

import model.Process;
import model.TimeSlot;

public class RoundRobinScheduler {

    public static List<TimeSlot> simulate(List<Process> processes, int quantum) {

        List<TimeSlot> timeline = new ArrayList<>();
        Queue<Process> queue = new LinkedList<>();

        int currentTime = 0;
        int completed = 0;
        int n = processes.size();

        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        int pIdx = 0;

        while (completed < n) {

            while (pIdx < n &&
                    processes.get(pIdx).getArrivalTime() <= currentTime) {
                queue.add(processes.get(pIdx));
                pIdx++;
            }

            if (queue.isEmpty()) {
                if (pIdx < n)
                    currentTime = processes.get(pIdx).getArrivalTime();
                else
                    break;
                continue;
            }

            Process p = queue.poll();

            if (p.responseTime == -1)
                p.responseTime = currentTime - p.getArrivalTime();

            int runTime = Math.min(p.remainingTime, quantum);

            timeline.add(new TimeSlot(
                    p.getId(),
                    currentTime,
                    currentTime + runTime));

            currentTime += runTime;
            p.remainingTime -= runTime;

            while (pIdx < n &&
                    processes.get(pIdx).getArrivalTime() <= currentTime) {
                queue.add(processes.get(pIdx));
                pIdx++;
            }

            if (p.remainingTime > 0) {
                queue.add(p);
            } else {

                p.completionTime = currentTime;
                p.turnaroundTime = p.completionTime - p.getArrivalTime();
                p.waitingTime = p.turnaroundTime - p.getBurstTime();
                completed++;
            }
        }

        return timeline;
    }
}
