package org.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ByteFileModel {

    private RandomAccessFile file;
    private boolean isOpen = false;
    private long fileSize;

    public void openFile(String name) throws IOException {
        if (isOpen) close();
        file = new RandomAccessFile(name, "rw");
        fileSize = file.length();
        isOpen = true;
    }

    public byte getByte(long position) throws IOException {
        if (isOpen) {
            if (position < 0 || position >= fileSize) {
                return 0;
            }
            file.seek(position);
            return file.readByte();
        }
        throw new IOException("Файл не открыт!");
    }

    public void close() throws IOException {
        if (file != null) {
            file.close();
            file = null;
            isOpen = false;
        }
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setByte(long position, byte value) throws IOException {
        if (!isOpen) {
            return;
        }

        if (position >= fileSize) {
            file.seek(fileSize);
            for (long i = fileSize; i < position; i++) {
                file.writeByte(0);
            }
            file.writeByte(value);
            fileSize = position + 1;
        } else {
            file.seek(position);
            file.writeByte(value);
        }
    }
}
