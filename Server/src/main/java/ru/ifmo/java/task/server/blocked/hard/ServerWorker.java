package ru.ifmo.java.task.server.blocked.hard;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.Lib;
import ru.ifmo.java.task.protocol.Protocol.Request;
import ru.ifmo.java.task.protocol.Protocol.Response;
import ru.ifmo.java.task.server.ServerStat.*;
import ru.ifmo.java.task.server.ServerStat.ClientStat.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWorker {
    private final BlockedHardServer blockedHardServer;

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    private final ClientStat clientStat;


    private final ExecutorService pool;

    private final ExecutorService outputExecutor = Executors.newSingleThreadExecutor();

    public ServerWorker(BlockedHardServer blockedHardServer, Socket socket, ClientStat clientStat, ExecutorService pool) throws IOException {
        this.blockedHardServer = blockedHardServer;

        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();

        this.clientStat = clientStat;

        this.pool = pool;

        Thread inputThread = new Thread(initInputThread());
        inputThread.setDaemon(true);
        inputThread.start();
    }

    private Runnable initInputThread() {
        return () -> {
            try {
                while (!Thread.interrupted()) {
                    RequestStat requestStat = clientStat.registerRequest();

                    requestStat.startClient = System.currentTimeMillis();

                    Request request = getRequest();

                    pool.submit(initTask(request, requestStat));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
       };
    }

    private Request getRequest() throws IOException {
        byte[] protoBuf = Lib.receive(input);
        Request request = Request.parseFrom(protoBuf);

        assert request != null;
        return request;
    }

    private void close() {
        try {
            socket.close();
            outputExecutor.shutdown();
            blockedHardServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable initTask(Request request, RequestStat requestStat) {
        return () -> {
            requestStat.startTask = System.currentTimeMillis();
            List<Integer> sortedList = Constants.SORT.apply(request.getElemList());
            requestStat.taskTime = System.currentTimeMillis() - requestStat.startTask;

            Response response = Response.newBuilder()
                    .setSize(request.getSize())
                    .addAllElem(sortedList)
                    .build();

            outputExecutor.submit(initOutputExecutor(response, requestStat));
        };
    }

    private Runnable initOutputExecutor(Response response, RequestStat requestStat) {
        return () -> {
            try {
                int packageSize = response.getSerializedSize();
                output.write(ByteBuffer.allocate(Constants.INT_SIZE).putInt(packageSize).array());
                response.writeTo(output);

                requestStat.clientTime = System.currentTimeMillis() - requestStat.startClient;
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        };
    }
}
