package org.example;

import javax.swing.table.TableCellEditor;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.AbstractCellEditor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.awt.Font;
import java.awt.Component;

public class HexCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JTextField textField;
    private String originalValue;
    private int row, col;
    private HexTableModel model;
    private char pendingChar = '\0';
    private boolean isKeyboardEdit = false;

    public HexCellEditor() {
        textField = new JTextField();
        textField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textField.setHorizontalAlignment(JTextField.CENTER);

        textField.addActionListener(e -> stopCellEditing());

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!isKeyboardEdit) {
                    textField.selectAll();
                }
                // Сбрасываем флаг
                isKeyboardEdit = false;
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                if (!HexUtils.isHexChar(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                    return;
                }

                String currentText = textField.getText();

                if (currentText.length() >= 2 && HexUtils.isHexChar(c)) {
                    textField.setText("");
                }
            }
        });
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) anEvent;
            return mouseEvent.getClickCount() >= 2;
        }
        return true;
    }

    public void setPendingChar(char c) {
        this.pendingChar = c;
    }


    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.row = row;
        this.col = column;
        this.model = (HexTableModel) table.getModel();
        this.originalValue = value.toString();

        if (pendingChar != '\0') {
            textField.setText("");
            pendingChar = '\0';
            isKeyboardEdit = true;
        } else {
            textField.setText(originalValue);
            isKeyboardEdit = false;
        }


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
