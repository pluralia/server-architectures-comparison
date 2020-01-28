package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class GuiApplication {
    private final Client client;

    private final ExecutorService uiThreadPool;

    private boolean notReady = true;

    private final String BLOCKED_SOFT = "Blocked: one-client-one-thread";
    private final String BLOCKED_HARD = "Blocked: thread pool";
    private final String UNBLOCKED = "Unblocked";

    private final String ELEMS_NUMBER = "N - Number of sorted elements";
    private final String CLIENTS_NUMBER = "M - Number of worked clients";
    private final String DURATION = "D - Lag from server response to new request";

    public static void main(String[] args) {
        new GuiApplication().run();
    }

    private GuiApplication() {
//        int port = getPortByArchitecture();
//
//        int X = chooseMetricValue(
//                "Please, enter a number of requires",
//                10,
//                "Error number of requests");
//
//        List<List<Integer>> metricsValues = getMetricsValues();
//        int testNum = metricsValues.get(0).size();

        int port = Constants.UNBLOCKED_PORT;
        int X = 1;
        int testNum = 1;

        client = new Client(port, X);

        for (int i = 0; i < testNum; i++) {
//            int N = metricsValues.get(0).get(i);
//            int M = metricsValues.get(1).get(i);
//            int D = metricsValues.get(2).get(i);
            int N = 1;
            int M = 1;
            int D = 1;

            System.out.println(N + " " + M + " " + D);
            try {
                client.run(N, M, D);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Error on the client");
                System.exit(0);
            }
        }

        uiThreadPool = Executors.newSingleThreadExecutor();
    }

    private int getPortByArchitecture() {
        Map<String, Integer> architectureInfoToPort = new HashMap<String, Integer>() {
            {
                put(BLOCKED_SOFT, Constants.BLOCKED_SOFT_PORT);
                put(BLOCKED_HARD, Constants.BLOCKED_HARD_PORT);
                put(UNBLOCKED, Constants.UNBLOCKED_PORT);
            }
        };

        Object[] architectures = new Object[] { BLOCKED_SOFT, BLOCKED_HARD, UNBLOCKED };
        String architectureString = (String)JOptionPane.showInputDialog(
                null,
                "Please, choose a testing server architecture",
                "Server architecture",
                JOptionPane.QUESTION_MESSAGE,
                null,
                architectures,
                architectures[0]);

        if (architectureString == null || architectureString.isEmpty()) {
            System.exit(0);
        }

        return architectureInfoToPort.get(architectureString);
    }

    private List<List<Integer>> getMetricsValues() {
        String[] metricsInfo = new String[]{ELEMS_NUMBER, CLIENTS_NUMBER, DURATION};

        List<List<Integer>> metricsValues = new ArrayList<>();
        for (int i = 0; i < metricsInfo.length; i++) {
            metricsValues.add(new ArrayList<>());
        }

        Object[] metrics = {"N", "M", "D"};
        int rangedMetricID = JOptionPane.showOptionDialog(
                null,
                "Please, choose changed metric:\n" +
                        String.join("\n", metricsInfo),
                "Metric",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                metrics,
                metrics[0]);

        int startValue = chooseMetricValue(
                "Please, enter a start value for " + metricsInfo[rangedMetricID],
                1,
                "Error number");

        int finishValue = chooseMetricValue(
                "Please, enter a finish value for " + metricsInfo[rangedMetricID],
                10,
                "Error number");

        if (finishValue < startValue) {
            JOptionPane.showMessageDialog(
                    null,
                    "Finish value less that start value");
            System.exit(0);
        }

        int step = chooseMetricValue(
                "Please, enter a step for " + metricsInfo[rangedMetricID],
                1,
                "Error number");

        for (int i = startValue; i < finishValue; i += step) {
            metricsValues.get(rangedMetricID).add(i);
        }

        for (int i = 0; i < metricsInfo.length; i++) {
            if (metricsValues.get(i).isEmpty()) {
                int value = chooseMetricValue(
                        "Please, enter a number of " + metricsInfo[i],
                        1,
                        "Error number of " + metricsInfo[i]);
                for (int j = 0; j < metricsValues.get(0).size(); j++) {
                    metricsValues.get(i).add(value);
                }
            }
        }

        return metricsValues;
    }

    private int chooseMetricValue(String message, int initValue, String errorMessage) {
        try {
            int x = Integer.parseInt(JOptionPane.showInputDialog(
                    message,
                    initValue));
            if (x < 0) {
                throw new NumberFormatException("Negative number!");
            }
            return x;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage);
            System.exit(0);
        }
        // unreachable state
        return 0;
    }

    private void run() {
        uiThreadPool.submit(() -> {
            JFrame mainFrame = createMainFrame();

            JButton downloadButton = createDownloadButton();
            mainFrame.add(downloadButton);

            mainFrame.setVisible(true);
        });
    }

    private JFrame createMainFrame() {
        final JFrame mainFrame = new JFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent x) {
                uiThreadPool.shutdownNow();
            }
        });

        mainFrame.setTitle("Test Server Architectures");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setBounds(100, 100, 1000, 600);

        return mainFrame;
    }

    public JButton createDownloadButton() {
        final JButton downloadButton = new JButton();
        downloadButton.addActionListener(arg -> {
            if (notReady) {
                JOptionPane.showMessageDialog(
                        null,
                        "Data are not ready for the downloading");
            } else {
                download();
                JOptionPane.showMessageDialog(
                        null,
                        "Data were successfully downloaded");
            }
        });

        downloadButton.setText("Download");
        downloadButton.setSize(400,400);
        downloadButton.setVisible(true);
        return downloadButton;
    }

    private void download() {
    }
}