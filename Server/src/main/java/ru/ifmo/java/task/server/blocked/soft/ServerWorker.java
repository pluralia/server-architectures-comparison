package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.Lib;
import ru.ifmo.java.task.protocol.Protocol.*;
import ru.ifmo.java.task.server.ServerStat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

public class ServerWorker implements Runnable {
    private final ServerStat serverStat;

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    // we work with time in different threads to get statistic;
    // it's okay for our goals when we want to get general statistic
    // so - we interested in the difference between moments of start and finish for all requires
    // and the order of math operations for this goal doesn't matter
    private long startClientOnServer = 0;
    private long finishClientOnServer = 0;

    public ServerWorker(ServerStat serverStat, Socket socket) throws IOException {
        this.serverStat = serverStat;

        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            while (true) {
                startClientOnServer = System.currentTimeMillis();

                byte[] protoBuf = Lib.receive(input);
                Request request = Request.parseFrom(protoBuf);
                assert request != null;
                processRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRequest(Request request) throws IOException {
        long start = System.currentTimeMillis();
        List<Integer> sortedList = Constants.SORT.apply(request.getElemList());
        long finish = System.currentTimeMillis();
        serverStat.taskOnServer.addAndGet(finish - start);

        Response response = Response.newBuilder()
                .setSize(request.getSize())
                .addAllElem(sortedList)
                .build();

        int packageSize = response.getSerializedSize();
        output.write(ByteBuffer.allocate(Constants.INT_SIZE).putInt(packageSize).array());
        response.writeTo(output);

        finishClientOnServer = System.currentTimeMillis();
        serverStat.clientOnServer.addAndGet(finishClientOnServer - startClientOnServer);
    }
}
