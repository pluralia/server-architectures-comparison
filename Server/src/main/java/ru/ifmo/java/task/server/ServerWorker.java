package ru.ifmo.java.task.server;

import ru.ifmo.java.task.Sort;
import ru.ifmo.java.task.protocol.Protocol.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerWorker implements Runnable {
    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;
    private final ConcurrentLinkedQueue<Request> requestQueue;

    public ServerWorker(Socket socket, ConcurrentLinkedQueue<Request> requestQueue) throws IOException {
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Request request = Request.parseDelimitedFrom(input);
                requestQueue.add(request);
                processRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRequest(Request request) throws IOException {
        Response.newBuilder()
                .setSize(request.getSize())
                .addAllElem(Sort.bubbleSort(request.getElemList()))
                .build()
                .writeDelimitedTo(output);
    }
}
