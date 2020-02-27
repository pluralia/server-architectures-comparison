package ru.ifmo.java.task.server.unblocked;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.ServerStat;
import ru.ifmo.java.task.server.ServerStat.*;
import ru.ifmo.java.task.server.AbstractServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UnblockedServer extends AbstractServer {
    private Selector inputSelector;
    private Selector outputSelector;

    private final CountDownLatch doneSignal = new CountDownLatch(1);

    private final List<ServerWorker> serverWorkerList = new ArrayList<>();

    public UnblockedServer(ServerStat serverStat) {
        super(serverStat);
    }

    @Override
    public void run() throws IOException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(Constants.NCORES);

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(Constants.UNBLOCKED_PORT));

        inputSelector = Selector.open();
        outputSelector = Selector.open();

        for (int i = 0; i < serverStat.getClientsNum(); i++) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            serverWorkerList.add(new ServerWorker(socketChannel, pool,
                    serverStat.registerClient(), doneSignal, outputSelector));
        }

        Thread inputSelectorThread = new Thread(initInputSelectorThread());
        inputSelectorThread.setDaemon(true);
        inputSelectorThread.start();

        Thread outputSelectorThread = new Thread(initOutputSelectorThread());
        outputSelectorThread.setDaemon(true);
        outputSelectorThread.start();

        doneSignal.await();

        pool.shutdownNow();
        for (final ServerWorker serverWorker : serverWorkerList) {
            serverWorker.close();
        }

        serverSocketChannel.close();

        inputSelector.close();
        outputSelector.close();
    }

    private Runnable initInputSelectorThread() {
        for (ServerWorker serverWorker : serverWorkerList) {
            SocketChannel socketChannel = serverWorker.getSocketChannel();
            try {
                socketChannel.register(inputSelector, SelectionKey.OP_READ, serverWorker);
            } catch (ClosedChannelException e) {
                doneSignal.countDown();
            }
        }

        return () -> {
            try {
                while (!Thread.interrupted()) {
                    inputSelector.select();
                    Iterator<SelectionKey> keyIterator = inputSelector.selectedKeys().iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey selectionKey = keyIterator.next();

                        if (selectionKey.isReadable()) {
                            ServerWorker serverWorker = (ServerWorker) selectionKey.attachment();
                            assert serverWorker != null;

                            ClientStat clientStat = serverWorker.getClientStat();
                            if (clientStat.waitForTime == 0) {
                                clientStat.waitForTime =
                                        System.currentTimeMillis() - clientStat.startWaitFor;
                            }

                            serverWorker.getRequestAndHandle();
                        }

                        keyIterator.remove();
                    }
                }
            } catch (Exception e) {
                System.out.println("Input Selector: " + e.getMessage());
                doneSignal.countDown();
            }
        };
    }

    private Runnable initOutputSelectorThread() {
        return () -> {
            try {
                while (!Thread.interrupted()) {
                    outputSelector.select();
                    Iterator<SelectionKey> keyIterator = outputSelector.selectedKeys().iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey selectionKey = keyIterator.next();

                        if (selectionKey.isWritable()) {
                            ServerWorker serverWorker = (ServerWorker) selectionKey.attachment();

                            assert serverWorker != null;
                            serverWorker.writeRes();
                        }

                        keyIterator.remove();
                        selectionKey.cancel();
                    }

                    for (ServerWorker serverWorker : serverWorkerList) {
                        if (serverWorker.isBufferQueueNotEmpty()) {
                            SocketChannel socketChannel = serverWorker.getSocketChannel();
                            if (socketChannel.keyFor(outputSelector) == null) {
                                socketChannel.register(outputSelector, SelectionKey.OP_WRITE, serverWorker);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Output Selector: " + e.getMessage());
                doneSignal.countDown();
            }
        };
    }
}
