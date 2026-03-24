package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class SearchDialog extends JDialog {

    private JTextField searchField;
    private JButton findButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private HexTable hexTable;
    private Searcher searcher;
    private long currentPosition = -1;
    private SearchPattern currentPattern;

    public SearchDialog(JFrame parent, HexTable hexTable, ByteFileModel fileModel) {
        super(parent, "Поиск последовательности байт", true);
        this.hexTable = hexTable;
        this.searcher = new Searcher(fileModel);

        setSize(500, 220);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(5, 5));

        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hintPanel.add(new JLabel("?? — любой байт"));
        hintPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        add(hintPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel label = new JLabel("Hex-последовательность (например: 4F A2 1C  или  4F ?? 1C):");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        inputPanel.add(label, BorderLayout.NORTH);

        searchField = new JTextField();
        searchField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        searchField.setColumns(30);
        inputPanel.add(searchField, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        findButton = new JButton("Найти");
        JButton findNextButton = new JButton("Найти далее");
        cancelButton = new JButton("Закрыть");

        findButton.addActionListener(e -> performSearch(false));
        findNextButton.addActionListener(e -> performSearch(true));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(findButton);
        buttonPanel.add(findNextButton);
        buttonPanel.add(cancelButton);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        searchField.addActionListener(e -> performSearch(false));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                searchField.requestFocus();
                searchField.selectAll();
            }
        });
    }

    private void performSearch(boolean findNext) {
        String input = searchField.getText().trim();

        if (!findNext || input.isEmpty()) {
            if (input.isEmpty()) {
                statusLabel.setText("Введите последовательность байт");
                return;
            }

            try {
                currentPattern = new SearchPattern(input);
                searcher.setPattern(currentPattern);

                long startPos = hexTable.getCurrentPosition();
                if (startPos < 0) startPos = 0;
                currentPosition = startPos;

                findAndHighlight();

            } catch (IllegalArgumentException e) {
                statusLabel.setText("Ошибка: " + e.getMessage());
            }
        } else {
            findAndHighlight();
        }
    }

    private void findAndHighlight() {
        if (currentPattern == null) {
            statusLabel.setText("Сначала выполните поиск");
            return;
        }

        try {
            long startPos = currentPosition + 1;

            long found = searcher.findNext(startPos);
            if (found < 0) {
                found = searcher.findNext(0);
                if (found >= 0) {
                    statusLabel.setText("Достигнут конец файла. Найдено с начала в позиции 0x" +
                            Long.toHexString(found));
                }
            }

            if (found >= 0) {
                currentPosition = found;
                highlightPosition(found);

                statusLabel.setText("Найдено в позиции 0x" + Long.toHexString(found) + " (" + found + ")");
            } else {
                statusLabel.setText("Ничего не найдено");
            }

        } catch (IOException e) {
            statusLabel.setText("Ошибка при поиске: " + e.getMessage());
        }
    }

    private void highlightPosition(long position) {
        int columns = hexTable.getModel().getColumnCount();
        int row = (int) (position / columns);
        int col = (int) (position % columns);

        hexTable.setRowSelectionInterval(row, row);
        hexTable.setColumnSelectionInterval(col, col);
        hexTable.scrollRectToVisible(hexTable.getCellRect(row, col, true));
    }
}