package ru.ifmo.java.task.server;

import java.nio.ByteBuffer;
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
        private ConcurrentLinkedQueue<RequestData> requestDataList = new ConcurrentLinkedQueue<>();

        public RequestData registerRequest() {
            RequestData requestData = new RequestData();
            requestDataList.add(requestData);
            return requestData;
        }

        public void save() {
            requestDataList.forEach(RequestData::save);
        }

        public static class RequestData {
            public long startTask = 0;
            public long taskTime = 0;

            public long startClient = 0;
            public long clientTime = 0;

            public ByteBuffer byteBuffer = null;

            public void save() {}
        }
    }
}
