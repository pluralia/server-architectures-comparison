package ru.ifmo.java.task.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientManager {
    private ExecutorService pool;

    private final int port;
    private final int X;
    private final List<List<Integer>> metricsValues;

    private final AtomicLong timeToResponse = new AtomicLong(0);
    private final List<List<Long>> timeToResponseStat = new ArrayList<>();

    public ClientManager(int port, int X, List<List<Integer>> metricsValues) {
        this.port = port;
        this.X = X;
        this.metricsValues = metricsValues;
    }

    public void run() {
        int testNum = metricsValues.get(0).size();
        for (int i = 0; i < testNum; i++) {
            int N = metricsValues.get(0).get(i);
            int M = metricsValues.get(1).get(i);
            int D = metricsValues.get(2).get(i);

            pool = Executors.newFixedThreadPool(M);
            for (int j = 0; j < M; j++) {
                pool.submit(initTask(N, D));
            }
            pool.shutdown();

            while (!pool.isTerminated());

            List<Long> longStat = Stream.of(X, N, M, D).map(Long::valueOf).collect(Collectors.toList());
            longStat.add(timeToResponse.get() / M);
            timeToResponseStat.add(longStat);
            timeToResponse.set(0);
        }
    }

    private Runnable initTask(int N, int D) {
        return () -> {
            try {
                timeToResponse.addAndGet(new Client(port).run(X, N, D));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    public boolean writeToFileTimeToResponseStat(String archType, String fileName) throws IOException {
        System.out.println("---------------------------------------------");

        for (int i = 0; i < timeToResponseStat.size(); i++) {
            for (int j = 0; j < timeToResponseStat.get(0).size(); j++) {
                System.out.print(timeToResponseStat.get(i).get(j) + " ");
            }
            System.out.println();
        }

        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        bufferedWriter.write(archType);
        bufferedWriter.write("X N M D TIME\n");

        for (int i = 0; i < timeToResponseStat.size(); i++) {
            for (int j = 0; j < timeToResponseStat.get(0).size(); j++) {
               bufferedWriter.write(timeToResponseStat.get(i).get(j).toString() + " ");
            }
            bufferedWriter.write("\n");
        }
        bufferedWriter.flush();
        bufferedWriter.close();

        System.out.println("---------------------------------------------");
        return true;
    }
}
