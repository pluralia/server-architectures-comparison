package ru.ifmo.java.task.server;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol;
import ru.ifmo.java.task.server.blocked.hard.BlockedHardServer;
import ru.ifmo.java.task.server.blocked.soft.BlockedSoftServer;
import ru.ifmo.java.task.server.unblocked.UnblockedServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerManager {
    private AbstractServer server;

    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < Constants.execNum; i++) {
            new ServerManager().run();
        }
    }

    public void run() throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(Constants.COMMON_PORT);
        Socket socket = serverSocket.accept();

        Protocol.ServerConfig serverConfig =
                Protocol.ServerConfig.parseDelimitedFrom(socket.getInputStream());

        System.out.println("CONFIG --> SERVER");

        ServerStat serverStat = new ServerStat(serverConfig.getClientNum(), serverConfig.getTaskNum());

        switch (serverConfig.getArchType()) {
            case Constants.BLOCKED_SOFT:
                server = new BlockedSoftServer(serverStat);
                break;
            case Constants.BLOCKED_HARD:
                server = new BlockedHardServer(serverStat);
                break;
            case Constants.UNBLOCKED:
                server = new UnblockedServer(serverStat);
                break;
        }

        server.run();

        System.out.println("SERVER --> STAT");
        serverStat.print();

        serverStat.getStat().writeDelimitedTo(socket.getOutputStream());

        socket.close();
        serverSocket.close();
    }
}
