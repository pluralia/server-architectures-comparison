package ru.ifmo.java.task.server.unblocked;


import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.*;
import ru.ifmo.java.task.server.ServerStat.*;
import ru.ifmo.java.task.server.ServerStat.ClientStat.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class ServerWorker {
    private final SocketChannel socketChannel;
    private final ExecutorService pool;
    private final ClientStat clientStat;
    private final CountDownLatch doneSignal;
    private final Selector outputSelector;

    private int size;
    private boolean isSizeReading = true;
    private int numOfBytes = 0;

    private boolean isFirst = true;
    private RequestStat currRequestStat;

    private ByteBuffer head = ByteBuffer.allocate(Constants.INT_SIZE);
    private ByteBuffer body;
    private ConcurrentLinkedQueue<RequestStat> bufferQueue = new ConcurrentLinkedQueue<>();

    private int taskCounter;

    public ServerWorker(SocketChannel socketChannel, ExecutorService pool, ClientStat clientStat,
                        CountDownLatch doneSignal, Selector outputSelector) {
        this.socketChannel = socketChannel;

        this.clientStat = clientStat;
        taskCounter = clientStat.getTasksNum();

        this.pool = pool;
        this.doneSignal = doneSignal;
        this.outputSelector = outputSelector;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public ClientStat getClientStat() {
        return clientStat;
    }

    public void close() throws IOException {
        socketChannel.close();
    }

    public void getRequestAndHandle() throws IOException {
        if (isFirst) {
            currRequestStat = clientStat.registerRequest();
            isFirst = false;
        }

        if (isSizeReading) {
            long num;
            do {
                num = socketChannel.read(head);
            } while (num == 0);

            currRequestStat.startClient = System.currentTimeMillis();

            numOfBytes += num;
            socketChannel.read(head);

            if (numOfBytes == Constants.INT_SIZE) {
                head.flip();
                size = head.getInt();

                numOfBytes = 0;
                isSizeReading = false;
                body = ByteBuffer.allocate(size);
                getRequestAndHandle();
            }
        } else {
            numOfBytes += socketChannel.read(body);
            if (numOfBytes == size) {
                body.flip();
                assert body.remaining() == size;
                byte[] protoBuf = new byte[size];
                body.get(protoBuf);

                Request request = Request.parseFrom(protoBuf);
                assert request != null;

                pool.submit(initTask(request, currRequestStat));

                numOfBytes = 0;
                isSizeReading = true;
                head.clear();
                body.clear();

                isFirst = true;

                getRequestAndHandle();
            }
        }
    }

    private Runnable initTask(Request request, RequestStat requestStat) {
        return () -> {
            Response response = processRequest(request, requestStat);
            putOnQueue(response, requestStat);
        };
    }

    private void putOnQueue(Response response, RequestStat requestStat) {
        try {
            ByteArrayOutputStream protoBufOS = new ByteArrayOutputStream(response.getSerializedSize());
            response.writeTo(protoBufOS);

            ByteBuffer result = ByteBuffer.allocate(response.getSerializedSize());
            result.put(protoBufOS.toByteArray());
            result.flip();

            requestStat.byteBuffer = result;
            bufferQueue.add(requestStat);

            outputSelector.wakeup();
        } catch (Exception e) {
            System.out.println("ServerWorker: putOnQueue: " + e.getMessage());
            doneSignal.countDown();
        }
    }

    public boolean isBufferQueueNotEmpty() {
        return !bufferQueue.isEmpty();
    }

    public synchronized void writeRes() throws IOException {
        assert !bufferQueue.isEmpty();

        RequestStat requestStat = bufferQueue.poll();
        ByteBuffer result = requestStat.byteBuffer;

        ByteBuffer sizeBB = ByteBuffer.allocate(Constants.INT_SIZE).putInt(result.remaining());
        sizeBB.flip();

        while (sizeBB.hasRemaining()) {
            socketChannel.write(sizeBB);
        }

        while (result.hasRemaining()) {
            socketChannel.write(result);
        }

        requestStat.clientTime = System.currentTimeMillis() - requestStat.startClient;

        taskCounter -= 1;
        if (taskCounter == 0) {
            doneSignal.countDown();
        }
    }

    private Response processRequest(Request request, RequestStat requestStat) {
        requestStat.startTask = System.currentTimeMillis();
        List<Integer> sortedList = Constants.SORT.apply(request.getElemList());
        requestStat.taskTime = System.currentTimeMillis() - requestStat.startTask;

        return Response.newBuilder()
                .setSize(request.getSize())
                .addAllElem(sortedList)
                .build();
    }
}
