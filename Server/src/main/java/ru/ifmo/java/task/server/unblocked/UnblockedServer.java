package ru.ifmo.java.task.server.unblocked;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.ServerStat;
import ru.ifmo.java.task.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UnblockedServer extends Server {
    private Selector inputSelector;
    private Selector outputSelector;

    private ExecutorService pool;
    private ServerSocketChannel serverSocketChannel;

    private final Lock inputLock = new ReentrantLock();
    private final Lock outputLock = new ReentrantLock();

    public UnblockedServer(ServerStat serverStat) {
        super(serverStat);
    }

    public void run() throws IOException {
        pool = Executors.newFixedThreadPool(Constants.NTHREADS);

        inputSelector = Selector.open();
        Thread inputSelectorThread = initInputSelectorThread();
        inputSelectorThread.setDaemon(true);
        inputSelectorThread.start();

        outputSelector = Selector.open();
        Thread outputSelectorThread = initOutputSelectorThread();
        outputSelectorThread.setDaemon(true);
        outputSelectorThread.start();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(Constants.LOCALHOST, Constants.UNBLOCKED_PORT));

        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            ServerWorker serverWorker = new ServerWorker(serverStat, pool, socketChannel, outputSelector, outputLock);

            inputLock.lock();
            inputSelector.wakeup();
            socketChannel.register(inputSelector, SelectionKey.OP_READ, serverWorker);
            inputLock.unlock();
        }
    }

    public void stop() throws IOException {
        inputSelector.close();
        outputSelector.close();
        serverSocketChannel.close();
        pool.shutdown();
    }

    private Thread initInputSelectorThread() {
        return new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    inputLock.lock();
                    inputLock.unlock();

                    inputSelector.select();

                    Iterator<SelectionKey> keyIterator = inputSelector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey selectionKey = keyIterator.next();

                        if (selectionKey.isReadable()) {
                            ServerWorker serverWorker = (ServerWorker) selectionKey.attachment();

                            assert serverWorker != null;
                            serverWorker.readAndHandle();
                        }

                        keyIterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Thread initOutputSelectorThread() {
        return new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    outputLock.lock();
                    outputLock.unlock();

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
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
