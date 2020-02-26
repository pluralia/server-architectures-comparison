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
    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    private final ClientStat clientStat;

    private final CountDownLatch start;
    private final CountDownLatch finish;

    public ServerWorker(Socket socket, ClientStat clientStat,
                        CountDownLatch start, CountDownLatch finish) throws IOException {
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();

        this.clientStat = clientStat;

        this.start = start;
        this.finish = finish;
    }

    @Override
    public void run() {
        try {
            clientStat.startWaitFor = System.currentTimeMillis();
            start.await();
            clientStat.waitForTime = System.currentTimeMillis() - clientStat.startWaitFor;

            for (int i = 0; i < clientStat.getTasksNum(); i++) {
                TaskData taskData = clientStat.registerRequest();
                taskData.startClient = System.currentTimeMillis();

                sendResponse(processRequest(getRequest(), taskData), taskData);
            }
        } catch(IOException | InterruptedException e) {
            System.out.println("Server: tasks exception: " + e.getMessage());
        } finally {
            finish.countDown();
        }
    }

    public void close() throws IOException {
        socket.close();
    }

    private Request getRequest() throws IOException {
        byte[] protoBuf = Lib.receive(input);
        Request request = Request.parseFrom(protoBuf);

        assert request != null;
        return request;
    }

    private Response processRequest(Request request, TaskData taskData) {
        taskData.startTask = System.currentTimeMillis();
        List<Integer> sortedList = Constants.SORT.apply(request.getElemList());
        taskData.taskTime = System.currentTimeMillis() - taskData.startTask;

        return Response.newBuilder()
                .setSize(request.getSize())
                .addAllElem(sortedList)
                .build();
    }

    private void sendResponse(Response response, TaskData taskData) throws IOException {
        int packageSize = response.getSerializedSize();
        output.write(ByteBuffer.allocate(Constants.INT_SIZE).putInt(packageSize).array());
        response.writeTo(output);

        taskData.clientTime = System.currentTimeMillis() - taskData.startClient;
    }
}
