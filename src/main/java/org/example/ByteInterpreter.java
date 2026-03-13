package org.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    public java.math.BigInteger getUInt64() {
        if (bytes.length < 8) return java.math.BigInteger.ZERO;
        byte[] unsignedBytes = new byte[8];
        System.arraycopy(bytes, 0, unsignedBytes, 0, 8);
        return new java.math.BigInteger(1, unsignedBytes); // 1 = positive
    }

    public double getFloat64() {
        if (bytes.length < 8) return 0;
        return buffer.getDouble(0);
    }
}