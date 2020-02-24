package ru.ifmo.java.task.server;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.blocked.hard.BlockedHardServer;
import ru.ifmo.java.task.server.blocked.soft.BlockedSoftServer;
import ru.ifmo.java.task.server.unblocked.UnblockedServer;

import java.io.IOException;

public class ServerManager {
    private AbstractServer server;

    public static void main(String[] args) throws IOException {
        new ServerManager().run(1, Constants.BLOCKED_SOFT);
    }

    public void run(int clientNum, String architectureType) {
        ServerStat serverStat = new ServerStat(clientNum);

        switch (architectureType) {
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

        try {
            server.run();
        } catch(IOException ignore) {
            System.out.println("SERVER MANAGER");
            server.stop();
        }
    }
}

