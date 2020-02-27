package ru.ifmo.java.task.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientManager {
    private ClientStat clientStat = new ClientStat();

    public void run(int port, int clientsNum, int taskNum, int taskSize, int sleepTime)
            throws InterruptedException, IOException {
        ExecutorService pool = Executors.newFixedThreadPool(clientsNum);

        for (int i = 0; i < clientsNum; i++) {
            pool.submit(new Client(port, taskNum, taskSize, sleepTime, clientStat.registerClient()));

            // simulate clients connections at different moments
            Thread.sleep(100);
        }

        pool.shutdown();
        pool.awaitTermination(1000, TimeUnit.SECONDS);
    }

    public List<Long> getStat() {
        return clientStat.getStat();
    }
}
