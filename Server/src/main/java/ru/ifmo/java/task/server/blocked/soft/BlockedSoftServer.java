package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.ServerStat;
import ru.ifmo.java.task.server.ServerStat.*;
import ru.ifmo.java.task.server.blocked.AbstractBlockedServer;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

public class BlockedSoftServer extends AbstractBlockedServer {
//    public ExecutorService pool;
//    public final CountDownLatch startSignal = new CountDownLatch(1);
//    public final CountDownLatch finishSignal = new CountDownLatch(1);

    public BlockedSoftServer(ServerStat serverStat) {
        super(serverStat);
    }

    @Override
    public ExecutorService initPool() {
        return Executors.newCachedThreadPool();
    }

    @Override
    public int initServerPort() {
        return Constants.BLOCKED_SOFT_PORT;
    }

    @Override
    public ServerWorker initServerWorker(Socket socket, ClientStat clientStat) throws IOException {
        return new ServerWorker(socket, pool, clientStat, startSignal, finishSignal);
    }
}
