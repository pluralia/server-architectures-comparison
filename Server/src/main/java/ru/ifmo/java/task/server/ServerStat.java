package ru.ifmo.java.task.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerStat {
    private int clientsNum;
    private List<ClientStat> clientStatList = new ArrayList<>();

    public ServerStat(int clientsNum) {
        this.clientsNum = clientsNum;
    }

    public int getClientsNum() {
        return clientsNum;
    }

    public ClientStat registerClient() {
        if (clientStatList.size() < clientsNum) {
            ClientStat clientStat = new ClientStat();
            clientStatList.add(clientStat);
            return clientStat;
        } else {
            throw new IndexOutOfBoundsException("You register too many clients!");
        }
    }

    public void save() {
        System.out.println("SAVE");
        clientStatList.forEach(ClientStat::save);
    }

    public static class ClientStat {
        private ConcurrentLinkedQueue<RequestStat> requestStatList = new ConcurrentLinkedQueue<>();

        public RequestStat registerRequest() {
            RequestStat requestStat = new RequestStat();
            requestStatList.add(requestStat);
            return requestStat;
        }

        public void save() {
            requestStatList.forEach(RequestStat::save);
        }

        public static class RequestStat {
            public long startTask = 0;
            public long taskTime = 0;

            public long startClient = 0;
            public long clientTime = 0;

            public void save() {}
        }
    }
}
