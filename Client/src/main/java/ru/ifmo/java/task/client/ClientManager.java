package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientManager {
    private final ExecutorService pool;
    private final int port;

    private final int x;
    private final int n;
    private final int m;
    private final int d;

    public ClientManager(int port, int x, int n, int m, int d) {
        this.port = port;

        this.x = x;
        this.n = n;
        this.m = m;
        this.d = d;

        pool = Executors.newFixedThreadPool(m);
    }

    public void run() {
        for (int i = 0; i < m; i++) {
            pool.submit(initTask());
        }
        pool.shutdown();
    }

    private Runnable initTask() {
        return () -> {
            try {
                new Client(port).run(x, n, d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}
