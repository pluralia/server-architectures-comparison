package ru.ifmo.java.task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Lib {
    public static byte[] receive(InputStream input) throws IOException {
        byte[] sizeB = new byte[Constants.INT_SIZE];
        int numOfBytes = input.read(sizeB);
        assert numOfBytes <= Constants.INT_SIZE;
        while (numOfBytes != Constants.INT_SIZE) {
            numOfBytes += input.read(sizeB, numOfBytes, Constants.INT_SIZE - numOfBytes);
        }

        ByteBuffer wrapped = ByteBuffer.wrap(sizeB); // big-endian by default
        int size = wrapped.getInt();

        byte[] protoBuf = new byte[size];
        numOfBytes = input.read(protoBuf);
        assert numOfBytes <= size;
        while (numOfBytes != size) {
            numOfBytes += input.read(protoBuf, numOfBytes, size - numOfBytes);
        }

        return protoBuf;
    }

    private Lib() {}
}
