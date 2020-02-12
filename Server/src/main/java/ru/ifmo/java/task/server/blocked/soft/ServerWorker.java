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

public class ServerWorker implements Runnable {
    private final BlockedSoftServer blockedSoftServer;

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    private final ClientStat clientStat;

    public ServerWorker(BlockedSoftServer blockedSoftServer, Socket socket, ClientStat clientStat) throws IOException {
        this.blockedSoftServer = blockedSoftServer;

        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();

        this.clientStat = clientStat;
    }

    @Override
    public void run() {
        try {
            while (true) {
                RequestData requestData = clientStat.registerRequest();
                requestData.startClient = System.currentTimeMillis();

                sendResponse(processRequest(getRequest(), requestData), requestData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void close() {
        try {
            socket.close();
            blockedSoftServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
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
