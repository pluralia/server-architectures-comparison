package ru.ifmo.java.task.server.blocked;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.AbstractServer;
import ru.ifmo.java.task.server.ServerStat;
import ru.ifmo.java.task.server.ServerStat.*;
import ru.ifmo.java.task.server.blocked.soft.ServerWorker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

abstract public class AbstractBlockedServer extends AbstractServer {
    public ExecutorService pool;
    public final CountDownLatch startSignal = new CountDownLatch(1);
    public final CountDownLatch finishSignal = new CountDownLatch(1);

    private final List<ServerWorker> serverWorkerList = new ArrayList<>();

    public AbstractBlockedServer(ServerStat serverStat) {
        super(serverStat);
    }

    abstract public ExecutorService initPool();
    abstract public int initServerPort();
    abstract public ServerWorker initServerWorker(Socket socket, ClientStat clientStat) throws IOException;

    @Override
    public void run() throws IOException, InterruptedException {
        pool = initPool();
        ServerSocket serverSocket = new ServerSocket(initServerPort());

        for (int i = 0; i < serverStat.getClientsNum(); i++) {
            Socket socket = serverSocket.accept();

            ServerWorker serverWorker = initServerWorker(socket, serverStat.registerClient());
            serverWorkerList.add(serverWorker);
        }
        startSignal.countDown();

        pool.shutdown();
        serverSocket.close();

        finishSignal.await();
        serverStat.save();

        for (final ServerWorker serverWorker : serverWorkerList) {
            serverWorker.close();
        }
    }

    @Override
    public void stop() {    }
}
