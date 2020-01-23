package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        new Server().run();
    }

    public void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(Constants.BLOCKED_SOFT_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            pool.submit(new ServerWorker(socket));
        }

        // the unreachable state in case of using a while-true cycle
//        serverSocket.close()
//        pool.shutdown();
    }
}

