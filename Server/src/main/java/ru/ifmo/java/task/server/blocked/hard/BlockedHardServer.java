package ru.ifmo.java.task.server.blocked.hard;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.AbstractServer;
import ru.ifmo.java.task.server.ServerStat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockedHardServer extends AbstractServer {
    public ExecutorService pool;
    public ServerSocket serverSocket;

    public BlockedHardServer(ServerStat serverStat) {
        super(serverStat);
    }

    @Override
    public void run() throws IOException {
        pool = Executors.newFixedThreadPool(Constants.NTHREADS);
        serverSocket = new ServerSocket(Constants.BLOCKED_HARD_PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new ServerWorker(serverStat, socket, pool);
        }
    }

    @Override
    public void stop() throws IOException {
        pool.shutdown();
        serverSocket.close();
    }
}
