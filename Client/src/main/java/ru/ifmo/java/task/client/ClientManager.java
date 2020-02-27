package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientManager {
    private ClientStat clientStat = new ClientStat();

    public static void main(String[] args) throws InterruptedException, IOException {
        new ClientManager().run(Constants.BLOCKED_SOFT_PORT, 10, 8, 10000, 0);
    }

    public void run(int port, int clientsNum, int taskNum, int taskSize, int sleepTime) throws InterruptedException, IOException {
        ExecutorService pool = Executors.newFixedThreadPool(clientsNum);

        for (int i = 0; i < clientsNum; i++) {
            pool.submit(new Client(port, taskNum, taskSize, sleepTime, clientStat.registerClient()));

            // simulate clients connections at different moments
            Thread.sleep(100);
        }

        pool.shutdown();
        pool.awaitTermination(1000, TimeUnit.SECONDS);

        clientStat.save();
    }
}
