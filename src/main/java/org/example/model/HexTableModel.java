package org.example.model;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.example.service.BackgroundFileOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JFrame;

public class HexTableModel extends AbstractTableModel {

    private static final Logger log = LoggerFactory.getLogger(HexTableModel.class);

    private static class CopiedData {
        byte[] bytes;
        int originalColumns;
    }

    private static CopiedData clipboard = null;
    private ByteFileModel fileModel;
    private int columns = 16;
    private JFrame parentFrame;

    public HexTableModel(ByteFileModel fileModel) {
        this.fileModel = fileModel;
    }

    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }

    private void showError(String message, Exception e) {
        log.error("{}: {}", message, e.getMessage(), e);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parentFrame,
                    message + "\n\nДетали: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    private void showWarning(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parentFrame,
                    message,
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
        });
    }

    private void showInfo(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parentFrame,
                    message,
                    "Информация",
                    JOptionPane.INFORMATION_MESSAGE);
        });
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
            log.error("Ошибка чтения байта на позиции {}", position, e);
            return "??";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        try {
            long position = (long) row * columns + col;
            String hex = ((String) value).trim();
            if (hex.isEmpty()) return;

            int intValue = Integer.parseInt(hex, 16);
            byte byteValue = (byte) intValue;
            fileModel.setByte(position, byteValue);
            fireTableCellUpdated(row, col);
        } catch (NumberFormatException e) {
            showWarning("Некорректное шестнадцатеричное значение: " + value +
                    "\nДопустимые символы: 0-9, A-F");
        } catch (IOException e) {
            showError("Ошибка при установке значения в ячейке", e);
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
        BackgroundFileOperation.execute(parentFrame,
                "Удаление",
                "Удаление байтов...",
                () -> {
                    fileModel.save();
                    long endPos = (long) endRow * columns + endCol;
                    for (long startPos = (long) startRow * columns + startCol; startPos <= endPos; startPos += columns) {
                        long length = endCol - startCol + 1;
                        fileModel.deleteWithZero(startPos, length);
                    }
                },
                () -> {
                    fireTableStructureChanged();
                    showInfo("Удаление выполнено успешно");
                },
                (e) -> showError("Ошибка при удалении", e)
        );
    }

    public void deleteWithShiftSelected(int startRow, int startCol, int endRow, int endCol) {

        BackgroundFileOperation.execute(parentFrame,
                "Удаление со сдвигом",
                "Удаление байтов со сдвигом...",
                () -> {
                    fileModel.save();
                    for (int row = endRow; row >= startRow; row--) {
                        long startPos = (long) row * columns + startCol;
                        long length = endCol - startCol + 1;
                        fileModel.deleteWithShift(startPos, length);
                    }
                },
                () -> {
                    fireTableStructureChanged();
                    showInfo("Удаление со сдвигом выполнено успешно");
                },
                (e) -> showError("Ошибка при удалении со сдвигом", e)
        );
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
            showWarning("Буфер обмена пуст. Сначала скопируйте данные.");
            return;
        }

        BackgroundFileOperation.execute(parentFrame,
                "Вставка",
                "Вставка байтов...",
                () -> {
                    long startPos = (long) startRow * columns + startCol;
                    for (int i = 0; i < clipboard.bytes.length; i++) {
                        long pos = startPos + i;
                        if (pos >= fileModel.getFileSize()) {
                            break;
                        }
                        fileModel.setByte(pos, clipboard.bytes[i]);
                    }
                },
                () -> {
                    fireTableDataChanged();
                    showInfo("Вставка выполнена успешно");
                },
                (e) -> showError("Ошибка при вставке", e)
        );
    }

    public void pasteWithShiftSelected(int startRow, int startCol) {
        if (clipboard == null || clipboard.bytes.length == 0) {
            showWarning("Буфер обмена пуст. Сначала скопируйте данные.");
            return;
        }

        BackgroundFileOperation.execute(parentFrame,
                "Вставка со сдвигом",
                "Вставка байтов со сдвигом...",
                () -> {
                    long insertPos = (long) startRow * columns + startCol;
                    fileModel.insertWithShift(insertPos, clipboard.bytes);
                },
                () -> {
                    fireTableStructureChanged();
                    showInfo("Вставка со сдвигом выполнена успешно");
                },
                (e) -> showError("Ошибка при вставке со сдвигом", e)
        );
    }


    public void cutSelected(int startRow, int startCol, int endRow, int endCol) {
        copySelected(startRow, startCol, endRow, endCol);
        deleteSelected(startRow, startCol, endRow, endCol);
    }

    public void cutWithShiftSelected(int startRow, int startCol, int endRow, int endCol) {
        copySelected(startRow, startCol, endRow, endCol);
        deleteWithShiftSelected(startRow, startCol, endRow, endCol);
    }

    public int getColumns() {
        return columns;
    }

    public static byte[] getClipboardBytes() {
        return clipboard.bytes;
    }
}
