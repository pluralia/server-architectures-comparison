package ru.ifmo.java.task;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UI {
//    public static void main(String[] args) throws InterruptedException, IOException {
////        new UIInternal().run(Constants.UNBLOCKED,
////                Arrays.asList(8, 12, 16, 20, 24, 28, 32), 4, 10000, 3000);
////        new UIInternal().run(Constants.UNBLOCKED, 8, 4,
////                Arrays.asList(2000, 4000, 6000, 8000, 10000, 12000, 14000), 3000);
//        new UIInternal().run(Constants.UNBLOCKED, 8, 4, 10000,
//                Arrays.asList(0, 500, 1000, 1500, 2000, 2500, 3000));
//    }
    private ExecutorService uiThreadPool;

    private final String ELEMS_NUMBER = "N - Number of sorted elements";
    private final String CLIENTS_NUMBER = "M - Number of worked clients";
    private final String DURATION = "D - Lag from server response to new request";

    private final String[] metricsInfo = new String[]{CLIENTS_NUMBER, ELEMS_NUMBER, DURATION};

    private boolean notReady = true;

    public static void main(String[] args) throws IOException, InterruptedException {
        new UI().run();
    }

    private void run() throws IOException, InterruptedException {
        String archType = getArchType();

        int X = chooseMetricValue(
                "Please, enter a number of requires",
                10,
                "Error number of requests");

        List<Integer> rangedMetric = getRangedMetric();
        int rangedMetricID = rangedMetric.get(0);
        rangedMetric = rangedMetric.subList(1, rangedMetric.size());

        List<Integer> otherMetrics = getOtherMetrics(rangedMetricID);
        int a = otherMetrics.get(0);
        int b = otherMetrics.get(1);

        uiThreadPool = Executors.newSingleThreadExecutor();
        uiThreadPool.submit(() -> {
            JFrame mainFrame = createMainFrame();

            JButton downloadButton = createDownloadButton();
            mainFrame.add(downloadButton);

            mainFrame.setVisible(true);
        });

        switch (rangedMetricID) {
            case 0:
                new UIInternal().run(archType, rangedMetric, X, a, b);
                break;
            case 1:
                new UIInternal().run(archType, a, X, rangedMetric, b);
                break;
            case 2:
                new UIInternal().run(archType, a, X, b, rangedMetric);
                break;
            default:
                throw new IOException("Strange metric!!");
        }

        notReady = false;
    }

    private String getArchType() {
        Object[] architectures = Constants.ARCH_TYPE_TO_PORT.keySet().toArray();
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

        return architectureString;
    }

    private List<Integer> getRangedMetric() {
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

        List<Integer> rangedMetric = new LinkedList<>();
        rangedMetric.add(rangedMetricID);

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

        int step = (finishValue - startValue) / Constants.execNum;
        for (int i = startValue; i < finishValue; i += step) {
            rangedMetric.add(i);
        }

        return rangedMetric;
    }

    private List<Integer> getOtherMetrics(int rangedMetricID) {
        List<Integer> otherMetrics = new ArrayList<>();

        for (int i = 0; i < metricsInfo.length; i++) {
            if (i != rangedMetricID) {
                int value = chooseMetricValue(
                        "Please, enter a number of " + metricsInfo[i],
                        1,
                        "Error number of " + metricsInfo[i]);
                otherMetrics.add(value);
            }
        }

        return otherMetrics;
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