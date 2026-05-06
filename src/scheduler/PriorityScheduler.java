package scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.Process;
import model.TimeSlot;

public class PriorityScheduler {

    public static List<TimeSlot> simulate(List<Process> processes, int quantum) {

        List<TimeSlot> timeline = new ArrayList<>();
        int currentTime = 0;
        int completed = 0;
        int n = processes.size();

        Process lastProcess = null;
        int intervalStart = 0;
        Process lastTiedProcess = null;

        while (completed < n) {
            int bestPriority = Integer.MAX_VALUE;
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && p.remainingTime > 0) {
                    if (p.getPriority() < bestPriority)
                        bestPriority = p.getPriority();
                }
            }

            if (bestPriority == Integer.MAX_VALUE) {
                Optional<Integer> nextArrival = processes.stream()
                        .filter(p -> p.remainingTime > 0)
                        .map(Process::getArrivalTime)
                        .min(Integer::compare);

                if (nextArrival.isPresent())
                    currentTime = nextArrival.get();
                else
                    break;

                lastTiedProcess = null;
                continue;
            }

            int bestArrival = Integer.MAX_VALUE;
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime
                        && p.remainingTime > 0
                        && p.getPriority() == bestPriority) {
                    if (p.getArrivalTime() < bestArrival)
                        bestArrival = p.getArrivalTime();
                }
            }

            List<Process> tiedGroup = new ArrayList<>();
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime
                        && p.remainingTime > 0
                        && p.getPriority() == bestPriority
                        && p.getArrivalTime() == bestArrival) {
                    tiedGroup.add(p);
                }
            }

            Process best;
            boolean isTie = tiedGroup.size() > 1;

            if (!isTie) {
                
                best = tiedGroup.get(0);
                lastTiedProcess = null;

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

            } else {
                int lastIdx = -1;
                if (lastTiedProcess != null) {
                    for (int i = 0; i < tiedGroup.size(); i++) {
                        if (tiedGroup.get(i) == lastTiedProcess) {
                            lastIdx = i;
                            break;
                        }
                    }
                }
                int nextIdx = (lastIdx + 1) % tiedGroup.size();
                best = tiedGroup.get(nextIdx);
                lastTiedProcess = best;

                if (best.responseTime == -1)
                    best.responseTime = currentTime - best.getArrivalTime();

                if (lastProcess != null && lastProcess != best) {
                    timeline.add(new TimeSlot(lastProcess.getId(), intervalStart, currentTime));
                    intervalStart = currentTime;
                } else if (lastProcess == null) {
                    intervalStart = currentTime;
                }
                int runTime = Math.min(best.remainingTime, quantum);
                best.remainingTime -= runTime;
                currentTime += runTime;

                timeline.add(new TimeSlot(best.getId(), intervalStart, currentTime));
                intervalStart = currentTime;
                lastProcess = null;
                if (best.remainingTime == 0) {
                    best.completionTime = currentTime;
                    best.turnaroundTime = best.completionTime - best.getArrivalTime();
                    best.waitingTime = best.turnaroundTime - best.getBurstTime();
                    completed++;
                    if (lastTiedProcess == best)
                        lastTiedProcess = null;
                }
            }
        }

        return timeline;
    }
}
