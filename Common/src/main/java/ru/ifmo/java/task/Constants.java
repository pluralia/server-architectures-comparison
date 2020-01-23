package ru.ifmo.java.task;

import java.util.List;
import java.util.function.Function;

public class Constants {
    public static final String HOST = "localhost";

    public static final int BLOCKED_SOFT_PORT = 1234;

    public static final int BLOCKED_HARD_PORT = 1235;
    public static final int NTHREADS = 4;

//    public static final int UNBLOCKED_PORT = 1236;

    public static final Function<List<Integer>, List<Integer>> SORT = Sort::bubbleSort;

    private Constants() {}
}
