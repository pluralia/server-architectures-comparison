package ru.ifmo.java.task.server.blocked.soft;

import ru.ifmo.java.task.Constants;
import ru.ifmo.java.task.protocol.Protocol.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ServerWorker implements Runnable {
    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    public ServerWorker(Socket socket) throws IOException {
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] sizeB = new byte[Constants.INT_SIZE];
                input.read(sizeB);
                ByteBuffer wrapped = ByteBuffer.wrap(sizeB); // big-endian by default
                int size = wrapped.getInt();

                byte[] protoBuf = new byte[size];
                input.read(protoBuf);
                Request request = Request.parseFrom(protoBuf);
                if (request != null) {
                    processRequest(request);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRequest(Request request) throws IOException {
        Response response = Response.newBuilder()
                .setSize(request.getSize())
                .addAllElem(Constants.SORT.apply(request.getElemList()))
                .build();

        int packageSize = response.getSerializedSize();
        output.write(ByteBuffer.allocate(Constants.INT_SIZE).putInt(packageSize).array());
        response.writeTo(output);
    }
}
