package org.example.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.math.BigInteger;

public class ByteInterpreter {

    private ByteBuffer buffer;
    private byte[] bytes;

    public ByteInterpreter(byte[] bytes) {
        this.bytes = bytes;
        this.buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public short getInt16() {
        if (bytes.length < 2) return 0;
        return buffer.getShort(0);
    }

    public int getUInt16() {
        if (bytes.length < 2) return 0;
        return buffer.getShort(0) & 0xFFFF;
    }

    public int getInt32() {
        if (bytes.length < 4) return 0;
        return buffer.getInt(0);
    }

    public long getUInt32() {
        if (bytes.length < 4) return 0;
        return buffer.getInt(0) & 0xFFFFFFFFL;
    }

    public float getFloat32() {
        if (bytes.length < 4) return 0;
        return buffer.getFloat(0);
    }

    public long getInt64() {
        if (bytes.length < 8) return 0;
        return buffer.getLong(0);
    }

    public BigInteger getUInt64() {
        if (bytes.length < 8) return BigInteger.ZERO;
        byte[] unsignedBytes = new byte[8];
        System.arraycopy(bytes, 0, unsignedBytes, 0, 8);
        return new BigInteger(1, unsignedBytes);
    }

    public double getFloat64() {
        if (bytes.length < 8) return 0;
        return buffer.getDouble(0);
    }
}