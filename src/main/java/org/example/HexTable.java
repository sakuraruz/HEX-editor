package org.example;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import java.io.IOException;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.EventObject;
import java.awt.event.MouseEvent;

public class HexTable extends JTable {

    private HexTableModel model;
    private int currentRow = -1;
    private int currentCol = -1;
    private HexCellEditor customEditor;
    private char pendingCharFromKey = '\0';

    public HexTable(HexTableModel model) {
        super(model);
        this.model = model;
        customEditor = new HexCellEditor();

        setDefaultEditor(Object.class, customEditor);

        setFont(new Font("Monospaced", Font.PLAIN, 14));
        getTableHeader().setReorderingAllowed(false);
        setRowHeight(22);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        setDefaultRenderer(Object.class, centerRenderer);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setCellSelectionEnabled(true);
        setColumnSelectionAllowed(true);
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setSurrendersFocusOnKeystroke(true);

        setDragEnabled(true);

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char keyChar = e.getKeyChar();

                if (HexUtils.isHexChar(keyChar)) {
                    int row = getSelectedRow();
                    int col = getSelectedColumn();

                    if (row != -1 && col != -1) {
                        pendingCharFromKey = keyChar;
                        editCellAt(row, col);
                        requestFocus();
                        e.consume();
                    }
                }
            }
        });

        getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateCurrentCell();
            }
        });

        getColumnModel().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateCurrentCell();
            }
        });
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        if (pendingCharFromKey != '\0') {
            // Передаём символ в редактор
            customEditor.setPendingChar(pendingCharFromKey);
            pendingCharFromKey = '\0';
            return super.editCellAt(row, column, e);
        }

        // Для мыши - проверяем двойной клик
        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            if (me.getClickCount() >= 2) {
                return super.editCellAt(row, column, e);
            }
            return false;
        }

        return super.editCellAt(row, column, e);
    }

    private void updateCurrentCell() {
        int row = getSelectedRow();
        int col = getSelectedColumn();

        if (row != -1 && col != -1 && row < getRowCount() && col < getColumnCount()) {
            currentRow = row;
            currentCol = col;
            firePropertyChange("currentByte", null, getCurrentByteValue());
            firePropertyChange("currentPosition", null, getCurrentPosition());
        }
    }

    public ByteValue getCurrentByteValue() {
        if (currentRow == -1 || currentCol == -1) {
            return null;
        }

        try {
            long position = (long) currentRow * model.getColumnCount() + currentCol;
            if (position >= model.getFileModel().getFileSize()) {
                return null;
            }

            byte b = model.getFileModel().getByte(position);
            return new ByteValue(b);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class ByteValue {
        public final byte value;
        public final int unsigned;
        public final int signed;

        public ByteValue(byte b) {
            this.value = b;
            this.unsigned = b & 0xFF;
            this.signed = (int) b;
        }

        public String getHex() {
            return String.format("%02X", unsigned);
        }

        public String getUnsigned() {
            return String.valueOf(unsigned);
        }

        public String getSigned() {
            return String.valueOf(signed);
        }
    }

    public int[] getSelectedRange() {
        int[] rows = getSelectedRows();
        int[] cols = getSelectedColumns();

        if (rows.length == 0 || cols.length == 0) return null;

        return new int[] {rows[0], cols[0], rows[rows.length-1], cols[cols.length-1]};
    }

    public byte[] getBytesAt(long position, int count) {
        if (position < 0 || position + count > model.getFileModel().getFileSize()) {
            return null;
        }

        byte[] result = new byte[count];
        try {
            for (int i = 0; i < count; i++) {
                result[i] = model.getFileModel().getByte(position + i);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getCurrentPosition() {
        if (currentRow == -1 || currentCol == -1) return -1;
        return (long) currentRow * model.getColumnCount() + currentCol;
    }
}
