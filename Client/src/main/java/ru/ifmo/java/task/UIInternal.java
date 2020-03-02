package ru.ifmo.java.task;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIInternal {
    public static void main(String[] args) throws InterruptedException, IOException {
//        new UIInternal().run(Constants.UNBLOCKED,
//                Arrays.asList(8, 12, 16, 20, 24, 28, 32), 4, 10000, 3000);
//        new UIInternal().run(Constants.UNBLOCKED, 8, 4,
//                Arrays.asList(2000, 4000, 6000, 8000, 10000, 12000, 14000), 3000);
        new UIInternal().run(Constants.UNBLOCKED, 8, 4, 10000,
                Arrays.asList(0, 500, 1000, 1500, 2000, 2500, 3000));
    }

    public void run(String archType, List<Integer> clientNum, int taskNum, int taskSize, int sleepTime)
            throws IOException, InterruptedException {
        FileOutputStream outputStream = new FileOutputStream(Constants.OUTPUT_TXT);

        outputStream.write((archType + "\n").getBytes());
        outputStream.write("X | N | D\n".getBytes());

        String otherMetrics = Stream.of(taskNum, taskSize, sleepTime)
                .map(Long::toString)
                .collect(Collectors.joining(" "));
        outputStream.write((otherMetrics + "\n").getBytes());

        outputStream.write("M | RESPONSE_TIME | TASK_ON_SERVER | CLIENT_ON_SERVER\n".getBytes());

        for (Integer i : clientNum) {
            String res = new Test(archType, i, taskNum, taskSize, sleepTime).run();
            Thread.sleep(1000);
            outputStream.write((i + " " + res + "\n").getBytes());
        }

        outputStream.close();
    }

    public void run(String archType, int clientNum, int taskNum, List<Integer> taskSize, int sleepTime)
            throws IOException, InterruptedException {
        FileOutputStream outputStream = new FileOutputStream(Constants.OUTPUT_TXT);

        outputStream.write((archType + "\n").getBytes());
        outputStream.write("M | X | D\n".getBytes());

        String otherMetrics = Stream.of(clientNum, taskNum, sleepTime)
                .map(Long::toString)
                .collect(Collectors.joining(" "));
        outputStream.write((otherMetrics + "\n").getBytes());

        outputStream.write("N | RESPONSE_TIME | TASK_ON_SERVER | CLIENT_ON_SERVER\n".getBytes());

        for (Integer i : taskSize) {
            String res = new Test(archType, clientNum, taskNum, i, sleepTime).run();
            Thread.sleep(1000);
            outputStream.write((i + " " + res + "\n").getBytes());
        }

        outputStream.close();
    }

    public void run(String archType, int clientNum, int taskNum, int taskSize, List<Integer> sleepTime)
            throws IOException, InterruptedException {
        FileOutputStream outputStream = new FileOutputStream(Constants.OUTPUT_TXT);

        outputStream.write((archType + "\n").getBytes());
        outputStream.write("M | X | N\n".getBytes());

        String otherMetrics = Stream.of(clientNum, taskNum, taskSize)
                .map(Long::toString)
                .collect(Collectors.joining(" "));
        outputStream.write((otherMetrics + "\n").getBytes());

        outputStream.write("D | RESPONSE_TIME | TASK_ON_SERVER | CLIENT_ON_SERVER\n".getBytes());

        for (Integer i : sleepTime) {
            String res = new Test(archType, clientNum, taskNum, taskSize, i).run();
            Thread.sleep(1000);
            outputStream.write((i + " " + res + "\n").getBytes());
        }

        outputStream.close();
    }
}
