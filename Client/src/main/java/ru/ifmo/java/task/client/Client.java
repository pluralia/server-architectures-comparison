package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.Lib;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Client implements Runnable {
    private final Random rand = new Random();

    private final int taskNum;
    private final int taskSize;
    private final int sleepTime;
    private final AtomicLong stat;

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    public Client(int port, int taskNum, int taskSize, int sleepTime, AtomicLong stat) throws IOException {
        this.taskNum = taskNum;
        this.taskSize = taskSize;
        this.sleepTime = sleepTime;
        this.stat = stat;

//        socket = new Socket(InetAddress.getByAddress(Constants.LOCAL_IP), port);
        socket = new Socket(Constants.LOCALHOST, port);
        input = socket.getInputStream();
        output = socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < taskNum; i++) {
                long start = System.currentTimeMillis();
                sendRequest(generateArray());

                if (receiveResponse()) {
                    stat.addAndGet(System.currentTimeMillis() - start);
                }

                Thread.sleep(sleepTime);
            }
        } catch (IOException | InterruptedException e) {
//            System.out.println("Client: tasks exception: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
//                System.out.println("Client: socket close exception: " + e.getMessage());
            }
        }
    }

    private List<Integer> generateArray() {
        List<Integer> array = new ArrayList<>();
        for (int i = 0; i < taskSize; i++) {
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

    private boolean receiveResponse() throws IOException {
        byte[] protoBuf = Lib.receive(input);
        Response response = Response.parseFrom(protoBuf);

//        System.out.println(response.getSize());
//        System.out.println(response.getElemList().stream()
//                .map(Object::toString)
//                .collect(Collectors.joining(" ")));

        return response.getElemList().size() == taskSize;
    }
}
