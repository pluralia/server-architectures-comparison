package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.Lib;
import ru.ifmo.java.task.protocol.Protocol.Request;
import ru.ifmo.java.task.protocol.Protocol.Response;
import ru.ifmo.java.task.server.ServerManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {
    private final Random rand = new Random();

    private Socket socket;
    private InputStream input;
    private OutputStream output;

    public static void main(String[] args) throws IOException {
        new Test(Constants.BLOCKED_SOFT_PORT).run(1000);
    }

    public Test(int port) {
        try {
            socket = new Socket(Constants.LOCALHOST, port);
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("ADDI TIONAL CLIENT");
        }
    }

    public void run(int n) {
        long start = System.currentTimeMillis();
        try {
            sendRequest(generateArray(n));
            receiveResponse();
            socket.close();
            System.out.println(System.currentTimeMillis() - start);
        } catch (IOException e) {
            System.out.println("METRICS BEFORE: ");
            System.out.println(System.currentTimeMillis() - start);
        }
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
        byte[] protoBuf = Lib.receive(input);
        Response response = Response.parseFrom(protoBuf);

        int size = response.getSize();
        System.out.println(size);

        for (int i : response.getElemList()) {
            System.out.print(i + " ");
        }
        System.out.println();
    }
}
