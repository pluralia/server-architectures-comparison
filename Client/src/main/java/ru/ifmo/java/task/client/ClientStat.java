package ru.ifmo.java.task.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ClientStat {
    private final List<AtomicLong> statList = new ArrayList<>();

    public AtomicLong registerClient() {
        AtomicLong stat = new AtomicLong(0);
        statList.add(stat);
        return stat;
    }

    public List<Long> getStat() {
        return statList.stream().map(AtomicLong::get).collect(Collectors.toList());
    }
}
