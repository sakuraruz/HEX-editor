package org.example;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.Color;

public class StatusBar extends JPanel implements PropertyChangeListener {

    private JLabel positionLabel;
    private JLabel hexLabel;
    private JLabel unsignedLabel;
    private JLabel signedLabel;
    private HexTable hexTable;
    private JLabel modeLabel;

    public StatusBar(HexTable hexTable) {
        this.hexTable = hexTable;
        hexTable.addPropertyChangeListener("currentByte", this);

        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
        setBorder(BorderFactory.createEtchedBorder());

        positionLabel = new JLabel("Позиция: --");
        hexLabel = new JLabel("HEX: --");
        unsignedLabel = new JLabel("Unsigned: --");
        signedLabel = new JLabel("Signed: --");
        modeLabel = new JLabel("Режим: --");

        add(positionLabel);
        add(hexLabel);
        add(unsignedLabel);
        add(signedLabel);
        add(modeLabel);

        updateLabels(null);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        HexTable.ByteValue value = (HexTable.ByteValue) evt.getNewValue();
        updateLabels(value);
    }

    private void updateLabels(HexTable.ByteValue value) {
        if (value == null) {
            positionLabel.setText("Позиция: --");
            hexLabel.setText("HEX: --");
            unsignedLabel.setText("Unsigned: --");
            signedLabel.setText("Signed: --");
        } else {
            int row = hexTable.getSelectedRow();
            int col = hexTable.getSelectedColumn();
            long position = (long) row * ((HexTableModel) hexTable.getModel()).getColumnCount() + col;

            positionLabel.setText(String.format("Позиция: 0x%08X (%d)", position, position));
            hexLabel.setText("HEX: " + value.getHex());
            unsignedLabel.setText("Unsigned: " + value.getUnsigned());
            signedLabel.setText("Signed: " + value.getSigned());
        }
    }

    public void updateMode(boolean isReadOnly) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (isReadOnly) {
                modeLabel.setText("Режим: ТОЛЬКО ЧТЕНИЕ");
                modeLabel.setForeground(Color.RED);
            } else {
                modeLabel.setText("Режим: РЕДАКТИРОВАНИЕ");
                modeLabel.setForeground(new Color(0, 150, 0)); // Темно-зеленый
            }
        });
    }
}