package ru.ifmo.java.task;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Constants {
    public static final String BLOCKED_SOFT = "Blocked: one-client-one-thread";
    public static final String BLOCKED_HARD = "Blocked: thread pool";
    public static final String UNBLOCKED = "Unblocked";

    public static final Map<String, Integer> ARCH_TYPE_TO_PORT = new HashMap<String, Integer>() {
        {
            put(BLOCKED_SOFT, BLOCKED_SOFT_PORT);
            put(BLOCKED_HARD, BLOCKED_HARD_PORT);
            put(UNBLOCKED, UNBLOCKED_PORT);
        }
    };

    public static final int execNum = 10;

    public static final String LOCALHOST = "localhost";
    public static final byte[] LOCAL_IP = new byte[]{(byte)192, (byte)168, 1, 15};
//    public static final byte[] LOCAL_IP = new byte[]{(byte)172, (byte)131, 0, (byte)229};

    public static final String OUTPUT_TXT = "output.txt";

    public static final int INT_SIZE = 4;

    public static final int BLOCKED_SOFT_PORT = 1234;

    public static final int BLOCKED_HARD_PORT = 1235;
    public static final int NCORES = 8;

    public static final int UNBLOCKED_PORT = 1236;

    public static final int COMMON_PORT = 1237;

    public static final Function<List<Integer>, List<Integer>> SORT = Sort::bubbleSort;

    public static final int MIN = -1000;
    public static final int MAX = 1000;

    private Constants() {}
}
