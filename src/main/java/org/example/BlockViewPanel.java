package org.example;


import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Font;

public class BlockViewPanel extends JPanel {

    private HexTable hexTable;
    private JLabel[] labels;

    public BlockViewPanel(HexTable hexTable) {
        this.hexTable = hexTable;

        setLayout(new GridLayout(0, 1, 5, 3));
        setBorder(BorderFactory.createTitledBorder("Просмотр блоков"));

        labels = new JLabel[8];
        String[] names = {
                "2 байта (signed):", "2 байта (unsigned):",
                "4 байта (signed):", "4 байта (unsigned):", "4 байта (float):",
                "8 байт (signed):", "8 байт (unsigned):", "8 байт (double):"
        };

        for (int i = 0; i < names.length; i++) {
            JPanel panel = new JPanel(new BorderLayout(5, 0));
            JLabel nameLabel = new JLabel(names[i]);
            nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

            labels[i] = new JLabel("--");
            labels[i].setFont(new Font("Monospaced", Font.PLAIN, 12));
            labels[i].setHorizontalAlignment(SwingConstants.RIGHT);

            panel.add(nameLabel, BorderLayout.WEST);
            panel.add(labels[i], BorderLayout.CENTER);
            add(panel);
        }
    }

    public void updateForPosition(long position) {
        if (position < 0) {
            clearLabels();
            return;
        }

        updateBlock(position, 2);
        updateBlock(position, 4);
        updateBlock(position, 8);
    }

    private void updateBlock(long position, int size) {
        byte[] bytes = hexTable.getBytesAt(position, size);
        if (bytes == null || bytes.length < size) {
            clearBlock(size);
            return;
        }

        ByteInterpreter interpreter = new ByteInterpreter(bytes);

        switch (size) {
            case 2:
                labels[0].setText(String.valueOf(interpreter.getInt16()));
                labels[1].setText(String.valueOf(interpreter.getUInt16()));
                break;
            case 4:
                labels[2].setText(String.valueOf(interpreter.getInt32()));
                labels[3].setText(String.valueOf(interpreter.getUInt32()));

                float f = interpreter.getFloat32();
                if (Math.abs(f) < 0.0001 && f != 0) {
                    labels[4].setText(String.format("%.6e", f));
                } else if (Math.abs(f) > 1e7) {
                    labels[4].setText(String.format("%.6e", f));
                } else {
                    labels[4].setText(String.format("%.6f", f));
                }
                break;

            case 8:
                labels[5].setText(String.valueOf(interpreter.getInt64()));
                labels[6].setText(interpreter.getUInt64().toString());

                double d = interpreter.getFloat64();
                if (Math.abs(d) < 0.0001 && d != 0) {
                    labels[7].setText(String.format("%.6e", d));
                } else if (Math.abs(d) > 1e7) {
                    labels[7].setText(String.format("%.6e", d));
                } else {
                    labels[7].setText(String.format("%.6f", d));
                }
                break;
        }
    }

    private void clearLabels() {
        for (int i = 0; i < labels.length; i++) {
            labels[i].setText("--");
        }
    }

    private void clearBlock(int size) {
        switch (size) {
            case 2:
                labels[0].setText("--");
                labels[1].setText("--");
                break;
            case 4:
                labels[2].setText("--");
                labels[3].setText("--");
                labels[4].setText("--");
                break;
            case 8:
                labels[5].setText("--");
                labels[6].setText("--");
                labels[7].setText("--");
                break;
        }
    }
}