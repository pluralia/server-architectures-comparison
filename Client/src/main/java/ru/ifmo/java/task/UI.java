package ru.ifmo.java.task;

import ru.ifmo.java.task.client.ClientManager;
import ru.ifmo.java.task.protocol.Protocol;

import java.io.IOException;
import java.net.Socket;

import java.util.List;

public class UI {
    public static void main(String[] args) throws InterruptedException, IOException {
        new UI().run(Constants.BLOCKED_SOFT_PORT, 10, 8, 10000, 0);
    }

    public void run(int port, int clientNum, int taskNum, int taskSize, int sleepTime)
            throws InterruptedException, IOException {
        ClientManager clientManager = new ClientManager();
        clientManager.run(port, clientNum, taskNum, taskSize, sleepTime);

        handlingStat(clientManager);
    }

    private void handlingStat(ClientManager clientManager) throws IOException {
        List<Long> clientStat = clientManager.getStat();
        clientStat.forEach(System.out::println);

        Socket socket = new Socket(Constants.LOCALHOST, Constants.COMMON_PORT);
        Protocol.ServerData serverStat = Protocol.ServerData.parseDelimitedFrom(socket.getInputStream());
        socket.close();
    }
}
