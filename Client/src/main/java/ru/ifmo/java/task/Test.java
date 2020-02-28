package ru.ifmo.java.task;

import ru.ifmo.java.task.client.ClientManager;
import ru.ifmo.java.task.protocol.Protocol;

import java.io.IOException;
import java.net.Socket;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) throws InterruptedException, IOException {
        new Test(Constants.UNBLOCKED, 2, 3, 10000, 1000).run();
    }

    private final String archType;
    private final int clientNum;
    private final int taskNum;
    private final int taskSize;
    private final int sleepTime;

    public Test(String archType, int clientNum, int taskNum, int taskSize, int sleepTime) {
        this.archType = archType;
        this.clientNum = clientNum;
        this.taskNum = taskNum;
        this.taskSize = taskSize;
        this.sleepTime = sleepTime;
    }

    public String run() throws InterruptedException, IOException {
//        Socket socket = new Socket(InetAddress.getByAddress(Constants.LOCAL_IP), Constants.COMMON_PORT);
        Socket socket = new Socket(Constants.LOCALHOST, Constants.COMMON_PORT);

        sendConfigData(socket);

        System.out.println("CONFIG --> CLIENT");
        Thread.sleep(2000);

        ClientManager clientManager = new ClientManager();

        clientManager.run(Constants.ARCH_TYPE_TO_PORT.get(archType),
                clientNum, taskNum, taskSize, sleepTime);

        System.out.println("CLIENT --> STAT");

        List<Long> clientStat = clientManager.getStat();
        Protocol.ServerData serverStat = receiveServerStat(socket);
        socket.close();

        return getResults(clientStat, serverStat);
    }

    private void sendConfigData(Socket socket) throws IOException {
        Protocol.ServerConfig serverConfig = Protocol.ServerConfig.newBuilder()
                .setArchType(archType)
                .setClientNum(clientNum)
                .setTaskNum(taskNum)
                .build();

        serverConfig.writeDelimitedTo(socket.getOutputStream());
    }

    private Protocol.ServerData receiveServerStat(Socket socket) throws IOException {
        return Protocol.ServerData.parseDelimitedFrom(socket.getInputStream());
    }

    public String getResults(List<Long> clientStat, Protocol.ServerData serverStat) {
        long responseTime = 0;
        long taskTime = 0;
        long clientTime = 0;

        for (int i = 0; i < clientNum; i++) {
            Protocol.ClientData clientData = serverStat.getClientData(i);

            long numOfCompletedTasks = clientData.getRequestDataList().size();

            if (numOfCompletedTasks > 0) {
                responseTime += clientStat.get(i) / numOfCompletedTasks;

                taskTime += clientData.getRequestDataList()
                        .stream()
                        .mapToLong(Protocol.RequestData::getTaskTime)
                        .average().orElse(0);

                clientTime += clientData.getRequestDataList()
                        .stream()
                        .mapToLong(Protocol.RequestData::getClientTime)
                        .average().orElse(0);
            }
        }

        return Stream.of(responseTime, taskTime, clientTime)
                .map(x -> x / clientNum)
                .map(Object::toString)
                .collect(Collectors.joining(" "));
    }
}
