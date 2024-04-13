import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

class Project3 {
    public static void main(String[] args) throws Exception {
        // TEST
        // System.out.println('Z' - 'A');
        // System.out.println((char) 65);

        String jobsPath = "";
        if (args.length > 0) {
            jobsPath = args[0];
        }

        parseJobs(jobsPath);

        int[][] s = fcfs();
        System.out.println("\nFCFS\n");
        printSchedule(s);

        s = rr(1);
        System.out.println("\nRR (q=1)\n");
        printSchedule(s);

        s = rr(2);
        System.out.println("\nRR (q=2)\n");
        printSchedule(s);

        s = rr(3);
        System.out.println("\nRR (q=3)\n");
        printSchedule(s);

        s = rr(4);
        System.out.println("\nRR (q=4)\n");
        printSchedule(s);
    }

    private static HashMap<Character, Job> jobs = new HashMap<>();

    private static class Job {
        char jobName;
        int startTime;
        int duration;

        public Job(char jobName, int startTime, int duration) {
            this.jobName = jobName;
            this.startTime = startTime;
            this.duration = duration;
        }

        public String toString() {
            return jobName + " " + startTime + " " + duration;
        }
    }

    private static class SortJobComparator implements Comparator<Job> {
        @Override
        public int compare(Job o1, Job o2) {
            return o1.startTime - o2.startTime;
        }
    }

    private static void parseJobs(String jobsPath) throws Exception {
        File file = new File(jobsPath);
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] tokens = line.split("\\s+");
            char job = tokens[0].charAt(0);
            int startTime = Integer.parseInt(tokens[1]);
            int duration = Integer.parseInt(tokens[2]);

            // TEST
            // System.out.println(job + " " + startTime + " " + duration);

            jobs.put(job, new Job(job, startTime, duration));
        }

        sc.close();
    }

    // First come first serve scheduling
    private static int[][] fcfs() {
        // Shallow copy
        List<Job> sortedJobs = new ArrayList<>(jobs.values());
        Collections.sort(sortedJobs, new SortJobComparator());

        int maxDuration = 0;
        for (int i = 0; i < sortedJobs.size(); i++) {
            maxDuration += sortedJobs.get(i).duration;
        }

        int[][] schedule = new int[26][maxDuration];
        Queue<Job> queue = new LinkedList<>();

        for (Job job : sortedJobs) {
            queue.add(job);
        }

        int curTime = 0;
        while (!queue.isEmpty()) {
            Job job = queue.poll();
            int idx = job.jobName - 'A';

            int duration = job.duration;
            while (duration > 0) {
                schedule[idx][curTime] = 1;
                curTime++;
                duration--;
            }
        }

        return schedule;
    }

    // Round robin scheduling
    private static int[][] rr(int timeQuantum) {
        // Shallow copy
        List<Job> sortedJobs = new ArrayList<>(jobs.values());
        Collections.sort(sortedJobs, new SortJobComparator());

        int maxDuration = 0;
        int[] durationLeft = new int[26];

        for (int i = 0; i < sortedJobs.size(); i++) {
            Job job = sortedJobs.get(i);
            maxDuration += job.duration;
            durationLeft[job.jobName - 'A'] = job.duration;
        }

        int[][] schedule = new int[26][maxDuration];
        Queue<Job> queue = new LinkedList<>();

        int nextJobIdx = 0;
        Job cpuJob = null;
        int cpuJobTime = 0;

        for (int curTime = 0; curTime <= maxDuration; curTime++) {
            // Let job execute
            if (curTime >= 1 && cpuJob != null) {
                int idx = cpuJob.jobName - 'A';
                schedule[idx][curTime - 1] = 1;
                cpuJobTime++;
                durationLeft[idx]--;

                // Job is done => Remove it from CPU
                if (durationLeft[idx] == 0) {
                    cpuJob = null;
                    cpuJobTime = 0;
                }
            }

            // Job arrives
            if (nextJobIdx < sortedJobs.size()) {
                if (curTime == sortedJobs.get(nextJobIdx).startTime) {
                    // TEST
                    // System.out.println("Job arrives: " + curTime + " / " +
                    // sortedJobs.get(nextJobIdx));

                    queue.add(sortedJobs.get(nextJobIdx));
                    nextJobIdx++;
                }
            }

            // TEST
            // System.out.println(queue);
            // System.out.println(cpuJob + " / " + cpuJobTime);

            // Preempt
            if (cpuJobTime == timeQuantum) {
                // Add job back to queue
                if (cpuJob != null) {
                    queue.add(cpuJob);
                }

                // Get job out of CPU
                cpuJob = null;
                cpuJobTime = 0;
            }

            // TEST
            // System.out.println(queue);
            // System.out.println(cpuJob + " / " + cpuJobTime);

            // Set current CPU job if none
            if (cpuJob == null) {
                cpuJob = queue.poll();
                cpuJobTime = 0;
            }

            // TEST
            // System.out.println(queue);
            // System.out.println(cpuJob + " / " + cpuJobTime);
            // System.out.println();
        }

        return schedule;
    }

    private static void printSchedule(int[][] schedule) {
        for (int i = 0; i < schedule.length; i++) {
            char jobName = (char) (i + 'A');

            if (!jobs.containsKey(jobName)) {
                continue;
            }

            System.out.print(jobName + " ");

            for (int j = 0; j < schedule[0].length; j++) {
                if (schedule[i][j] == 1)
                    System.out.print("X");
                else
                    System.out.print(" ");
            }

            System.out.println();
        }
    }
}