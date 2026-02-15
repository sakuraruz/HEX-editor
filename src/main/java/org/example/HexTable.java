package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class HexTable extends JTable {
    public HexTable(HexTableModel model) {
        super(model);

        setFont(new Font("Monospaced", Font.PLAIN, 14));
        getTableHeader().setReorderingAllowed(false);
        setRowHeight(22);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        setDefaultRenderer(Object.class, centerRenderer);

        setDefaultEditor(Object.class, new HexCellEditor());
    }
}
