package org.example;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;

public class HexTableModel extends AbstractTableModel {
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
        long startPos = (long) startRow * columns + startCol;
        long endPos = (long) endRow * columns + endCol;
        long length = endPos - startPos;

        try {
            fileModel.deleteWithZero(startPos, length);
            fireTableStructureChanged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
