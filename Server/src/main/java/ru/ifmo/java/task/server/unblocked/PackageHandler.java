package ru.ifmo.java.task.server.unblocked;


import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;

public class PackageHandler {
    private final ExecutorService pool;
    private final SocketChannel socketChannel;

    private final Selector outputSelector;
    private final Lock writerLock;

    private int size;
    boolean isSizeReading = true;
    int numOfBytes = 0;

    private ByteBuffer head = ByteBuffer.allocate(Constants.INT_SIZE);
    private ByteBuffer body;
    private ConcurrentLinkedQueue<ByteBuffer> resultQueue = new ConcurrentLinkedQueue<>();

    public PackageHandler(ExecutorService pool, SocketChannel socketChannel, Selector outputSelector, Lock writerLock) {
        this.pool = pool;
        this.socketChannel = socketChannel;

        this.outputSelector = outputSelector;
        this.writerLock = writerLock;
    }

    public void readAndHandle() throws IOException {
        if (isSizeReading) {
            numOfBytes += socketChannel.read(head);
            if (numOfBytes == Constants.INT_SIZE) {
                head.flip();
                size = head.getInt();

                numOfBytes = 0;
                isSizeReading = false;
                body = ByteBuffer.allocate(size);
                readAndHandle();
            }
        } else {
            numOfBytes += socketChannel.read(body);
            if (numOfBytes == size) {
                body.flip();
                assert body.remaining() == size;
                byte[] protoBuf = new byte[size];
                body.get(protoBuf);

                Request request = Request.parseFrom(protoBuf);
                if (request != null) {
                    System.out.println(request.getSize());
                    for (int i : request.getElemList()) {
                        System.out.print(i + " ");
                    }
                    System.out.println();

                    pool.submit(initTask(request));
                } else {
                    throw new IOException("null request");
                }

                numOfBytes = 0;
                isSizeReading = true;
                head.clear();
                body.clear();
                readAndHandle();
            }
        }
    }

    private Runnable initTask(Request request) {
        return () -> {
            Response response = Response.newBuilder()
                    .setSize(request.getSize())
                    .addAllElem(Constants.SORT.apply(request.getElemList()))
                    .build();

            writeToOutputBuffer(response);
        };
    }

    private void writeToOutputBuffer(Response response) {
        try {
            System.out.println("RESPONSE " + response.getSize());

            ByteArrayOutputStream protoBufOS = new ByteArrayOutputStream(response.getSerializedSize());
            response.writeTo(protoBufOS);

            ByteBuffer result = ByteBuffer.allocate(response.getSerializedSize());
            result.put(protoBufOS.toByteArray());
            result.flip();
            resultQueue.add(result);

            if (socketChannel.keyFor(outputSelector) == null) {
                writerLock.lock();
                outputSelector.wakeup();
                socketChannel.register(outputSelector, SelectionKey.OP_WRITE, this);
                writerLock.unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeRes() throws IOException {
        if (!resultQueue.isEmpty()) {
            ByteBuffer result = resultQueue.poll();

            ByteBuffer sizeBB = ByteBuffer.allocate(Constants.INT_SIZE).putInt(result.remaining());
            sizeBB.flip();

            while (sizeBB.hasRemaining()) {
                socketChannel.write(sizeBB);
            }

            while (result.hasRemaining()) {
                socketChannel.write(result);
            }
        } else {
            throw new IOException("Empty result queue");
        }
    }
}
