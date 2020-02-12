package ru.ifmo.java.task.server;

import java.io.IOException;

public abstract class AbstractServer {
    public ServerStat serverStat;

    public AbstractServer(ServerStat serverStat) {
        this.serverStat = serverStat;
    }

    public abstract void run() throws IOException;

    public abstract void stop();
}

