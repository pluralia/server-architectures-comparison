package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.AbstractServer;
import ru.ifmo.java.task.server.ServerStat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockedSoftServer extends AbstractServer {
    public ExecutorService pool;
    public ServerSocket serverSocket;

    public BlockedSoftServer(ServerStat serverStat) {
        super(serverStat);
    }

    @Override
    public void run() throws IOException {
        pool = Executors.newCachedThreadPool();
        serverSocket = new ServerSocket(Constants.BLOCKED_SOFT_PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            pool.submit(new ServerWorker(serverStat, socket));
        }
    }

    @Override
    public void stop() throws IOException {
        pool.shutdown();
        serverSocket.close();
    }
}

