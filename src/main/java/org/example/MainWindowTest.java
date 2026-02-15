package org.example;

import javax.swing.*;

public class MainWindowTest {

    public static void main(String[] args)
    {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(() -> new MainWindow());
    }
}
