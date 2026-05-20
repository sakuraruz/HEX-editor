package org.example;

import org.example.model.ByteInterpreter;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ByteInterpreterTest {

    @Test
    public void testInt16() {
        byte[] bytes = {0x34, 0x12};
        ByteInterpreter bi = new ByteInterpreter(bytes);
        assertEquals(4660, bi.getInt16());
        assertEquals(4660, bi.getUInt16());
    }

    @Test
    public void testInt16Negative() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xFF};
        ByteInterpreter bi = new ByteInterpreter(bytes);
        assertEquals(-1, bi.getInt16());
        assertEquals(65535, bi.getUInt16());
    }

    @Test
    public void testInt32() {
        byte[] bytes = {0x78, 0x56, 0x34, 0x12};
        ByteInterpreter bi = new ByteInterpreter(bytes);
        assertEquals(305419896, bi.getInt32());
        assertEquals(305419896L, bi.getUInt32());
    }

    @Test
    public void testFloat32() {
        byte[] bytes = {0x00, 0x00, (byte) 0x80, 0x3F};
        ByteInterpreter bi = new ByteInterpreter(bytes);
        assertEquals(1.0f, bi.getFloat32(), 0.0001);
    }

    @Test
    public void testInt64() {
        byte[] bytes = {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ByteInterpreter bi = new ByteInterpreter(bytes);
        assertEquals(1L, bi.getInt64());
    }

    @Test
    public void testDouble64() {
        byte[] bytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xF0, 0x3F};
        ByteInterpreter bi = new ByteInterpreter(bytes);
        assertEquals(1.0, bi.getFloat64(), 0.0001);
    }

    @Test
    public void testInsufficientBytes() {
        ByteInterpreter bi = new ByteInterpreter(new byte[1]);
        assertEquals(0, bi.getInt16());
        assertEquals(0, bi.getInt32());
        assertEquals(0.0f, bi.getFloat32(), 0.0);
        assertEquals(0L, bi.getInt64());
        assertEquals(0.0, bi.getFloat64(), 0.0);
    }
}