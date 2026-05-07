package org.example;

import javax.swing.SwingUtilities;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackgroundFileOperation {
    private static final Logger log = LoggerFactory.getLogger(BackgroundFileOperation.class);

    public static void execute(Component parent, String title, String message,
                               FileOperation operation,
                               Runnable onSuccess,
                               Consumer<Exception> onError) {

        ProgressMonitor progressMonitor = new ProgressMonitor(parent, message, null, 0, 100);
        progressMonitor.setProgress(0);
        progressMonitor.setMillisToDecideToPopup(100);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    operation.execute();
                } catch (Exception e) {
                    throw e;
                }
                return null;
            }

            @Override
            protected void done() {
                progressMonitor.close();
                try {
                    get(); // Проверяем наличие исключения
                    if (onSuccess != null) {
                        SwingUtilities.invokeLater(onSuccess);
                    }
                } catch (Exception e) {
                    log.error("Ошибка при выполнении операции: {}", title, e);
                    if (onError != null) {
                        SwingUtilities.invokeLater(() -> onError.accept(e));
                    } else if (parent != null) {
                        JOptionPane.showMessageDialog(parent,
                                "Ошибка при выполнении операции:\n" + e.getMessage(),
                                title,
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        worker.execute();
    }

    @FunctionalInterface
    public interface FileOperation {
        void execute() throws Exception;
    }
}