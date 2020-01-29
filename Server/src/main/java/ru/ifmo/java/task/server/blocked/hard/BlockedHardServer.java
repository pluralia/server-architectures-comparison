package ru.ifmo.java.task.server.blocked.hard;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockedHardServer extends Server {
    private ExecutorService pool;
    private ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        new BlockedHardServer().run();
    }

    public void run() throws IOException {
        pool = Executors.newFixedThreadPool(Constants.NTHREADS);
        serverSocket = new ServerSocket(Constants.BLOCKED_HARD_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            new ServerWorker(socket, pool);
        }
    }

    public void stop() throws IOException {
        pool.shutdown();
        serverSocket.close();
    }
}
