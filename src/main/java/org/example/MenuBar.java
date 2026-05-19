package org.example;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MenuBar extends JMenuBar {
    private MainWindow mainWindow;
    private HexTable hexTable;
    private SearchDialog searchDialog;

    private JMenuItem saveItem;
    private JMenuItem deleteZeroItem;
    private JMenuItem deleteShiftItem;
    private JMenuItem pasteItem;
    private JMenuItem pasteWithShiftItem;
    private JMenuItem cutItem;
    private JMenuItem cutShiftItem;
    private JMenuItem enableEditItem;

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

        saveItem = new JMenuItem("Сохранить");
        saveItem.addActionListener(e -> saveFile());

        JMenuItem saveAsItem = new JMenuItem("Сохранить как");
        saveAsItem.addActionListener(e -> saveFileAs());

        JMenu viewMenu = new JMenu("Вид");
        JMenu columnsMenu = new JMenu("Столбцы");

        int[] columnOptions = {8, 16, 32, 64};
        for (int cols : columnOptions) {
            JMenuItem item = new JMenuItem(cols + " столбцов");
            item.addActionListener(e -> {
                HexTableModel model = (HexTableModel) hexTable.getModel();
                model.setColumns(cols);
            });
            columnsMenu.add(item);
        }
        viewMenu.add(columnsMenu);

        JMenu editMenu = new JMenu("Правка");

        deleteZeroItem = new JMenuItem("Удалить");
        deleteZeroItem.addActionListener(e -> deleteSelected());
        JMenuItem copyItem = new JMenuItem("Копировать");
        copyItem.addActionListener(e -> copySelected());
        pasteItem = new JMenuItem("Вставить");
        pasteItem.addActionListener(e -> pasteSelected());
        pasteWithShiftItem = new JMenuItem("Вставить со сдвигом");
        pasteWithShiftItem.addActionListener(e -> pasteWithShiftSelected());
        cutItem = new JMenuItem("Вырезать");
        cutItem.addActionListener(e -> cutSelected());
        deleteShiftItem = new JMenuItem("Удалить со сдвигом");
        deleteShiftItem.addActionListener(e -> deleteWithShiftSelected());
        cutShiftItem = new JMenuItem("Вырезать со сдвигом");
        cutShiftItem.addActionListener(e -> cutWithShiftSelected());
        enableEditItem = new JMenuItem("Включить редактирование");
        enableEditItem.addActionListener(e -> mainWindow.enableEditing());

        JMenu searchMenu = new JMenu("Поиск");

        JMenuItem findItem = new JMenuItem("Найти");
        findItem.addActionListener(e -> showSearchDialog());

        searchMenu.add(findItem);

        editMenu.add(deleteZeroItem);
        editMenu.add(deleteShiftItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.add(pasteWithShiftItem);
        editMenu.add(cutItem);
        editMenu.add(cutShiftItem);
        editMenu.addSeparator();
        editMenu.add(enableEditItem);
        add(editMenu);
        add(viewMenu);
        add(searchMenu);



        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> mainWindow.closeApplication());


        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        add(fileMenu);
        setEditingEnabled(false);
    }

    public void setEditingEnabled(boolean enabled) {
        if (saveItem != null) saveItem.setEnabled(enabled);
        if (deleteZeroItem != null) deleteZeroItem.setEnabled(enabled);
        if (deleteShiftItem != null) deleteShiftItem.setEnabled(enabled);
        if (pasteItem != null) pasteItem.setEnabled(enabled);
        if (pasteWithShiftItem != null) pasteWithShiftItem.setEnabled(enabled);
        if (cutItem != null) cutItem.setEnabled(enabled);
        if (cutShiftItem != null) cutShiftItem.setEnabled(enabled);
        if (enableEditItem != null) enableEditItem.setVisible(!enabled);
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

    private void pasteWithShiftSelected() {
        operateOnSelected((startRow, startCol, endRow, endCol) -> {
            HexTableModel model = (HexTableModel) hexTable.getModel();
            model.pasteWithShiftSelected(startRow, startCol);
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

    private void showSearchDialog() {
        if (searchDialog == null || !searchDialog.isVisible()) {
            searchDialog = new SearchDialog(mainWindow, hexTable,
                    ((HexTableModel) hexTable.getModel()).getFileModel());
            searchDialog.setVisible(true);
        } else {
            searchDialog.toFront();
        }
    }

    private void saveFile() {
        try {
            HexTableModel model = (HexTableModel) hexTable.getModel();
            ByteFileModel fileModel = model.getFileModel();

            if (fileModel != null) {
                fileModel.save();
                JOptionPane.showMessageDialog(mainWindow,
                        "Файл успешно сохранен",
                        "Сохранение",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Ошибка при сохранении: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFileAs() {
        try {
            HexTableModel model = (HexTableModel) hexTable.getModel();
            ByteFileModel fileModel = model.getFileModel();

            if (fileModel == null) return;

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить файл как");
            fileChooser.setSelectedFile(fileModel.getCurrentFile());

            int result = fileChooser.showSaveDialog(mainWindow);

            if (result == JFileChooser.APPROVE_OPTION) {
                String newPath = fileChooser.getSelectedFile().getAbsolutePath();
                fileModel.saveAs(newPath);
                mainWindow.updateTitle(newPath);
                JOptionPane.showMessageDialog(mainWindow,
                        "Файл сохранен как: " + newPath,
                        "Сохранение",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Ошибка при сохранении: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
