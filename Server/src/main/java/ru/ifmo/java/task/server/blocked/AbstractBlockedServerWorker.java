package ru.ifmo.java.task.server.blocked;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.Lib;
import ru.ifmo.java.task.protocol.Protocol.Request;
import ru.ifmo.java.task.protocol.Protocol.Response;
import ru.ifmo.java.task.server.ServerStat.ClientStat;
import ru.ifmo.java.task.server.ServerStat.ClientStat.RequestStat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AbstractBlockedServerWorker {
    public final ClientStat clientStat;

    public final CountDownLatch startSignal;
    public final CountDownLatch doneSignal;

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    public AbstractBlockedServerWorker(Socket socket, ClientStat clientStat,
                                       CountDownLatch startSignal, CountDownLatch doneSignal) throws IOException {
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();

        this.clientStat = clientStat;

        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
    }

    public void close() throws IOException {
        socket.close();
    }

    public Request getRequest(RequestStat requestStat) throws IOException {
        while (input.available() == 0) {}
        requestStat.startClient = System.currentTimeMillis();

        byte[] protoBuf = Lib.receive(input);
        Request request = Request.parseFrom(protoBuf);

        assert request != null;
        return request;
    }

    public Response processRequest(Request request, RequestStat requestStat) {
        requestStat.startTask = System.currentTimeMillis();
        List<Integer> sortedList = Constants.SORT.apply(request.getElemList());
        requestStat.taskTime = System.currentTimeMillis() - requestStat.startTask;

        return Response.newBuilder()
                .setSize(request.getSize())
                .addAllElem(sortedList)
                .build();
    }

    public void sendResponse(Response response, RequestStat requestStat) throws IOException {
        int packageSize = response.getSerializedSize();
        output.write(ByteBuffer.allocate(Constants.INT_SIZE).putInt(packageSize).array());
        response.writeTo(output);

        requestStat.clientTime = System.currentTimeMillis() - requestStat.startClient;
    }
}
