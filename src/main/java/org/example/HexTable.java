package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class HexTable extends JTable {
    public HexTable(HexTableModel model) {
        super(model);

        setDefaultEditor(Object.class, new HexCellEditor());

        setFont(new Font("Monospaced", Font.PLAIN, 14));
        getTableHeader().setReorderingAllowed(false);
        setRowHeight(22);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        setDefaultRenderer(Object.class, centerRenderer);

        setCellSelectionEnabled(true);
        setColumnSelectionAllowed(true);
        setRowSelectionAllowed(true);
    }

    public int[] getSelectedRange() {
        int startRow = getSelectedRow();
        int endRow = getSelectionModel().getMaxSelectionIndex();
        int startCol = getSelectedColumn();
        int endCol = getColumnModel().getSelectionModel().getMaxSelectionIndex();

        if (startRow == -1 || startCol == -1) return null;

        return new int[] {startRow, startCol, endRow, endCol};
    }
}
