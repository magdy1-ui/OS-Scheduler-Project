package scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.Process;
import model.TimeSlot;

public class PriorityScheduler {

    public static List<TimeSlot> simulate(List<Process> processes) {

        List<TimeSlot> timeline = new ArrayList<>();
        int currentTime = 0;
        int completed = 0;
        int n = processes.size();

        Process lastProcess = null;
        int intervalStart = 0;

        while (completed < n) {

            Process best = null;

            for (Process p : processes) {

                if (p.getArrivalTime() <= currentTime && p.remainingTime > 0) {

                    if (best == null ||
                            p.getPriority() < best.getPriority()) {
                        best = p;
                    }
                }
            }

            if (best == null) {

                Optional<Integer> nextArrival = processes.stream()
                        .filter(p -> p.remainingTime > 0)
                        .map(Process::getArrivalTime)
                        .min(Integer::compare);

                if (nextArrival.isPresent())
                    currentTime = nextArrival.get();
                else
                    break;

                continue;
            }

            if (best.responseTime == -1)
                best.responseTime = currentTime - best.getArrivalTime();

            if (lastProcess != null && lastProcess != best) {
                timeline.add(new TimeSlot(lastProcess.getId(), intervalStart, currentTime));
                intervalStart = currentTime;
            } else if (lastProcess == null) {
                intervalStart = currentTime;
            }

            best.remainingTime--;
            currentTime++;
            lastProcess = best;

            if (best.remainingTime == 0) {

                timeline.add(new TimeSlot(best.getId(), intervalStart, currentTime));

                best.completionTime = currentTime;
                best.turnaroundTime = best.completionTime - best.getArrivalTime();
                best.waitingTime = best.turnaroundTime - best.getBurstTime();

                completed++;
                lastProcess = null;
            }
        }

        return timeline;
    }
}

