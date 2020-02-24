package ru.ifmo.java.task.client;

import ru.ifmo.java.task.server.ServerStat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientManager {
//    private ExecutorService pool;
//
//    private final int port;
//    private final int X;
//    private final List<List<Integer>> metricsValues;
//
//    private final AtomicLong timeToResponse = new AtomicLong(0);
//    private final List<List<Long>> timeToResponseStat = new ArrayList<>();
//
//    private ServerStat serverStat;
//    private GuiApplication gui;
//
//    public ClientManager(GuiApplication gui, ServerStat serverStat,
//                         int port, int X,
//                         List<List<Integer>> metricsValues) {
//        this.gui = gui;
//        this.serverStat = serverStat;
//        this.port = port;
//        this.X = X;
//        this.metricsValues = metricsValues;
//    }
//
//    public void run() {
//        int testNum = metricsValues.get(0).size();
//        for (int i = 0; i < testNum; i++) {
//            int N = metricsValues.get(0).get(i);
//            int M = metricsValues.get(1).get(i);
//            int D = metricsValues.get(2).get(i);
//
//            pool = Executors.newFixedThreadPool(M);
//            for (int j = 0; j < M; j++) {
//                pool.submit(initTask(N, D));
//            }
//            pool.shutdown();
//
//            while (!pool.isTerminated());
//
//            List<Long> longStat = Stream.of(X, N, M, D).map(Long::valueOf).collect(Collectors.toList());
//            longStat.add(timeToResponse.get() / M);
//            longStat.add(serverStat.taskOnServer.get() / X * M);
//            longStat.add(serverStat.clientOnServer.get() / X * M);
//            timeToResponseStat.add(longStat);
//
//            timeToResponse.set(0);
//            serverStat.taskOnServer.set(0);
//            serverStat.clientOnServer.set(0);
//        }
//
//        gui.terminateAll();
//    }
//
//    private Runnable initTask(int N, int D) {
//        return () -> {
//            try {
//                timeToResponse.addAndGet(new Client(port).run(X, N, D));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        };
//    }
//
//    public void writeToFileTimeToResponseStat(String archType, String fileName) throws IOException {
//        File file = new File(fileName);
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//
//        FileWriter fileWriter = new FileWriter(file);
//        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//
//        bufferedWriter.write(archType);
//        bufferedWriter.write("\nX N M D RESPONSE_TIME TASK_ON_SERVER CLIENT_ON_SERVER\n");
//
//        for (int i = 0; i < timeToResponseStat.size(); i++) {
//            for (int j = 0; j < timeToResponseStat.get(0).size(); j++) {
//               bufferedWriter.write(timeToResponseStat.get(i).get(j).toString() + " ");
//            }
//            bufferedWriter.write("\n");
//        }
//        bufferedWriter.flush();
//        bufferedWriter.close();
//    }
}
