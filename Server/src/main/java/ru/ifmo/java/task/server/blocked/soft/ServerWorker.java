package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.Lib;
import ru.ifmo.java.task.protocol.Protocol.*;
import ru.ifmo.java.task.server.ServerStat.*;
import ru.ifmo.java.task.server.ServerStat.ClientStat.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ServerWorker implements Runnable {
    private final BlockedSoftServer blockedSoftServer;

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    private final ClientStat clientStat;

    private final CountDownLatch countDownLatch;

    public ServerWorker(BlockedSoftServer blockedSoftServer, Socket socket, ClientStat clientStat,
                        CountDownLatch countDownLatch) throws IOException {
        this.blockedSoftServer = blockedSoftServer;

        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();

        this.clientStat = clientStat;

        this.countDownLatch = countDownLatch;
        countDownLatch.countDown();
    }

    @Override
    public void run() {
        try {
            countDownLatch.await();
            while (true) {
                RequestData requestData = clientStat.registerRequest();
                requestData.startClient = System.currentTimeMillis();

                sendResponse(processRequest(getRequest(), requestData), requestData);
            }
        } catch (IOException | InterruptedException e) {
            close();
        }
    }

    private void close() {
        try {
            System.out.println("SERVER WORKER CLOSE");
            socket.close();
            clientStat.save();
            blockedSoftServer.stop();
        } catch (IOException ignore) {
        }
    }

    private Request getRequest() throws IOException {
        byte[] protoBuf = Lib.receive(input);
        Request request = Request.parseFrom(protoBuf);

        assert request != null;
        return request;
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

    private void sendResponse(Response response, RequestData requestData) throws IOException {
        int packageSize = response.getSerializedSize();
        output.write(ByteBuffer.allocate(Constants.INT_SIZE).putInt(packageSize).array());
        response.writeTo(output);

        requestData.clientTime = System.currentTimeMillis() - requestData.startClient;
    }
}
