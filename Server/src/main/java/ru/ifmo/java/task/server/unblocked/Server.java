package ru.ifmo.java.task.server.blocked.hard;

import ru.ifmo.java.task.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.NTHREADS);

    public static void main(String[] args) throws IOException {
        new Server().run();
    }

    public void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(Constants.BLOCKED_HARD_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            new ServerWorker(socket, pool);
        }

        // the unreachable state in case of using a while-true cycle
//        serverSocket.close()
//        pool.shutdown();
    }
}
