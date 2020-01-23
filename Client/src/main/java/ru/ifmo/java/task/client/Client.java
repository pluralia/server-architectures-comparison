package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.Request;
import ru.ifmo.java.task.protocol.Protocol.Response;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class Client {
    private int port;
    private Socket socket;

    public Client(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        socket = new Socket(Constants.LOCALHOST, port);

        try {
            sendRequest(Arrays.asList(4, 3, 4, 6, 7, 8, 3));
            sendRequest(Arrays.asList(3, 6, 8, 9, 8, 6, 0));
            receiveResponse();
            receiveResponse();
        } finally {
            socket.close();
        }
    }

    private void sendRequest(List<Integer> arr) throws IOException {
        Request.newBuilder()
                .setSize(arr.size())
                .addAllElem(arr)
                .build()
                .writeDelimitedTo(socket.getOutputStream());
    }

    private void receiveResponse() throws IOException {
        Response response = Response.parseDelimitedFrom(socket.getInputStream());

        System.out.println(response.getSize());
        for (int i : response.getElemList()) {
            System.out.print(i + " ");
        }
        System.out.println();
    }
}
