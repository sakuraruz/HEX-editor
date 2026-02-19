package org.example;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class HexCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JTextField textField;
    private String originalValue;
    private int row, col;
    private HexTableModel model;

    public HexCellEditor() {
        textField = new JTextField();
        textField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textField.setHorizontalAlignment(JTextField.CENTER);

        textField.addActionListener(e -> stopCellEditing());

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                if (!isHexChar(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }

                if (textField.getText().length() >= 2 &&
                        c != KeyEvent.VK_BACK_SPACE &&
                        c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });
    }

    private boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'A' && c <= 'F') ||
                (c >= 'a' && c <= 'f');
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.row = row;
        this.col = column;
        this.model = (HexTableModel) table.getModel();
        this.originalValue = value.toString();
        textField.setText(originalValue);
        textField.selectAll();

        return textField;
    }

    @Override
    public Object getCellEditorValue() {
        return textField.getText().toUpperCase();
    }

    @Override
    public boolean stopCellEditing() {
        String input = textField.getText().trim();

        if (input.isEmpty()) {
            input = "00";
        } else if (input.length() == 1) {
            input = "0" + input;
        } else if (input.length() > 2) {
            input = input.substring(0, 2);
        }

        try {
            Integer.parseInt(input, 16);
        } catch (NumberFormatException e) {
            textField.setText(originalValue);
            return super.stopCellEditing();
        }

        textField.setText(input.toUpperCase());

        //сохранение в файл
        try {
            long position = (long) row * model.getColumnCount() + col;
            byte value = (byte) Integer.parseInt(input, 16);
            model.getFileModel().setByte(position, value);
            model.fireTableCellUpdated(row, col);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.stopCellEditing();
    }
}
