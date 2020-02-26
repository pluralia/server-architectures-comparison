package ru.ifmo.java.task.server.blocked.hard;

import jdk.vm.ci.meta.Constant;
import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.Request;
import ru.ifmo.java.task.protocol.Protocol.Response;
import ru.ifmo.java.task.server.ServerStat.*;
import ru.ifmo.java.task.server.ServerStat.ClientStat.*;
import ru.ifmo.java.task.server.blocked.AbstractBlockedServerWorker;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWorker extends AbstractBlockedServerWorker {
//    public final ClientStat clientStat;
//
//    public final CountDownLatch startSignal;
//    public final CountDownLatch doneSignal;

    private final ExecutorService pool;
    private final ExecutorService outputPool = Executors.newSingleThreadExecutor();

    public ServerWorker(Socket socket, ExecutorService pool, ClientStat clientStat,
                        CountDownLatch startSignal, CountDownLatch doneSignal) throws IOException {
        super(socket, clientStat, startSignal, doneSignal);

        this.pool = pool;

        Thread inputThread = new Thread(initInputThread());
        inputThread.setDaemon(true);
        inputThread.start();
    }

    private Runnable initInputThread() {
        return () -> {
            try {
                clientStat.startWaitFor = System.currentTimeMillis();
                startSignal.await();
                clientStat.waitForTime = System.currentTimeMillis() - clientStat.startWaitFor;

                for (int i = 0; i < clientStat.getTasksNum(); i++) {
                    TaskData taskData = clientStat.registerRequest();
                    taskData.startClient = System.currentTimeMillis();

                    pool.submit(initTask(getRequest(), taskData));
                }
            } catch(IOException | InterruptedException e) {
                System.out.println("Server: input thread exception: " + e.getMessage());
            } finally {
                doneSignal.countDown();
            }
       };
    }

    @Override
    public void close() throws IOException {
        super.close();
        outputPool.shutdown();
    }

    private Runnable initTask(Request request, TaskData taskData) {
        return () -> {
            Response response = processRequest(request, taskData);
            outputPool.submit(initOutputPool(response, taskData));
        };
    }

    private Runnable initOutputPool(Response response, TaskData taskData) {
        return () -> {
            try {
                sendResponse(response, taskData);
            } catch(IOException e) {
                System.out.println("Server: output thread exception: " + e.getMessage());
            } finally {
                doneSignal.countDown();
            }
        };
    }
}
