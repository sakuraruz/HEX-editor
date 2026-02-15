package org.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBar extends JMenuBar {
    private MainWindow mainWindow;

    public MenuBar(MainWindow mainWindow){
        this.mainWindow = mainWindow;

        JMenu fileMenu = new JMenu("Файл");
        JMenu viewMenu = new JMenu("Вид");

        JMenuItem openItem = new JMenuItem("Открыть");

        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        JMenuItem saveItem = new JMenuItem("Сохранить");
        JMenuItem exitItem = new JMenuItem("Выход");

        JMenu columnsMenu = new JMenu("Столбцы");
        JMenuItem col8Item = new JMenuItem("8");
        JMenuItem col16Item = new JMenuItem("16");
        JMenuItem col32Item = new JMenuItem("32");
        JMenuItem col64Item = new JMenuItem("64");

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        viewMenu.add(columnsMenu);
        columnsMenu.add(col8Item);
        columnsMenu.add(col16Item);
        columnsMenu.add(col32Item);
        columnsMenu.add(col64Item);

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
