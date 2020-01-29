package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.Request;
import ru.ifmo.java.task.protocol.Protocol.Response;
import sun.security.util.IOUtils;

import java.io.ByteArrayInputStream;
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
        run();
    }

    public void run() throws IOException {
        socket = new Socket(Constants.LOCALHOST, port);
        input = socket.getInputStream();
        output = socket.getOutputStream();

        try {
            sendRequest(Arrays.asList(4, 3, 4, 6, 7, 8));
            receiveResponse();
//            Thread.sleep(1000);

            sendRequest(Arrays.asList(3, 6, 8, 9, 8, 6, 0));
            receiveResponse();
            Thread.sleep(1000);

            sendRequest(Arrays.asList(3, 9, 8, 6, 0));
            receiveResponse();
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        output.write(ByteBuffer.allocate(Constants.INT_SIZE).putInt(packageSize).array());
        request.writeTo(output);
    }

    private void receiveResponse() throws IOException {
        byte[] sizeB = new byte[Constants.INT_SIZE];
        int numOfBytes = input.read(sizeB);
        assert numOfBytes <= Constants.INT_SIZE;
        while (numOfBytes != Constants.INT_SIZE) {
            numOfBytes = input.read(sizeB, numOfBytes, Constants.INT_SIZE - numOfBytes);
        }

        ByteBuffer wrapped = ByteBuffer.wrap(sizeB); // big-endian by default
        int size = wrapped.getInt();

        byte[] protoBuf = new byte[size];
        numOfBytes = input.read(protoBuf);
        assert numOfBytes <= size;
        while (numOfBytes != size) {
            numOfBytes = input.read(protoBuf, numOfBytes, size - numOfBytes);
        }

        Response response = Response.parseFrom(protoBuf);
        System.out.println(response.getSize());
        for (int i : response.getElemList()) {
            System.out.print(i + " ");
        }
        System.out.println();
    }
}
