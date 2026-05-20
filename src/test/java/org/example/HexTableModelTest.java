package org.example;

import org.example.model.ByteFileModel;
import org.example.model.HexTableModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.Assert.assertEquals;

public class HexTableModelTest {

    private ByteFileModel fileModel;
    private HexTableModel tableModel;
    private File tempFile;

    @Before
    public void setUp() throws IOException {
        tempFile = File.createTempFile("modelTest", ".bin");
        byte[] data = new byte[32];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        Files.write(tempFile.toPath(), data);
        fileModel = new ByteFileModel();
        fileModel.openFile(tempFile.getAbsolutePath(), false);
        tableModel = new HexTableModel(fileModel);
    }

    @After
    public void tearDown() throws IOException {
        fileModel.close();
        tempFile.delete();
    }

    @Test
    public void testRowCount() {
        assertEquals(2, tableModel.getRowCount());
        tableModel.setColumns(8);
        assertEquals(4, tableModel.getRowCount());
    }

    @Test
    public void testGetValueAt() {
        assertEquals("00", tableModel.getValueAt(0, 0));
        assertEquals("01", tableModel.getValueAt(0, 1));
    }

    @Test
    public void testSetValueAt() throws IOException {
        tableModel.setValueAt("FF", 0, 0);
        assertEquals("FF", tableModel.getValueAt(0, 0));
        byte b = fileModel.getByte(0);
        assertEquals((byte) 0xFF, b);
    }

    @Test
    public void testCopyPaste() throws IOException {
        tableModel.copySelected(0, 0, 0, 1);
        long startPos = (long) 1 * 16 + 0;
        byte[] clipboard = ((HexTableModel) tableModel).getClipboardBytes();
        for (int i = 0; i < clipboard.length; i++) {
            fileModel.setByte(startPos + i, clipboard[i]);
        }
        tableModel.fireTableDataChanged();

        assertEquals("00", tableModel.getValueAt(1, 0));
        assertEquals("01", tableModel.getValueAt(1, 1));
    }

    @Test
    public void testDeleteSelected() throws IOException {
        fileModel.deleteWithZero(0, 2);
        tableModel.fireTableDataChanged();

        assertEquals("00", tableModel.getValueAt(0, 0));
        assertEquals("00", tableModel.getValueAt(0, 1));
        assertEquals("02", tableModel.getValueAt(0, 2));
    }
}