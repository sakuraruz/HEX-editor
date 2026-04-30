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
            System.err.println("Ошибка чтения байта на позиции " + position + ": " + e.getMessage());
            return "??";
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        long position = (long) row * getColumnCount() + col;
        return position < fileModel.getFileSize();
    }

    @Override
    public String getColumnName(int col) {
        return String.valueOf(col);
    }

    public String getRowHeaderValue(int row) {
        long offset = (long) row * columns;
        return String.format("%08X", offset);
    }

    public void setColumns(int newColumns) {
        if (newColumns < 1) newColumns = 1;
        if (newColumns > 64) newColumns = 64;

        this.columns = newColumns;
        fireTableStructureChanged();
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

    public void deleteWithShiftSelected(int startRow, int startCol, int endRow, int endCol) {
        try {
            for (int row = endRow; row >= startRow; row--) {
                long startPos = (long) row * columns + startCol;
                long length = endCol - startCol + 1;

                fileModel.deleteWithShift(startPos, length);
            }

            fireTableStructureChanged();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }

    public void pasteSelected(int startRow, int startCol) {
        if (clipboard == null || clipboard.bytes.length == 0) {
            return;
        }

        long startPos = (long) startRow * columns + startCol;

        try {
            for (int i = 0; i < clipboard.bytes.length; i++) {
                fileModel.setByte(startPos + i, clipboard.bytes[i]);
            }

            fireTableDataChanged();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pasteWithShiftSelected(int startRow, int startCol) {
        if (clipboard == null || clipboard.bytes.length == 0) {
            return;
        }

        long insertPos = (long) startRow * columns + startCol;

        try {
            fileModel.insertWithShift(insertPos, clipboard.bytes);

            fireTableStructureChanged();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void cutSelected(int startRow, int startCol, int endRow, int endCol) {
        copySelected(startRow, startCol, endRow, endCol);
        deleteSelected(startRow, startCol, endRow, endCol);
    }

    public void cutWithShiftSelected(int startRow, int startCol, int endRow, int endCol) {
        copySelected(startRow, startCol, endRow, endCol);
        deleteWithShiftSelected(startRow, startCol, endRow, endCol);
    }
}
