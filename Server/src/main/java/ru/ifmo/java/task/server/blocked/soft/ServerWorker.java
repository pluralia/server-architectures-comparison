package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.Lib;
import ru.ifmo.java.task.protocol.Protocol.*;
import ru.ifmo.java.task.server.ServerStat.*;
import ru.ifmo.java.task.server.ServerStat.ClientStat.*;
import ru.ifmo.java.task.server.blocked.AbstractBlockedServerWorker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class ServerWorker extends AbstractBlockedServerWorker implements Runnable {
    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    private final ClientStat clientStat;

    private final CountDownLatch startSignal;
    private final CountDownLatch doneSignal;

    public ServerWorker(Socket socket, ExecutorService pool, ClientStat clientStat,
                        CountDownLatch startSignal, CountDownLatch doneSignal) throws IOException {
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();

        this.clientStat = clientStat;

        this.startSignal = startSignal;
        this.doneSignal = doneSignal;

        pool.submit(this);
    }

    @Override
    public void run() {
        try {
            clientStat.startWaitFor = System.currentTimeMillis();
            startSignal.await();
            clientStat.waitForTime = System.currentTimeMillis() - clientStat.startWaitFor;

            for (int i = 0; i < clientStat.getTasksNum(); i++) {
                TaskData taskData = clientStat.registerRequest();
                taskData.startClient = System.currentTimeMillis();

                sendResponse(processRequest(getRequest(), taskData), taskData);
            }
        } catch(IOException | InterruptedException e) {
            System.out.println("Server: tasks exception: " + e.getMessage());
        } finally {
            doneSignal.countDown();
        }
    }
}
