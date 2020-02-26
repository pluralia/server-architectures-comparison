package ru.ifmo.java.task.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerStat {
    private int clientsNum;
    private int tasksNum;
    private List<ClientStat> clientStatList = new ArrayList<>();

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
        private ConcurrentLinkedQueue<TaskData> taskDataQueue = new ConcurrentLinkedQueue<>();
        private final int tasksNum;

        public long startWaitFor = 0;
        public long waitForTime = 0;

        public ClientStat(int tasksNum) {
            this.tasksNum = tasksNum;
        }

        public int getTasksNum() {
            return tasksNum;
        }

        public TaskData registerRequest() {
            TaskData taskData = new TaskData();
            taskDataQueue.add(taskData);
            return taskData;
        }

        public void save() {
            System.out.println("CLIENT");
            taskDataQueue.removeIf(TaskData::isNotDone);

            System.out.println("WaitForTime: " + waitForTime);

            while (!taskDataQueue.isEmpty()) {
                System.out.print(taskDataQueue.size() + ": ");
                Objects.requireNonNull(taskDataQueue.poll()).save();
            }
        }

        public static class TaskData {
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
