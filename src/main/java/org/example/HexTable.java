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

        setDragEnabled(true);
    }

    public int[] getSelectedRange() {
        int[] rows = getSelectedRows();
        int[] cols = getSelectedColumns();

        if (rows.length == 0 || cols.length == 0) return null;

        return new int[] {rows[0], cols[0], rows[rows.length-1], cols[cols.length-1]};
    }
}
