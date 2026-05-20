package org.example.service;

import org.example.model.ByteFileModel;
import org.example.model.SearchPattern;

import java.io.IOException;

public class Searcher {

    private ByteFileModel fileModel;
    private SearchPattern pattern;
    private volatile boolean cancelled = false;

    public Searcher(ByteFileModel fileModel) {
        this.fileModel = fileModel;
    }

    public void setPattern(SearchPattern pattern) {
        this.pattern = pattern;
    }

    public void cancel() {
        cancelled = true;
    }

    public long findNext(long startPos) throws IOException {
        if (pattern == null) return -1;

        long fileSize = fileModel.getFileSize();
        int patternLength = pattern.length();

        for (long pos = startPos; pos <= fileSize - patternLength; pos++) {

            if (cancelled) {
                cancelled = false;
                return -1;
            }

            if (matchesAt(pos)) {
                return pos;
            }
        }
        return -1;
    }

    private boolean matchesAt(long position) throws IOException {
        for (int i = 0; i < pattern.length(); i++) {
            Byte expected = pattern.getByteAt(i);

            // Если маска (null) — пропускаем проверку
            if (expected == null) continue;

            byte actual = fileModel.getByte(position + i);
            if (actual != expected) {
                return false;
            }
        }
        return true;
    }
}