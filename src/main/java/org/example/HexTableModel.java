package org.example;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;

public class HexTableModel extends AbstractTableModel {

    private static class CopiedData {
        byte[] bytes;
        int originalColumns;
    }

    private static CopiedData clipboard = null;
    private ByteFileModel fileModel;
    private int columns = 16;

    public HexTableModel(ByteFileModel fileModel) {
        this.fileModel = fileModel;
    }

    @Override
    public int getRowCount() {
        long size = fileModel.getFileSize();
        if (size == 0) return 0;
        return (int) Math.ceil((double) size / columns);
    }

    @Override
    public int getColumnCount() {
        return columns;
    }

    @Override
    public Object getValueAt(int row, int col) {
        long position = (long) row * columns + col;

        try {
            byte b = fileModel.getByte(position);
            return String.format("%02X", (b & 0xFF));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        long position = (long) row * getColumnCount() + col;
        return position < fileModel.getFileSize();
    }

    public ByteFileModel getFileModel() {
        return fileModel;
    }

    public void deleteSelected(int startRow, int startCol, int endRow, int endCol) {

        long endPos = (long) endRow * columns + endCol;

        for (long startPos = (long) startRow * columns + startCol; startPos <= endPos; startPos += columns) {
            long length = endCol - startCol + 1;
            try {
                fileModel.deleteWithZero(startPos, length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fireTableStructureChanged();

    }

    public void copySelected(int startRow, int startCol, int endRow, int endCol) {
        int width = endCol - startCol + 1;
        int height = endRow - startRow + 1;

        byte[] bytes = new byte[width * height];
        int index = 0;

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                long position = (long) row * columns + col;
                try {
                    bytes[index++] = fileModel.getByte(position);
                } catch (IOException e) {
                    bytes[index++] = 0;
                }
            }
        }

        clipboard = new CopiedData();
        clipboard.bytes = bytes;
        clipboard.originalColumns = width;

        System.out.println("Скопировано " + bytes.length + " байт");
    }

    public void pasteSelected(int startRow, int startCol) {
        if (clipboard == null || clipboard.bytes.length == 0) {
            System.out.println("Буфер пуст.");
            return;
        }

        long startPos = (long) startRow * columns + startCol;

        int countRows = clipboard.bytes.length / clipboard.originalColumns;
        int pos = 0;
        try {
            for (int i = 0; i < countRows; i++) {
                for (int j = 0; j < clipboard.originalColumns; j++) {
                    fileModel.setByte(startPos + j + i * columns, clipboard.bytes[pos++]);
                }
            }

            fireTableDataChanged();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void cutSelected(int startRow, int startCol, int endRow, int endCol) {
        copySelected(startRow, startCol, endRow, endCol);
        deleteSelected(startRow, startCol, endRow, endCol);
    }
}
