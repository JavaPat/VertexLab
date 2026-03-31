package org.vertex;

import org.vertex.app.EditorApp;
import org.vertex.ui.MainWindow;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EditorApp app = new EditorApp();
            new MainWindow(app);
        });
    }
}