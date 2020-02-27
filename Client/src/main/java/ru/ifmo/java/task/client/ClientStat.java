package ru.ifmo.java.task.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ClientStat {
    private final List<AtomicLong> statList = new ArrayList<>();

    public AtomicLong registerClient() {
        AtomicLong stat = new AtomicLong(0);
        statList.add(stat);
        return stat;
    }

    public void save() {
        System.out.println();
        statList.forEach(x -> System.out.println(x.get()));
    }
}
