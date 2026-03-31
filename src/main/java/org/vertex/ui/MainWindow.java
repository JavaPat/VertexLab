package org.vertex.ui;

import org.vertex.app.EditorApp;
import org.vertex.model.Model3D;
import org.vertex.renderer.Camera;
import org.vertex.io.ModelIO;
import org.vertex.ui.HelpPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainWindow extends JFrame {

    private final JButton saveButton = new JButton("Save");
    private final JButton loadButton = new JButton("Load");
    private final JPanel topBar = new JPanel();
    JButton cubeButton = new JButton("New Cube");

    public MainWindow(EditorApp app) {
        setTitle("VertexLab");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Model3D model = app.getModel();
        Camera camera = app.getCamera();

        ViewportCanvas canvas = new ViewportCanvas(model, camera);
        setLayout(new BorderLayout());

        // Load button
        loadButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("VertexLab Model (*.json)", "json"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Model3D loaded = ModelIO.load(chooser.getSelectedFile());

                    model.clear();
                    model.vertices.addAll(loaded.vertices);
                    model.faces.addAll(loaded.faces);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Save button
        saveButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("VertexLab Model (*.json)", "json"));

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();

                    if (!file.getName().endsWith(".json")) {
                        file = new File(file.getAbsolutePath() + ".json");
                    }

                    ModelIO.save(model, file);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        //generate a cube.
        cubeButton.addActionListener(e -> {
            model.clear();
            new org.vertex.editor.EditorController(model).createCube(2);
        });

        topBar.add(saveButton);
        topBar.add(loadButton);
        topBar.add(cubeButton);

        add(topBar, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(new HelpPanel(), BorderLayout.EAST);
        setLocationRelativeTo(null);
        setVisible(true);

        new Timer(16, e -> canvas.repaint()).start();
    }
}