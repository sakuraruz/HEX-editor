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
        JMenu viewMenu = new JMenu("Вид");

        JMenuItem openItem = new JMenuItem("Открыть");

        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        JMenuItem exitItem = new JMenuItem("Выход");


        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        add(fileMenu);
        add(viewMenu);
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
}
