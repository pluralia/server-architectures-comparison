package ru.ifmo.java.task.server.unblocked;

import ru.ifmo.java.task.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private Selector inputSelector;
    private Selector outputSelector;

    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.NTHREADS);

    public static void main(String[] args) throws IOException {
        new Server().run();
    }

    public void run() throws IOException {
        inputSelector = Selector.open();
        Thread inputSelectorThread = initInputSelectorThread();
        inputSelectorThread.setDaemon(true);
        inputSelectorThread.start();

        outputSelector = Selector.open();
        Thread outputSelectorThread = initOutputSelectorThread();
        outputSelectorThread.setDaemon(true);
        outputSelectorThread.start();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(Constants.LOCALHOST, Constants.UNBLOCKED_PORT));

        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            PackageHandler packageHandler = new PackageHandler(pool, socketChannel, outputSelector);
            socketChannel.register(inputSelector, SelectionKey.OP_READ, packageHandler);
        }

        // the unreachable state in case of using a while-true cycle
//        inputSelector.close();
//        outputSelector.close();
//        serverSocketChannel.close();
//        pool.shutdown();
    }

    private Thread initInputSelectorThread() {
        return new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    inputSelector.selectNow();
                    Iterator<SelectionKey> keyIterator = inputSelector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey selectionKey = keyIterator.next();

                        if (selectionKey.isReadable()) {
                            PackageHandler packageHandler = (PackageHandler) selectionKey.attachment();

                            if (packageHandler != null) {
                                packageHandler.readAndHandle();
                            }
                        }

                        keyIterator.remove();
                    }
                    Thread.sleep(1000);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private Thread initOutputSelectorThread() {
        return new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    outputSelector.selectNow();
                    Iterator<SelectionKey> keyIterator = outputSelector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey selectionKey = keyIterator.next();

                        if (selectionKey.isWritable()) {
                            PackageHandler packageHandler = (PackageHandler) selectionKey.attachment();

                            if (packageHandler != null) {
                                packageHandler.writeRes();
                            }
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
