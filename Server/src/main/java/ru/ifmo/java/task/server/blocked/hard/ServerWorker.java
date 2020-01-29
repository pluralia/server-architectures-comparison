package ru.ifmo.java.task.server.blocked.hard;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.Request;
import ru.ifmo.java.task.protocol.Protocol.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWorker {
    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    private final ExecutorService pool;

    private final Thread inputThread;
    private final ExecutorService outputExecutor = Executors.newSingleThreadExecutor();

    public ServerWorker(Socket socket, ExecutorService pool) throws IOException {
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();

        this.pool = pool;

        inputThread = initInputThread();
        inputThread.setDaemon(true);
        inputThread.start();
    }

    private Thread initInputThread() {
        return new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    byte[] sizeB = new byte[Constants.INT_SIZE];
                    int numOfBytes = input.read(sizeB);
                    assert numOfBytes <= Constants.INT_SIZE;
                    while (numOfBytes != Constants.INT_SIZE) {
                        numOfBytes += input.read(sizeB, numOfBytes, Constants.INT_SIZE - numOfBytes);
                    }
                    int size = ByteBuffer.wrap(sizeB).getInt();

                    byte[] protoBuf = new byte[size];
                    numOfBytes = input.read(protoBuf);
                    while (numOfBytes != size) {
                        numOfBytes += input.read(protoBuf, numOfBytes, size - numOfBytes);
                    }
                    Request request = Request.parseFrom(protoBuf);
                    if (request != null) {
                        pool.submit(initTask(request));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    outputExecutor.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Runnable initTask(Request request) {
        return () -> {
            Response response = Response.newBuilder()
                    .setSize(request.getSize())
                    .addAllElem(Constants.SORT.apply(request.getElemList()))
                    .build();
            outputExecutor.submit(initOutputExecutor(response));
        };
    }

    private Runnable initOutputExecutor(Response response) {
        return () -> {
            try {
                int packageSize = response.getSerializedSize();
                output.write(ByteBuffer.allocate(Constants.INT_SIZE).putInt(packageSize).array());
                response.writeTo(output);
            } catch(IOException e) {
                e.printStackTrace();
            }
        };
    }
}
