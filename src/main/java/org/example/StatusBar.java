package org.example;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class StatusBar extends JPanel implements PropertyChangeListener {

    private JLabel positionLabel;
    private JLabel hexLabel;
    private JLabel unsignedLabel;
    private JLabel signedLabel;
    private HexTable hexTable;

    public StatusBar(HexTable hexTable) {
        this.hexTable = hexTable;
        hexTable.addPropertyChangeListener("currentByte", this);

        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
        setBorder(BorderFactory.createEtchedBorder());

        positionLabel = new JLabel("Позиция: --");
        hexLabel = new JLabel("HEX: --");
        unsignedLabel = new JLabel("Unsigned: --");
        signedLabel = new JLabel("Signed: --");

        add(positionLabel);
        add(hexLabel);
        add(unsignedLabel);
        add(signedLabel);

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
}