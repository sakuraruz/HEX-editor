package org.example;

import org.example.model.SearchPattern;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class SearchPatternTest {

    @Test
    public void testExactPattern() {
        SearchPattern sp = new SearchPattern("1A 2B 3C");
        assertEquals(3, sp.length());
        assertEquals((byte) 0x1A, sp.getByteAt(0).byteValue());
        assertEquals((byte) 0x2B, sp.getByteAt(1).byteValue());
        assertEquals((byte) 0x3C, sp.getByteAt(2).byteValue());
        assertFalse(sp.isMasked());
        assertTrue(sp.isExact());
    }

    @Test
    public void testMaskedPattern() {
        SearchPattern sp = new SearchPattern("1A ?? 3C");
        assertEquals(3, sp.length());
        assertEquals((byte) 0x1A, sp.getByteAt(0).byteValue());
        assertNull(sp.getByteAt(1));
        assertEquals((byte) 0x3C, sp.getByteAt(2).byteValue());
        assertTrue(sp.isMasked());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidHex() {
        new SearchPattern("1G 2B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPattern() {
        new SearchPattern("");
    }
}