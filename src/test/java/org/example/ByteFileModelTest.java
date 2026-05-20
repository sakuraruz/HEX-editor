package org.example;

import org.example.model.ByteFileModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class ByteFileModelTest {

    private ByteFileModel model;
    private File tempFile;

    @Before
    public void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".bin");
        byte[] data = new byte[256];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        Files.write(tempFile.toPath(), data);
        model = new ByteFileModel();
    }

    @After
    public void tearDown() throws IOException {
        if (model != null) model.close();
        if (tempFile != null) tempFile.delete();
    }

    @Test
    public void testOpenReadOnly() throws IOException {
        model.openFile(tempFile.getAbsolutePath(), true);
        assertTrue(model.isReadOnly());
        assertEquals(256, model.getFileSize());
        assertEquals(0, model.getByte(0) & 0xFF);
        assertEquals(127, model.getByte(127) & 0xFF);
        assertEquals(255, model.getByte(255) & 0xFF);
        model.setByte(0, (byte) 0xAA);
        assertFalse(model.isModified());
        assertEquals(0, model.getByte(0) & 0xFF);
    }

    @Test
    public void testOpenReadWrite() throws IOException {
        model.openFile(tempFile.getAbsolutePath(), false);
        assertFalse(model.isReadOnly());
        model.setByte(0, (byte) 0xAA);
        assertTrue(model.isModified());
        assertEquals(0xAA, model.getByte(0) & 0xFF);
        model.save();
        assertFalse(model.isModified());
        model.close();
        model.openFile(tempFile.getAbsolutePath(), true);
        assertEquals(0xAA, model.getByte(0) & 0xFF);
    }

    @Test
    public void testEnableEditing() throws IOException {
        model.openFile(tempFile.getAbsolutePath(), true);
        assertTrue(model.isReadOnly());
        model.enableEditing();
        assertFalse(model.isReadOnly());
        model.setByte(10, (byte) 0x55);
        model.save();
        model.close();
        model.openFile(tempFile.getAbsolutePath(), true);
        assertEquals(0x55, model.getByte(10) & 0xFF);
    }

    @Test
    public void testSaveAs() throws IOException {
        model.openFile(tempFile.getAbsolutePath(), false);
        model.setByte(10, (byte) 0x77);
        model.saveAs(tempFile.getAbsolutePath() + ".copy");
        File copy = new File(tempFile.getAbsolutePath() + ".copy");
        assertTrue(copy.exists());
        ByteFileModel copyModel = new ByteFileModel();
        copyModel.openFile(copy.getAbsolutePath(), true);
        assertEquals(0x77, copyModel.getByte(10) & 0xFF);
        copyModel.close();
        copy.delete();
    }

    @Test
    public void testDeleteWithZero() throws IOException {
        model.openFile(tempFile.getAbsolutePath(), false);
        model.deleteWithZero(10, 5);
        model.save();
        model.close();
        model.openFile(tempFile.getAbsolutePath(), true);
        for (int i = 10; i <= 14; i++) {
            assertEquals(0, model.getByte(i) & 0xFF);
        }
        assertEquals(9, model.getByte(9) & 0xFF);
        assertEquals(15, model.getByte(15) & 0xFF);
    }

    @Test
    public void testDeleteWithShift() throws IOException {
        model.openFile(tempFile.getAbsolutePath(), false);
        model.deleteWithShift(10, 5);
        model.save();
        model.close();
        model.openFile(tempFile.getAbsolutePath(), true);
        assertEquals(251, model.getFileSize());
        assertEquals(15, model.getByte(10) & 0xFF);
        assertEquals(16, model.getByte(11) & 0xFF);
        assertEquals(255, model.getByte(250) & 0xFF);
    }

    @Test
    public void testInsertWithShift() throws IOException {
        model.openFile(tempFile.getAbsolutePath(), false);
        byte[] data = {0x11, 0x22, 0x33};
        model.insertWithShift(10, data);
        model.save();
        model.close();
        model.openFile(tempFile.getAbsolutePath(), true);
        assertEquals(259, model.getFileSize());
        assertEquals(0x11, model.getByte(10) & 0xFF);
        assertEquals(0x22, model.getByte(11) & 0xFF);
        assertEquals(0x33, model.getByte(12) & 0xFF);
        assertEquals(10, model.getByte(13) & 0xFF); // сдвинутый байт
    }

    @Test
    public void testPendingWritesGrouping() throws IOException {
        model.openFile(tempFile.getAbsolutePath(), false);
        model.setByte(10, (byte) 0xAA);
        model.setByte(20, (byte) 0xBB);
        model.setByte(30, (byte) 0xCC);
        model.save();
        model.close();
        model.openFile(tempFile.getAbsolutePath(), true);
        assertEquals(0xAA, model.getByte(10) & 0xFF);
        assertEquals(0xBB, model.getByte(20) & 0xFF);
        assertEquals(0xCC, model.getByte(30) & 0xFF);

        model.openFile(tempFile.getAbsolutePath(), false);
        model.setByte(40, (byte) 0x11);
        model.setByte(41, (byte) 0x22);
        model.setByte(42, (byte) 0x33);
        model.save();
        model.close();
        model.openFile(tempFile.getAbsolutePath(), true);
        assertEquals(0x11, model.getByte(40) & 0xFF);
        assertEquals(0x22, model.getByte(41) & 0xFF);
        assertEquals(0x33, model.getByte(42) & 0xFF);
    }
}