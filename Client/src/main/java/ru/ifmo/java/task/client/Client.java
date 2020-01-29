package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.Request;
import ru.ifmo.java.task.protocol.Protocol.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client {
    private final Random rand = new Random();

    private Socket socket;
    private InputStream input;
    private OutputStream output;

    public Client(int port) throws IOException {
        socket = new Socket(Constants.LOCALHOST, port);
        input = socket.getInputStream();
        output = socket.getOutputStream();
    }

    public long run(int x, int n, int d) throws IOException {
        long time = 0;
        try {
            long start = System.currentTimeMillis();
            for (int i = 0; i < x; i++) {
                sendRequest(generateArray(n));
                receiveResponse();
                Thread.sleep(d);
            }
            long finish = System.currentTimeMillis();
            time = finish - start;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
        return time / x - d;
    }

    private List<Integer> generateArray(int n) {
        List<Integer> array = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            array.add(rand.nextInt((Constants.MAX - Constants.MIN) + 1) + Constants.MIN);
        }
        return array;
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
            numOfBytes += input.read(sizeB, numOfBytes, Constants.INT_SIZE - numOfBytes);
        }

        ByteBuffer wrapped = ByteBuffer.wrap(sizeB); // big-endian by default
        int size = wrapped.getInt();

        byte[] protoBuf = new byte[size];
        numOfBytes = input.read(protoBuf);
        assert numOfBytes <= size;
        while (numOfBytes != size) {
            numOfBytes += input.read(protoBuf, numOfBytes, size - numOfBytes);
        }

        Response response = Response.parseFrom(protoBuf);
        response.getSize();
    }
}
