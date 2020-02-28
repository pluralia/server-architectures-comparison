package ru.ifmo.java.task.server;

import org.w3c.dom.ls.LSOutput;
import ru.ifmo.java.task.protocol.Protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerStat {
    private final int clientsNum;
    private final int tasksNum;
    private final List<ClientStat> clientStatList = new ArrayList<>();

    public ServerStat(int clientsNum, int tasksNum) {
        this.clientsNum = clientsNum;
        this.tasksNum = tasksNum;
    }

    public int getClientsNum() {
        return clientsNum;
    }

    public ClientStat registerClient() {
        assert clientStatList.size() > clientsNum;

        ClientStat clientStat = new ClientStat(tasksNum);
        clientStatList.add(clientStat);
        return clientStat;
    }

    public Protocol.ServerData getStat() {
        List<Protocol.ClientData> clientDataList =
                clientStatList.stream()
                        .map(ClientStat::getStat)
                        .collect(Collectors.toList());
        return Protocol.ServerData.newBuilder()
                .addAllClientData(clientDataList)
                .build();
    }

    public void print() {
        System.out.println("STAT");
        clientStatList.forEach(ClientStat::print);
    }

    public static class ClientStat {
        private List<RequestStat> requestStatList = new ArrayList<>();
        private final int tasksNum;
        private boolean isNotFirst = false;

        public ClientStat(int tasksNum) {
            this.tasksNum = tasksNum;
        }

        public int getTasksNum() {
            return tasksNum;
        }

        public RequestStat registerRequest() {
            assert requestStatList.size() > tasksNum;

            RequestStat requestStat = new RequestStat();
            if (isNotFirst) {
                requestStatList.add(requestStat);
            }
            isNotFirst = true;
            return requestStat;
        }

        public Protocol.ClientData getStat() {
            List<Protocol.RequestData> requestDataList =
                    requestStatList.stream()
                            .filter(RequestStat::isDone)
                            .map(RequestStat::getStat)
                            .collect(Collectors.toList());
            return Protocol.ClientData.newBuilder()
                    .addAllRequestData(requestDataList)
                    .build();
        }

        public void print() {
            System.out.println("CLIENT");
            requestStatList = requestStatList.stream()
                    .filter(RequestStat::isDone)
                    .collect(Collectors.toList());
            for (int i = 0; i < requestStatList.size(); i++) {
                System.out.print((i + 1) + " | ");
                requestStatList.get(i).print();
            }
        }

        public static class RequestStat {
            public long startTask = 0;
            public long taskTime = 0;

            public long startClient = 0;
            public long clientTime = 0;

            public ByteBuffer byteBuffer = null;

            public boolean isDone() {
                return taskTime > 0 && clientTime > 0;
            }

            public Protocol.RequestData getStat() {
                return Protocol.RequestData.newBuilder()
                        .setTaskTime(taskTime)
                        .setClientTime(clientTime)
                        .build();
            }

            public void print() {
                System.out.println("taskTime: " + taskTime + " | clientTime: " + clientTime);
            }
        }
    }
}
