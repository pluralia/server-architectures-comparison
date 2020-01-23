package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.Sort;
import ru.ifmo.java.task.protocol.Protocol.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ServerWorker implements Runnable {
    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    public ServerWorker(Socket socket) throws IOException {
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Request request = Request.parseDelimitedFrom(input);
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
