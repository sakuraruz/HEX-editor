package org.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBar extends JMenuBar {
    private MainWindow mainWindow;
    private HexTable hexTable;

    public MenuBar(MainWindow mainWindow, HexTable hexTable){
        this.mainWindow = mainWindow;
        this.hexTable = hexTable;

        JMenu fileMenu = new JMenu("Файл");

        JMenuItem openItem = new JMenuItem("Открыть");

        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        JMenu editMenu = new JMenu("Правка");

        JMenuItem deleteZeroItem = new JMenuItem("Удалить");
        deleteZeroItem.addActionListener(e -> deleteSelected());
        JMenuItem copyItem = new JMenuItem("Копировать");
        copyItem.addActionListener(e -> copySelected());
        JMenuItem pasteItem = new JMenuItem("Вставить");
        pasteItem.addActionListener(e -> pasteSelected());
        JMenuItem cutItem = new JMenuItem("Вырезать");
        cutItem.addActionListener(e -> cutSelected());
        JMenuItem deleteShiftItem = new JMenuItem("Удалить со сдвигом");
        deleteShiftItem.addActionListener(e -> deleteWithShiftSelected());
        JMenuItem cutShiftItem = new JMenuItem("Вырезать со сдвигом");
        cutShiftItem.addActionListener(e -> cutWithShiftSelected());

        editMenu.add(deleteZeroItem);
        editMenu.add(deleteShiftItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.add(cutItem);
        editMenu.add(cutShiftItem);
        add(editMenu);



        JMenuItem exitItem = new JMenuItem("Выход");


        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        add(fileMenu);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл для просмотра");

        int result = fileChooser.showOpenDialog(mainWindow);

        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            mainWindow.openFile(path);
        }
    }

    private void operateOnSelected(SelectedBlockOperation operation) {
        int[] range = hexTable.getSelectedRange();
        if (range == null) {
            JOptionPane.showMessageDialog(mainWindow, "Сначала выделите байты");
            return;
        }

        int startRow = range[0];
        int startCol = range[1];
        int endRow = range[2];
        int endCol = range[3];

        if (hexTable.isEditing()) {
            hexTable.getCellEditor().stopCellEditing();
        }

        operation.execute(startRow, startCol, endRow, endCol);
    }

    private void deleteSelected() {
        operateOnSelected((startRow, startCol, endRow, endCol) -> {
            HexTableModel model = (HexTableModel) hexTable.getModel();
            model.deleteSelected(startRow, startCol, endRow, endCol);
        });
    }

    private void copySelected() {
        operateOnSelected((startRow, startCol, endRow, endCol) -> {
            HexTableModel model = (HexTableModel) hexTable.getModel();
            model.copySelected(startRow, startCol, endRow, endCol);
        });
    }

    private void pasteSelected() {
        operateOnSelected((startRow, startCol, endRow, endCol) -> {
            HexTableModel model = (HexTableModel) hexTable.getModel();
            model.pasteSelected(startRow, startCol);
        });
    }

    private void cutSelected() {
        operateOnSelected((startRow, startCol, endRow, endCol) -> {
            HexTableModel model = (HexTableModel) hexTable.getModel();
            model.cutSelected(startRow, startCol, endRow, endCol);
        });
    }

    private void cutWithShiftSelected() {
        operateOnSelected((startRow, startCol, endRow, endCol) -> {
            HexTableModel model = (HexTableModel) hexTable.getModel();
            model.cutWithShiftSelected(startRow, startCol, endRow, endCol);
        });
    }

    private void deleteWithShiftSelected() {
        operateOnSelected((startRow, startCol, endRow, endCol) -> {
            HexTableModel model = (HexTableModel) hexTable.getModel();
            model.deleteWithShiftSelected(startRow, startCol, endRow, endCol);
        });
    }
}
