package ru.ifmo.java.task.server;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ConcurrentLinkedQueue<Request> requestQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        new Server().run();
    }

    public void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(Constants.PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            pool.submit(new ServerWorker(socket, requestQueue));
        }
    }
}

