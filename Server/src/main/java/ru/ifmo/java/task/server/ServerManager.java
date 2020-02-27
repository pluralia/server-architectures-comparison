package ru.ifmo.java.task.server;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.blocked.hard.BlockedHardServer;
import ru.ifmo.java.task.server.blocked.soft.BlockedSoftServer;
import ru.ifmo.java.task.server.unblocked.UnblockedServer;

import java.io.IOException;

public class ServerManager {
    private AbstractServer server;

    public static void main(String[] args) throws IOException, InterruptedException {
        new ServerManager().run(Constants.BLOCKED_HARD, 10, 8);
    }

    public void run(String architectureType, int clientNum, int tasksNum) throws IOException, InterruptedException {
        ServerStat serverStat = new ServerStat(clientNum, tasksNum);

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

        server.run();
    }
}

