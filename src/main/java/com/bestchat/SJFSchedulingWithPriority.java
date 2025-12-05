import java.util.*;

class Process {
    int id;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int priority;
    int finishTime;
    public Process(int id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.finishTime = -1;
    }
}

public class SJFSchedulingWithPriority {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of processes:");
        int n = sc.nextInt();
        List<Process> processes = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            System.out.println("Enter arrival time, burst time, and priority for process " + i + ":");
            int arrival = sc.nextInt();
            int burst = sc.nextInt();
            int priority = sc.nextInt();
            processes.add(new Process(i, arrival, burst, priority));
        }
        // Sort processes by arrival time
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        // Ready queue (priority queue sorted by priority then remaining time then arrival time)
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(new Comparator<Process>() {
            @Override
            public int compare(Process p1, Process p2) {
                if (p1.priority != p2.priority) {
                    return Integer.compare(p1.priority, p2.priority);
                } else if (p1.remainingTime != p2.remainingTime) {
                    return Integer.compare(p1.remainingTime, p2.remainingTime);
                } else {
                    return Integer.compare(p1.arrivalTime, p2.arrivalTime);
                }
            }
        });

        int time = 0;
        int completed = 0;
        int nextIndex = 0;
        Process current = null;
        // Simulation loop
        while (completed < n) {
            // If no process is currently running and CPU is idle, jump to next arrival if exists
            if (current == null && readyQueue.isEmpty()) {
                if (nextIndex < n) {
                    time = processes.get(nextIndex).arrivalTime;
                    // add all processes that arrive at this time
                    while (nextIndex < n && processes.get(nextIndex).arrivalTime == time) {
                        readyQueue.add(processes.get(nextIndex));
                        nextIndex++;
                    }
                    continue; // go to next iteration to select from readyQueue
                } else {
                    break;
                }
            }

            // Add all processes that arrive at this time to ready queue
            while (nextIndex < n && processes.get(nextIndex).arrivalTime == time) {
                readyQueue.add(processes.get(nextIndex));
                nextIndex++;
            }

            // If a process is currently running, check if a new arrival in readyQueue should preempt it
            if (current != null && !readyQueue.isEmpty()) {
                Process top = readyQueue.peek();
                if (top.priority < current.priority ||
                        (top.priority == current.priority && top.remainingTime < current.remainingTime)) {
                    // Preempt current process
                    readyQueue.add(current);
                    current = readyQueue.poll(); // this will retrieve the top (which should be 'top')
                }
            }

            // If no process is running currently, pick one from ready queue
            if (current == null) {
                if (!readyQueue.isEmpty()) {
                    current = readyQueue.poll();
                } else {
                    // if readyQueue empty, loop will continue and either break or jump at top
                    time++;
                    continue;
                }
            }

            // Run the current process for one time unit
            current.remainingTime--;
            // If process finishes execution
            if (current.remainingTime == 0) {
                current.finishTime = time + 1;
                completed++;
                // Process finished, mark it as completed
                current = null;
            }
            // Increment time after executing one unit
            time++;
        }

        // After simulation, output the results for each process
        System.out.println("\nResults:");
        System.out.println("ID\tArrival\tBurst\tPriority\tFinish\tWaiting\tTurnaround");
        for (Process p : processes) {
            // Compute waiting and turnaround times
            int turnaround = p.finishTime - p.arrivalTime;
            int waiting = turnaround - p.burstTime;
            System.out.println(p.id + "\t" + p.arrivalTime + "\t\t" + p.burstTime + "\t\t" + p.priority +
                    "\t\t" + p.finishTime + "\t" + waiting + "\t" + turnaround);
        }
        sc.close();
    }
}
