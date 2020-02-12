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
import java.util.concurrent.ExecutorService;

public class ServerWorker {
    private final UnblockedServer unblockedServer;

    private final SocketChannel socketChannel;
    private final ClientStat clientStat;

    private final ExecutorService pool;
    private final Selector outputSelector;

    private int size;
    private boolean isSizeReading = true;
    private int numOfBytes = 0;

    private boolean isFirst = true;
    private RequestData currRequestData;

    private ByteBuffer head = ByteBuffer.allocate(Constants.INT_SIZE);
    private ByteBuffer body;
    private ConcurrentLinkedQueue<RequestData> bufferQueue = new ConcurrentLinkedQueue<>();

    public ServerWorker(UnblockedServer unblockedServer, SocketChannel socketChannel, ClientStat clientStat,
                        ExecutorService pool, Selector outputSelector) {
        this.unblockedServer = unblockedServer;

        this.socketChannel = socketChannel;
        this.clientStat = clientStat;

        this.pool = pool;
        this.outputSelector = outputSelector;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void close() {
        try {
            socketChannel.close();
            unblockedServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getRequestAndHandle() throws IOException {
        if (isFirst) {
            currRequestData = clientStat.registerRequest();
            currRequestData.startClient = System.currentTimeMillis();
            isFirst = false;
        }

        if (isSizeReading) {
            numOfBytes += socketChannel.read(head);
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

                pool.submit(initTask(request, currRequestData));

                numOfBytes = 0;
                isSizeReading = true;
                head.clear();
                body.clear();

                isFirst = true;

                getRequestAndHandle();
            }
        }
    }

    private Runnable initTask(Request request, RequestData requestData) {
        return () -> {
            Response response = processRequest(request, requestData);
            putOnQueue(response, requestData);
        };
    }

    private void putOnQueue(Response response, RequestData requestData) {
        try {
            ByteArrayOutputStream protoBufOS = new ByteArrayOutputStream(response.getSerializedSize());
            response.writeTo(protoBufOS);

            ByteBuffer result = ByteBuffer.allocate(response.getSerializedSize());
            result.put(protoBufOS.toByteArray());
            result.flip();

            requestData.byteBuffer = result;
            bufferQueue.add(requestData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public boolean isBufferQueueNotEmpty() {
        return !bufferQueue.isEmpty();
    }

    public synchronized void writeRes() throws IOException {
        assert !bufferQueue.isEmpty();

        RequestData requestData = bufferQueue.poll();
        ByteBuffer result = requestData.byteBuffer;

        ByteBuffer sizeBB = ByteBuffer.allocate(Constants.INT_SIZE).putInt(result.remaining());
        sizeBB.flip();

        outputSelector.wakeup();

        while (sizeBB.hasRemaining()) {
            socketChannel.write(sizeBB);
        }

        while (result.hasRemaining()) {
            socketChannel.write(result);
        }

        requestData.clientTime = System.currentTimeMillis() - requestData.startClient;
    }

    private Response processRequest(Request request, RequestData requestData) {
        requestData.startTask = System.currentTimeMillis();
        List<Integer> sortedList = Constants.SORT.apply(request.getElemList());
        requestData.taskTime = System.currentTimeMillis() - requestData.startTask;

        return Response.newBuilder()
                .setSize(request.getSize())
                .addAllElem(sortedList)
                .build();
    }
}
