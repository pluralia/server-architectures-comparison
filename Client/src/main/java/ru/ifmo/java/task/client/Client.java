package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.Request;
import ru.ifmo.java.task.protocol.Protocol.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class Client {
    private int port;

    private Socket socket;
    private InputStream input;
    private OutputStream output;

    public Client(int port, int X) {
        this.port = port;
    }

    public void run(int n, int m, int d) throws IOException {
        socket = new Socket(Constants.LOCALHOST, port);
        input = socket.getInputStream();
        output = socket.getOutputStream();

        try {
            sendRequest(Arrays.asList(4, 3, 4, 6, 7, 8));
            sendRequest(Arrays.asList(3, 6, 8, 9, 8, 6, 0));
            receiveResponse();
            receiveResponse();
        } finally {
            socket.close();
        }
    }

    private void sendRequest(List<Integer> arr) throws IOException {
        Request request = Request.newBuilder()
                .setSize(arr.size())
                .addAllElem(arr)
                .build();


        int packageSize = request.getSerializedSize();
        assert packageSize > 0 && packageSize < Integer.MAX_VALUE;
        output.write(ByteBuffer.allocate(4).putInt(packageSize).array());

        request.writeDelimitedTo(output);
        output.flush();
    }

    private void receiveResponse() throws IOException {
        Response response = Response.parseDelimitedFrom(input);

        System.out.println(response.getSize());
        for (int i : response.getElemList()) {
            System.out.print(i + " ");
        }
        System.out.println();
    }
}
