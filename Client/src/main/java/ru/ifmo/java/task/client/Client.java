package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {
    private Socket socket = null;

    public static void main(String[] args) throws IOException {
        new Client().run();
    }

    public void run() throws IOException {
        Scanner scanner = new Scanner(System.in);
        socket = new Socket("localhost", Constants.PORT);
        try {
            List arr = Arrays.asList(4, 3, 4, 6, 7, 8, 3);
            sendRequest(Request.newBuilder()
                    .setSize(arr.size())
                    .addAllElem(arr)
                    .build()
            );
            receiveResponse();
        } finally {
            socket.close();
        }
    }

    private void receiveResponse() throws IOException {
        Response response = Response.parseDelimitedFrom(socket.getInputStream());
        System.out.println(response.getSize());
        for (int i : response.getElemList()) {
            System.out.print(i + " ");
        }
    }

    private void sendRequest(Request request) throws IOException {
        request.writeDelimitedTo(socket.getOutputStream());
    }
}
