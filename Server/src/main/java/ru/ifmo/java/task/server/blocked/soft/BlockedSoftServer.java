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
    private ExecutorService pool;
    private ServerSocket serverSocket;

    public BlockedSoftServer(ServerStat serverStat) {
        super(serverStat);
    }

    @Override
    public void run() throws IOException {
        pool = Executors.newCachedThreadPool();
        serverSocket = new ServerSocket(Constants.BLOCKED_SOFT_PORT);

        for (int i = 0; i < serverStat.getClientsNum(); i++) {
            Socket socket = serverSocket.accept();
            pool.submit(new ServerWorker(this, socket, serverStat.registerClient()));
        }
    }

    @Override
    public void stop() {
        serverStat.save();
        pool.shutdown();

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

