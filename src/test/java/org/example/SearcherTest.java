package org.example;

import org.example.model.ByteFileModel;
import org.example.model.SearchPattern;
import org.example.service.Searcher;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.Assert.assertEquals;

public class SearcherTest {

    private ByteFileModel model;
    private Searcher searcher;
    private File tempFile;

    @Before
    public void setUp() throws IOException {
        tempFile = File.createTempFile("searcher", ".bin");
        byte[] data = new byte[256];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        data[10] = 1;
        data[11] = 2;
        data[12] = 3;
        Files.write(tempFile.toPath(), data);
        model = new ByteFileModel();
        model.openFile(tempFile.getAbsolutePath(), true);
        searcher = new Searcher(model);
    }

    @After
    public void tearDown() throws IOException {
        model.close();
        tempFile.delete();
    }

    @Test
    public void testFindExact() throws IOException {
        SearchPattern pattern = new SearchPattern("01 02 03");
        searcher.setPattern(pattern);
        long pos = searcher.findNext(2);
        assertEquals(10, pos);
        pos = searcher.findNext(11);
        assertEquals(-1, pos);
    }

    @Test
    public void testFindMasked() throws IOException {
        SearchPattern pattern = new SearchPattern("01 ?? 03");
        searcher.setPattern(pattern);
        long pos = searcher.findNext(2);
        assertEquals(10, pos);
    }

    @Test
    public void testNotFound() throws IOException {
        SearchPattern pattern = new SearchPattern("FF FE FD");
        searcher.setPattern(pattern);
        long pos = searcher.findNext(0);
        assertEquals(-1, pos);
    }

    @Test
    public void testCancel() throws IOException {
        SearchPattern pattern = new SearchPattern("01 02 03");
        searcher.setPattern(pattern);
        searcher.cancel();
        long pos = searcher.findNext(0);
        assertEquals(-1, pos);
    }
}