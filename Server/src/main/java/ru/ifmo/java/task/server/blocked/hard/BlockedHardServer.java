package ru.ifmo.java.task.server.blocked.hard;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.ServerStat;
import ru.ifmo.java.task.server.blocked.AbstractBlockedServer;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class BlockedHardServer extends AbstractBlockedServer {
//    public ExecutorService pool;
//    public final CountDownLatch startSignal = new CountDownLatch(1);
//    public final CountDownLatch finishSignal = new CountDownLatch(1);

    public BlockedHardServer(ServerStat serverStat) {
        super(serverStat);
    }

    @Override
    public ExecutorService initPool() {
        return Executors.newFixedThreadPool(Constants.NCORES);
    }

    @Override
    public int initServerPort() {
        return Constants.BLOCKED_HARD_PORT;
    }

    @Override
    public ServerWorker initServerWorker(Socket socket) throws IOException {
        return new ServerWorker(socket, pool, serverStat.registerClient(), startSignal, doneSignal);
    }
}
