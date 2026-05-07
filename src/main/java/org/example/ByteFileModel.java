package org.example;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;
import java.util.TreeMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteFileModel {

    private static final Logger log = LoggerFactory.getLogger(ByteFileModel.class);

    private RandomAccessFile file;
    private boolean isOpen = false;
    private long fileSize;
    private byte[] buffer;
    private long bufferStartPos;
    private int bufferValidBytes;
    private static final int BUFFER_SIZE = 8192;

    private File currentFile;
    private boolean modified = false;
    private TreeMap<Long, Byte> pendingWrites = new TreeMap<>();
    private static final int MAX_PENDING_WRITES = 10000;
    private File tempFileForSaveAs = null;

    public void openFile(String name) throws IOException {
        log.info("Открытие файла: {}", name);
        if (isOpen) close();
        this.currentFile = new File(name);
        file = new RandomAccessFile(name, "rw");
        fileSize = file.length();
        isOpen = true;
        buffer = new byte[BUFFER_SIZE];
        bufferStartPos = -1;
        bufferValidBytes = 0;
        modified = false;
        pendingWrites.clear();
        log.debug("Файл открыт. Размер: {} байт", fileSize);
    }

    public byte getByte(long position) throws IOException {
        if (pendingWrites.containsKey(position)) {
            log.trace("Возвращаем измененный байт на позиции {}", position);
            return pendingWrites.get(position);
        }

        if (isOpen) {
            if (position < 0 || position >= fileSize) {
                return 0;
            }

            if (position >= bufferStartPos && position < bufferStartPos + bufferValidBytes) {
                int offset = (int)(position - bufferStartPos);
                return buffer[offset];
            }

            bufferStartPos = (position / BUFFER_SIZE) * BUFFER_SIZE;

            file.seek(bufferStartPos);
            bufferValidBytes = file.read(buffer, 0, BUFFER_SIZE);
            if (bufferValidBytes < 0) {
                bufferValidBytes = 0;
                log.warn("Не удалось прочитать данные с позиции {}", bufferStartPos);
                return 0;
            }

            long bytesRemaining = fileSize - bufferStartPos;
            if (bufferValidBytes > bytesRemaining) {
                bufferValidBytes = (int) bytesRemaining;
            }

            int offset = (int)(position - bufferStartPos);
            return buffer[offset];
        }
        log.error("Попытка чтения из неоткрытого файла");
        throw new IOException("Файл не открыт!");
    }

    public void close() throws IOException {
        if (modified && file != null) {
            save();
        }

        if (file != null) {
            file.close();
            file = null;
        }
        isOpen = false;
        buffer = null;
        pendingWrites.clear();

        if (tempFileForSaveAs != null && tempFileForSaveAs.exists()) {
            tempFileForSaveAs.delete();
        }
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setByte(long position, byte value) throws IOException {
        if (!isOpen) {
            return;
        }

        pendingWrites.put(position, value);
        modified = true;

        if (pendingWrites.size() > MAX_PENDING_WRITES) {
            save();
        }

        if (position >= bufferStartPos && position < bufferStartPos + bufferValidBytes) {
            int offset = (int)(position - bufferStartPos);
            buffer[offset] = value;
        }
    }

    public void save() throws IOException {
        if (!modified || !isOpen || pendingWrites.isEmpty()) {
            return;
        }

        long blockStart = -1;
        long blockEnd = -1;

        for (Map.Entry<Long, Byte> entry : pendingWrites.entrySet()) {
            long pos = entry.getKey();

            if (blockStart == -1) {
                blockStart = pos;
                blockEnd = pos;
            } else if (pos == blockEnd + 1) {
                blockEnd = pos;
            } else {
                writeBlock(blockStart, blockEnd);
                blockStart = pos;
                blockEnd = pos;
            }
        }

        if (blockStart != -1) {
            writeBlock(blockStart, blockEnd);
        }

        pendingWrites.clear();
        modified = false;
    }

    private void writeBlock(long start, long end) throws IOException {
        if (start > end) return;

        int length = (int)(end - start + 1);
        byte[] blockData = new byte[length];

        for (long i = start; i <= end; i++) {
            blockData[(int)(i - start)] = pendingWrites.get(i);
        }

        if (start >= fileSize) {
            file.setLength(start + length);
            file.seek(start);
            file.write(blockData);
            fileSize = start + length;
        } else {
            file.seek(start);
            file.write(blockData);
        }
    }

    public void saveAs(String newFileName) throws IOException {
        save();

        try (RandomAccessFile sourceFile = new RandomAccessFile(currentFile, "r");
             RandomAccessFile destFile = new RandomAccessFile(newFileName, "rw")) {

            final long BUFFER_SIZE = 1024 * 1024; // 1MB буфер
            byte[] buffer = new byte[(int)BUFFER_SIZE];
            long bytesRead = 0;

            while (bytesRead < fileSize) {
                long remaining = fileSize - bytesRead;
                int toRead = (int)Math.min(BUFFER_SIZE, remaining);

                sourceFile.seek(bytesRead);
                int read = sourceFile.read(buffer, 0, toRead);
                if (read <= 0) break;

                destFile.seek(bytesRead);
                destFile.write(buffer, 0, read);

                bytesRead += read;
            }
        }

        close();
        openFile(newFileName);
    }

    //удаление нулями
    public void deleteWithZero(long startPos, long length) throws IOException {
        if (startPos < 0 || startPos >= fileSize || length < 0) {
            return;
        }

        save();

        bufferStartPos = -1;
        bufferValidBytes = 0;

        file.seek(startPos);
        long endPosition = Math.min(startPos + length ,fileSize);
        for (long i = startPos; i < endPosition; i++) {
            file.writeByte(0);
            pendingWrites.put(i, (byte)0);
        }
        modified = true;
    }

    //удаление со сдвигом
    public void deleteWithShift(long startPos, long length) throws IOException {
        if (!isOpen || startPos < 0 || startPos >= fileSize || length <= 0) {
            return;
        }

        save();

        bufferStartPos = -1;
        bufferValidBytes = 0;

        long endPos = Math.min(startPos + length, fileSize);
        long shiftLength = endPos - startPos;
        long remainingBytes = fileSize - endPos;

        if (remainingBytes > 0) {
            final int BLOCK_SIZE = 1024 * 1024;
            byte[] tmpBuffer = new byte[BLOCK_SIZE];

            long bytesProcessed = 0;

            while (bytesProcessed < remainingBytes) {
                int toRead = (int) Math.min(BLOCK_SIZE, remainingBytes - bytesProcessed);

                file.seek(endPos + bytesProcessed);
                int actuallyRead = file.read(tmpBuffer, 0, toRead);
                if (actuallyRead <= 0) break;

                file.seek(startPos + bytesProcessed);
                file.write(tmpBuffer, 0, actuallyRead);

                bytesProcessed += actuallyRead;
            }
        }

        fileSize -= shiftLength;
        file.setLength(fileSize);
        modified = true;
        pendingWrites.clear();
    }

    public void insertWithShift(long insertPos, byte[] data) throws IOException {
        if (!isOpen || data == null || data.length == 0) {
            return;
        }

        save();

        bufferStartPos = -1;
        bufferValidBytes = 0;

        if (insertPos > fileSize) insertPos = fileSize;
        if (insertPos < 0) insertPos = 0;

        long bytesToShift = fileSize - insertPos;

        fileSize += data.length;
        file.setLength(fileSize);
        final int BLOCK_SIZE = 1024 * 1024;
        byte[] tmpBuffer = new byte[BLOCK_SIZE];

        long bytesMoved = 0;

        while (bytesMoved < bytesToShift) {
            int toMove = (int) Math.min(BLOCK_SIZE, bytesToShift - bytesMoved);

            long readPos = insertPos + (bytesToShift - bytesMoved - toMove);

            file.seek(readPos);
            int actuallyRead = file.read(tmpBuffer, 0, toMove);
            if (actuallyRead <= 0) break;

            file.seek(readPos + data.length);
            file.write(tmpBuffer, 0, actuallyRead);

            bytesMoved += actuallyRead;
        }
        file.seek(insertPos);
        file.write(data);

        modified = true;
        pendingWrites.clear();
    }

    public boolean isModified() {
        return modified;
    }

    public File getCurrentFile() {
        return currentFile;
    }
}
