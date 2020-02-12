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
        this.clientStat = clientStat;

        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();

        this.blockedSoftServer = blockedSoftServer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                RequestStat requestStat = clientStat.registerRequest();

                requestStat.startClient = System.currentTimeMillis();

                Request request = getRequest();

                requestStat.startTask = System.currentTimeMillis();
                List<Integer> sortedList = Constants.SORT.apply(request.getElemList());
                requestStat.taskTime = System.currentTimeMillis() - requestStat.startTask;

                sendResponse(request, sortedList);

                requestStat.clientTime = System.currentTimeMillis() - requestStat.startClient;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                blockedSoftServer.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Request getRequest() throws IOException {
        byte[] protoBuf = Lib.receive(input);
        Request request = Request.parseFrom(protoBuf);

        assert request != null;
        return request;
    }

    private void sendResponse(Request request, List<Integer> sortedList) throws IOException {
        Response response = Response.newBuilder()
                .setSize(request.getSize())
                .addAllElem(sortedList)
                .build();

        int packageSize = response.getSerializedSize();
        output.write(ByteBuffer.allocate(Constants.INT_SIZE).putInt(packageSize).array());
        response.writeTo(output);
    }
}
