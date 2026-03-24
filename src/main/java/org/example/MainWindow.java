package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class MainWindow extends JFrame {
    private ByteFileModel fileModel;
    private MenuBar menuBar;
    private HexTable hexTable;
    private HexTableModel tableModel;
    private JTable rowHeaderTable;
    private StatusBar statusBar;
    private BlockViewPanel blockViewPanel;

    public MainWindow() {
        super("HEX-редактор");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 700);  // шире для панели блоков

        fileModel = new ByteFileModel();

        try {
            fileModel.openFile("test.bin");
        } catch (IOException e) {
            System.out.println("Не удалось открыть файл: " + e.getMessage());
        }

        tableModel = new HexTableModel(fileModel);
        hexTable = new HexTable(tableModel);

        rowHeaderTable = createRowHeader();

        JLabel cornerLabel = new JLabel("Смещ.", SwingConstants.CENTER);
        cornerLabel.setFont(new Font("Arial", Font.BOLD, 12));
        cornerLabel.setBackground(UIManager.getColor("TableHeader.background"));
        cornerLabel.setOpaque(true);
        cornerLabel.setPreferredSize(new Dimension(80, 25));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(cornerLabel, BorderLayout.NORTH);
        headerPanel.add(rowHeaderTable, BorderLayout.CENTER);

        statusBar = new StatusBar(hexTable);

        blockViewPanel = new BlockViewPanel(hexTable);

        hexTable.addPropertyChangeListener("currentPosition", evt -> {
            Long position = (Long) evt.getNewValue();
            blockViewPanel.updateForPosition(position != null ? position : -1);
        });

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(blockViewPanel);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.setMinimumSize(new Dimension(250, 0));
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel mainPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(hexTable);
        scrollPane.setRowHeaderView(headerPanel);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        menuBar = new MenuBar(this, hexTable);
        setJMenuBar(menuBar);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        tableModel.addTableModelListener(e -> {
            if (rowHeaderTable != null) {
                ((DefaultTableModel) rowHeaderTable.getModel()).fireTableDataChanged();
            }
        });

        setVisible(true);
    }

    private JTable createRowHeader() {
        DefaultTableModel headerModel = new DefaultTableModel() {
            @Override
            public int getRowCount() {
                return hexTable.getRowCount();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getValueAt(int row, int column) {
                return tableModel.getRowHeaderValue(row);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable headerTable = new JTable(headerModel);
        headerTable.setRowHeight(hexTable.getRowHeight());
        headerTable.setShowGrid(false);
        headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        headerTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        headerTable.getColumnModel().getColumn(0).setMinWidth(80);
        headerTable.getColumnModel().getColumn(0).setMaxWidth(80);

        headerTable.setFont(new Font("Monospaced", Font.BOLD, 14));
        headerTable.setBackground(new Color(240, 240, 240));
        headerTable.setForeground(Color.BLACK);
        headerTable.setIntercellSpacing(new Dimension(0, 0));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        center.setBorder(BorderFactory.createEmptyBorder());
        headerTable.getColumnModel().getColumn(0).setCellRenderer(center);

        headerTable.setTableHeader(null);

        return headerTable;
    }

    public void openFile(String path) {
        try {
            fileModel.openFile(path);
            tableModel.fireTableStructureChanged();
            ((DefaultTableModel) rowHeaderTable.getModel()).fireTableDataChanged();
            setTitle("HEX-редактор - " + path);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        }
    }
}