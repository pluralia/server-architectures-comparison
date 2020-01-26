package ru.ifmo.java.task.server.unblocked;


import org.jetbrains.annotations.NotNull;
import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

public class PackageHandler {
    private final ExecutorService pool;
    private final SocketChannel socketChannel;
    private final Selector outputSelector;

    private final ByteBuffer inputByteBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
    private final ByteBuffer outputByteBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);

    private boolean isSizeReading = true;
    private int size;

    public PackageHandler(ExecutorService pool, SocketChannel socketChannel, Selector outputSelector) {
        this.pool = pool;
        this.socketChannel = socketChannel;
        this.outputSelector = outputSelector;
    }

    public void readAndHandle() throws IOException {
        socketChannel.read(inputByteBuffer);
        inputByteBuffer.flip();
        handlingRead();
    }

    public synchronized void writeRes() throws IOException {
        outputByteBuffer.flip();
        socketChannel.write(outputByteBuffer);
        outputByteBuffer.compact();
    }

    // if this is the first reading of a new package
    // try to read only a size of the protobuf part;
    // else read protobuf part
    private void handlingRead() throws IOException {
        if (isSizeReading) {
            if (inputByteBuffer.limit() - inputByteBuffer.position() >= Constants.INT_SIZE) {
                isSizeReading = false;
                size = inputByteBuffer.getInt();
                handlingRead();
            }
        } else if (inputByteBuffer.limit() - inputByteBuffer.position() >= size) {
            isSizeReading = true;

            Request request = Request.parseDelimitedFrom(new ByteBufferInputStream(inputByteBuffer));
            if (request != null) {
                pool.submit(initTask(request));
            }

            if (inputByteBuffer.limit() == inputByteBuffer.position()) {
                inputByteBuffer.compact();
                return;
            }

            handlingRead();
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

    private synchronized void writeToOutputBuffer(Response response) {
        try {
            System.out.println("RESPONSE " + response.getSize());

            response.writeDelimitedTo(new ByteBufferOutputStream(outputByteBuffer));

            if (socketChannel.keyFor(outputSelector) == null) {
                outputSelector.wakeup();
                socketChannel.register(outputSelector, SelectionKey.OP_WRITE, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ByteBufferInputStream and ByteBufferOutputStream from
    // http://www.java2s.com/Code/Java/File-Input-Output/CreatinganinputoroutputstreamonaByteBuffer.htm

    private class ByteBufferInputStream extends InputStream {
        private ByteBuffer buf;

        ByteBufferInputStream(ByteBuffer buf) {
            this.buf = buf;
        }

        public synchronized int read() {
            if (!buf.hasRemaining()) {
                return -1;
            }
            return buf.get();
        }

        public synchronized int read(@NotNull byte[] bytes, int off, int len) {
            len = Math.min(len, buf.remaining());
            buf.get(bytes, off, len);
            return len;
        }
    }

    private class ByteBufferOutputStream extends OutputStream {
        private ByteBuffer buf;

        ByteBufferOutputStream(ByteBuffer buf) {
            this.buf = buf;
        }
        public synchronized void write(int b) {
            buf.put((byte) b);
        }

        public synchronized void write(@NotNull byte[] bytes, int off, int len) {
            buf.put(bytes, off, len);
        }
    }
}
