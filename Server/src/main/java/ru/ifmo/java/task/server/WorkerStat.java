package ru.ifmo.java.task.server;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkerStat {
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

        public void save() {
            System.out.println("taskTime: " + taskTime);
            System.out.println("clientTime: " + clientTime);
        }
    }
}
