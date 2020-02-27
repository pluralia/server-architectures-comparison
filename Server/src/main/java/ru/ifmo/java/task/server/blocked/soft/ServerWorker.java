package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.protocol.Protocol;
import ru.ifmo.java.task.server.ServerStat.*;
import ru.ifmo.java.task.server.ServerStat.ClientStat.*;
import ru.ifmo.java.task.server.blocked.AbstractBlockedServerWorker;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class ServerWorker extends AbstractBlockedServerWorker implements Runnable {
//    public final ClientStat clientStat;
//
//    public final CountDownLatch startSignal;
//    public final CountDownLatch doneSignal;

    public ServerWorker(Socket socket, ExecutorService pool, ClientStat clientStat,
                        CountDownLatch startSignal, CountDownLatch doneSignal) throws IOException {
        super(socket, clientStat, startSignal, doneSignal);
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

                Protocol.Request request = getRequest(taskData);
                Protocol.Response response = processRequest(request, taskData);
                sendResponse(response, taskData);
            }
        } catch(Exception e) {
//            System.out.println("Server: tasks exception: " + e.getMessage());
        } finally {
            doneSignal.countDown();
        }
    }
}
