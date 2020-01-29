package ru.ifmo.java.task.client;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.server.ServerStat;
import ru.ifmo.java.task.server.ServerManager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class GuiApplication {
    private final ExecutorService uiThreadPool;
    private final ExecutorService workerThreadPool;

    private ServerManager serverManager;
    private ClientManager clientManager;

    private final String ELEMS_NUMBER = "N - Number of sorted elements";
    private final String CLIENTS_NUMBER = "M - Number of worked clients";
    private final String DURATION = "D - Lag from server response to new request";

    private String architectureString;
    private final String fileName = "output.txt";

    private ServerStat serverStat = new ServerStat();

    public static void main(String[] args) {
        new GuiApplication().run();
    }

    private GuiApplication() {
        String architectureType = getArchitectureType();

        int X = chooseMetricValue(
                "Please, enter a number of requires",
                10,
                "Error number of requests");

        uiThreadPool = Executors.newFixedThreadPool(2);
        workerThreadPool = Executors.newFixedThreadPool(2);

        serverManager = new ServerManager(serverStat);
        workerThreadPool.submit(() -> {
            try {
                serverManager.run(architectureType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        int port = Constants.ARCH_TYPE_TO_PORT.get(architectureType);
        clientManager = new ClientManager(this, serverStat, port, X, getMetricsValues());
        workerThreadPool.submit(clientManager::run);

        workerThreadPool.shutdown();
    }

    private String getArchitectureType() {
        Object[] architectures = new Object[]
                { Constants.BLOCKED_SOFT, Constants.BLOCKED_HARD, Constants.UNBLOCKED };
        architectureString = (String)JOptionPane.showInputDialog(
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

        return architectureString;
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

            JButton downloadButton = createStatusButton();
            mainFrame.add(downloadButton);

            mainFrame.setVisible(true);
        });
    }

    public void terminateAll() {
        uiThreadPool.shutdown();
        workerThreadPool.shutdown();

        try {
            clientManager.writeToFileTimeToResponseStat(architectureString, fileName);
            serverManager.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JFrame createMainFrame() {
        final JFrame mainFrame = new JFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent x) {
                terminateAll();
            }
        });

        mainFrame.setTitle("Test Server Architectures");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setBounds(100, 100, 500, 300);

        return mainFrame;
    }

    public JButton createStatusButton() {
        final JButton statusButton = new JButton();
        statusButton.addActionListener(arg ->
            JOptionPane.showMessageDialog(
                        null,
                        "Data are not ready for the downloading"));

        statusButton.setText("Status");
        statusButton.setSize(200,200);
        statusButton.setVisible(true);
        return statusButton;
    }
}
