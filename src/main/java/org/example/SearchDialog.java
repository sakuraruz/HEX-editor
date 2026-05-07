package org.example;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.SwingWorker;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class SearchDialog extends JDialog {

    private JTextField searchField;
    private JButton findButton;
    private JButton findNextButton;
    private JButton cancelButton;
    private JButton closeButton;
    private JLabel statusLabel;
    private JCheckBox wrapAroundCheckBox;
    private HexTable hexTable;
    private Searcher searcher;
    private long currentPosition = -1;
    private SearchPattern currentPattern;
    private boolean searchInProgress = false;
    private SwingWorker<Void, Void> searchWorker;

    public SearchDialog(JFrame parent, HexTable hexTable, ByteFileModel fileModel) {
        super(parent, "Поиск последовательности байт", true);
        this.hexTable = hexTable;
        this.searcher = new Searcher(fileModel);

        setSize(550, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(5, 5));
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel hintLabel = new JLabel("?? — любой байт | Примеры: 4F A2 1C | 4F ?? 1C | 01 02 03");
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hintPanel.add(hintLabel);
        hintPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        add(hintPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel label = new JLabel("Hex-последовательность (байты через пробел):");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        inputPanel.add(label, BorderLayout.NORTH);

        searchField = new JTextField();
        searchField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        searchField.setColumns(30);
        inputPanel.add(searchField, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapAroundCheckBox = new JCheckBox("Продолжить с начала файла, если не найдено");
        wrapAroundCheckBox.setSelected(true);
        checkBoxPanel.add(wrapAroundCheckBox);
        bottomPanel.add(checkBoxPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        findButton = new JButton("Найти");
        findNextButton = new JButton("Найти далее");
        cancelButton = new JButton("Отмена");
        closeButton = new JButton("Закрыть");

        cancelButton.setEnabled(false);

        findButton.addActionListener(e -> performSearch(false));
        findNextButton.addActionListener(e -> performSearch(true));
        cancelButton.addActionListener(e -> cancelSearch());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(findButton);
        buttonPanel.add(findNextButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(closeButton);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        searchField.addActionListener(e -> performSearch(false));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                searchField.requestFocus();
                searchField.selectAll();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                cancelSearch();
                dispose();
            }
        });
    }

    private void performSearch(boolean findNext) {
        if (searchInProgress) {
            statusLabel.setText("Поиск уже выполняется...");
            return;
        }

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

                startSearch(false);

            } catch (IllegalArgumentException e) {
                statusLabel.setText("Ошибка: " + e.getMessage());
            }
        } else {
            startSearch(true);
        }
    }

    private void startSearch(boolean findNext) {
        if (currentPattern == null) {
            statusLabel.setText("Сначала выполните поиск (кнопка 'Найти')");
            return;
        }

        searchInProgress = true;
        updateButtonsState();

        final long startPos = findNext ? currentPosition + 1 : currentPosition;
        final boolean wrapAround = wrapAroundCheckBox.isSelected();

        statusLabel.setText("Поиск...");

        searchWorker = new SwingWorker<Void, Void>() {
            private long foundPosition = -1;
            private String errorMessage = null;
            private boolean wrapped = false;

            @Override
            protected Void doInBackground() {
                try {
                    long found = searcher.findNext(startPos);

                    if (found == -1 && wrapAround && startPos > 0) {
                        setStatusMessage("Поиск с начала файла...");
                        found = searcher.findNext(0);
                        if (found >= 0) {
                            wrapped = true;
                        }
                    }

                    foundPosition = found;

                } catch (IOException e) {
                    errorMessage = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                searchInProgress = false;
                updateButtonsState();

                if (errorMessage != null) {
                    statusLabel.setText("Ошибка: " + errorMessage);
                    JOptionPane.showMessageDialog(SearchDialog.this,
                            "Ошибка при поиске:\n" + errorMessage,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } else if (foundPosition >= 0) {
                    currentPosition = foundPosition;
                    highlightPosition(foundPosition);

                    String message = String.format("Найдено в позиции 0x%08X (%d)", foundPosition, foundPosition);
                    if (wrapped) {
                        message += " (поиск с начала файла)";
                    }
                    statusLabel.setText(message);

                } else {
                    statusLabel.setText("Ничего не найдено");
                    JOptionPane.showMessageDialog(SearchDialog.this,
                            "Последовательность не найдена в файле",
                            "Результат поиска",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };

        searchWorker.execute();
    }

    private void cancelSearch() {
        if (searchInProgress && searcher != null) {
            searcher.cancel();
            if (searchWorker != null && !searchWorker.isDone()) {
                searchWorker.cancel(true);
            }
            statusLabel.setText("Поиск отменен");
            searchInProgress = false;
            updateButtonsState();
        }
    }

    private void setStatusMessage(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    private void updateButtonsState() {
        SwingUtilities.invokeLater(() -> {
            findButton.setEnabled(!searchInProgress);
            findNextButton.setEnabled(!searchInProgress && currentPattern != null);
            cancelButton.setEnabled(searchInProgress);
            closeButton.setEnabled(!searchInProgress);
            searchField.setEnabled(!searchInProgress);
            wrapAroundCheckBox.setEnabled(!searchInProgress);
        });
    }

    private void highlightPosition(long position) {
        int columns = ((HexTableModel) hexTable.getModel()).getColumns();
        int row = (int) (position / columns);
        int col = (int) (position % columns);

        SwingUtilities.invokeLater(() -> {
            hexTable.setRowSelectionInterval(row, row);
            hexTable.setColumnSelectionInterval(col, col);
            hexTable.scrollRectToVisible(hexTable.getCellRect(row, col, true));
            hexTable.requestFocusInWindow();
        });
    }

    @Override
    public void dispose() {
        cancelSearch();
        super.dispose();
    }
}