package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
//        new Client(Constants.BLOCKED_SOFT_PORT).run();
//        new Client(Constants.BLOCKED_HARD_PORT).run();
        new Client(Constants.UNBLOCKED_PORT, 10).run();
    }
}
