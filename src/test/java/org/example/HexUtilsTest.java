package org.example;

import org.example.service.HexUtils;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class HexUtilsTest {

    @Test
    public void testIsHexChar() {
        assertTrue(HexUtils.isHexChar('0'));
        assertTrue(HexUtils.isHexChar('9'));
        assertTrue(HexUtils.isHexChar('A'));
        assertTrue(HexUtils.isHexChar('F'));
        assertTrue(HexUtils.isHexChar('a'));
        assertTrue(HexUtils.isHexChar('f'));
        assertFalse(HexUtils.isHexChar('G'));
        assertFalse(HexUtils.isHexChar(' '));
        assertFalse(HexUtils.isHexChar('x'));
    }
}