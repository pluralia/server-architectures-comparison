package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ClientManager {
    private ClientStat clientStat = new ClientStat();

    public static void main(String[] args) throws InterruptedException, IOException {
        new ClientManager().run(Constants.BLOCKED_SOFT_PORT, 10, 4, 1000, 1000);
    }

    public void run(int port, int clientsNum, int taskNum, int taskSize, int sleepTime) throws InterruptedException, IOException {
        ExecutorService pool = Executors.newFixedThreadPool(clientsNum);

        for (int i = 0; i < clientsNum; i++) {
            pool.execute(new Client(port, taskNum, taskSize, sleepTime, clientStat.addClient()));

            // simulate clients connections at different moments
            Thread.sleep(100);
        }

        pool.shutdown();
        pool.awaitTermination(1000, TimeUnit.SECONDS);

        clientStat.save();
    }

    public static class ClientStat {
        private final List<AtomicLong> statList = new ArrayList<>();

        public AtomicLong addClient() {
            AtomicLong stat = new AtomicLong(0);
            statList.add(stat);
            return stat;
        }

        public void save() {
            System.out.println();
            statList.forEach(x -> System.out.println(x.get()));
        }
    }
}
