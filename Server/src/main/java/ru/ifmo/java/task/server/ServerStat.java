package ru.ifmo.java.task.server;

import java.util.concurrent.atomic.AtomicLong;

public class ServerStat {
    public final AtomicLong taskOnServer = new AtomicLong(0);
    public final AtomicLong clientOnServer = new AtomicLong(0);
}
