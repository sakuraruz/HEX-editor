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

        editMenu.add(deleteZeroItem);

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

    private void deleteSelected() {
        int[] range = hexTable.getSelectedRange();
        if (range == null) {
            JOptionPane.showMessageDialog(mainWindow, "Сначала выделите байты для удаления");
            return;
        }

        int startRow = range[0];
        int startCol = range[1];
        int endRow = range[2];
        int endCol = range[3];

        HexTableModel model = (HexTableModel) hexTable.getModel();
        model.deleteSelected(startRow, startCol, endRow, endCol);
    }
}
