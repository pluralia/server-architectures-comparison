package ru.ifmo.java.task.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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

    public void save() {
        System.out.println("SAVE");
        clientStatList.forEach(ClientStat::save);
    }

    public static class ClientStat {
        private final List<RequestStat> requestStatList = new ArrayList<>();
        private final int tasksNum;

        public long startWaitFor = 0;
        public long waitForTime = 0;

        public ClientStat(int tasksNum) {
            this.tasksNum = tasksNum;
        }

        public int getTasksNum() {
            return tasksNum;
        }

        public RequestStat registerRequest() {
            assert requestStatList.size() > tasksNum;

            RequestStat requestStat = new RequestStat();
            requestStatList.add(requestStat);
            return requestStat;
        }

        public void save() {
            System.out.println("CLIENT");
            requestStatList.removeIf(RequestStat::isNotDone);

            System.out.println("WaitForTime: " + waitForTime);

            for (int i = 0; i < requestStatList.size(); i++) {
                System.out.print(i + ": ");
                requestStatList.get(i).save();
            }
        }

        public static class RequestStat {
            public long startTask = 0;
            public long taskTime = 0;

            public long startClient = 0;
            public long clientTime = 0;

            public ByteBuffer byteBuffer = null;

            public boolean isNotDone() {
                return taskTime == 0 || clientTime == 0;
            }

            public void save() {
                System.out.println("taskTime: " + taskTime + " | clientTime: " + clientTime);
            }
        }
    }
}
