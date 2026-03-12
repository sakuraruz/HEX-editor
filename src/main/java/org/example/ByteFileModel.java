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

    //удаление нулями
    public void deleteWithZero(long startPos, long length) throws IOException {
        if (startPos < 0 || startPos >= fileSize || length < 0) {
            return;
        }
        file.seek(startPos);
        long endPosition = Math.min(startPos + length ,fileSize);
        for (long i = startPos; i < endPosition; i++) {
            file.writeByte(0);
        }
    }

    //удаление со сдвигом
    public void deleteWithShift(long startPos, long length) throws IOException {
        if (!isOpen || startPos < 0 || startPos >= fileSize || length <= 0) {
            return;
        }

        long endPos = Math.min(startPos + length, fileSize);
        long shiftLength = endPos - startPos;
        long remainingBytes = fileSize - endPos;

        if (remainingBytes > 0) {
            byte[] buffer = new byte[(int) remainingBytes];
            file.seek(endPos);
            file.readFully(buffer);

            file.seek(startPos);
            file.write(buffer);
        }

        fileSize -= shiftLength;
        file.setLength(fileSize);
    }

    public void insertWithShift(long insertPos, byte[] data) throws IOException {
        if (!isOpen || data == null || data.length == 0) {
            return;
        }

        if (insertPos > fileSize) {
            insertPos = fileSize;
        }
        if (insertPos < 0) {
            insertPos = 0;
        }

        long bytesToShift = fileSize - insertPos;

        if (bytesToShift > 0) {
            byte[] buffer = new byte[(int) bytesToShift];
            file.seek(insertPos);
            file.readFully(buffer);

            file.seek(insertPos);

            file.write(data);

            file.write(buffer);
        } else {
            file.seek(fileSize);
            file.write(data);
        }
        fileSize += data.length;
    }
}
