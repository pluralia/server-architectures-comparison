package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.AbstractServer;
import ru.ifmo.java.task.server.ServerStat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BlockedSoftServer extends AbstractServer {
    private ExecutorService pool;
    private ServerSocket serverSocket;

    private final List<ServerWorker> serverWorkerList = new ArrayList<>();

    private final CountDownLatch startSignal = new CountDownLatch(1);
    private final CountDownLatch finishSignal = new CountDownLatch(1);

    public BlockedSoftServer(ServerStat serverStat) {
        super(serverStat);
    }

    @Override
    public void run() throws IOException, InterruptedException {
        pool = Executors.newCachedThreadPool();
        serverSocket = new ServerSocket(Constants.BLOCKED_SOFT_PORT);

        for (int i = 0; i < serverStat.getClientsNum(); i++) {
            Socket socket = serverSocket.accept();

            ServerWorker serverWorker =
                    new ServerWorker(socket, serverStat.registerClient(), startSignal, finishSignal);
            serverWorkerList.add(serverWorker);

            pool.submit(serverWorker);
        }

        startSignal.countDown();
        finishSignal.await();

        stop();
    }

    @Override
    public void stop() throws IOException {
        pool.shutdown();
        serverSocket.close();
        serverStat.save();

        for (final ServerWorker serverWorker : serverWorkerList) {
            serverWorker.close();
        }
    }
}
