package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainWindow extends JFrame
{
    private ByteFileModel fileModel;
    private MenuBar menuBar;
    private HexTable hexTable;
    private HexTableModel tableModel;

    public MainWindow()
    {
        super("HEX-редактор");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);


        fileModel = new ByteFileModel();

        try {
            fileModel.openFile("test.bin");
            System.out.println("Файл открыт! Размер: " + fileModel.getFileSize() + " байт");
        } catch (IOException e) {
            System.out.println("Не удалось открыть файл: " + e.getMessage());
        }
        tableModel = new HexTableModel(fileModel);
        hexTable = new HexTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(hexTable);
        menuBar = new MenuBar(this, hexTable);
        setJMenuBar(menuBar);
        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    public void openFile(String path) {
        try {
            fileModel.openFile(path);
            tableModel.fireTableStructureChanged();  // ÎÁÍÎÂËßÅÌ ÒÀÁËÈÖÓ!
            setTitle("HEX-редактор - " + path);
            System.out.println("Файл открыт: " + path + " (" + fileModel.getFileSize() + " байт)");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при открытии файла:\n" + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
