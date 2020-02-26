package ru.ifmo.java.task.server.unblocked;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.ServerStat;
import ru.ifmo.java.task.server.AbstractServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UnblockedServer extends AbstractServer {
    private Selector inputSelector;
    private Selector outputSelector;

    private ExecutorService pool;
    private ServerSocketChannel serverSocketChannel;

    private List<ServerWorker> serverWorkers = new ArrayList<>();

    public UnblockedServer(ServerStat serverStat) {
        super(serverStat);
    }

    @Override
    public void run() throws IOException {
        pool = Executors.newFixedThreadPool(Constants.NCORES);

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(Constants.LOCALHOST, Constants.UNBLOCKED_PORT));

        for (int i = 0; i < serverStat.getClientsNum(); i++) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            serverWorkers.add(new ServerWorker(this, socketChannel, serverStat.registerClient(),
                    pool, outputSelector));
        }

        inputSelector = Selector.open();
        Thread inputSelectorThread = new Thread(initInputSelectorThread());
        inputSelectorThread.setDaemon(true);
        inputSelectorThread.start();

        outputSelector = Selector.open();
        Thread outputSelectorThread = new Thread(initOutputSelectorThread());
        outputSelectorThread.setDaemon(true);
        outputSelectorThread.start();
    }

    @Override
    public void stop() {
        try {
            serverStat.save();

            inputSelector.close();
            outputSelector.close();

            serverSocketChannel.close();

            pool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable initInputSelectorThread() {
        for (ServerWorker serverWorker : serverWorkers) {
            SocketChannel socketChannel = serverWorker.getSocketChannel();
            try {
                socketChannel.register(inputSelector, SelectionKey.OP_READ, serverWorker);
            } catch (ClosedChannelException e) {
                stop();
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
                            serverWorker.getRequestAndHandle();
                        }

                        keyIterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stop();
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

                    for (ServerWorker serverWorker : serverWorkers) {
                        if (serverWorker.isBufferQueueNotEmpty()) {
                            SocketChannel socketChannel = serverWorker.getSocketChannel();
                            if (socketChannel.keyFor(outputSelector) == null) {
                                socketChannel.register(outputSelector, SelectionKey.OP_WRITE, serverWorker);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        };
    }
}
