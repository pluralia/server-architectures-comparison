package ru.ifmo.java.task.server;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.blocked.hard.BlockedHardServer;
import ru.ifmo.java.task.server.blocked.soft.BlockedSoftServer;
import ru.ifmo.java.task.server.unblocked.UnblockedServer;

import java.io.IOException;

public class ServerManager {
    private Server server;
    private ServerStat serverStat;

    public ServerManager(ServerStat serverStat) {
        this.serverStat = serverStat;
    }

    public void run(String architectureType) throws IOException {
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

    public void stop() throws IOException {
        server.stop();
    }
}

