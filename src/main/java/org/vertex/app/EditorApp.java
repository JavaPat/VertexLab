package org.vertex.app;

import org.vertex.model.Model3D;
import org.vertex.renderer.Camera;
import org.vertex.editor.EditorController;

public class EditorApp {

    private final Model3D model;
    private final Camera camera;
    private final EditorController controller;

    public EditorApp() {
        this.model = new Model3D();
        this.camera = new Camera();
        this.controller = new EditorController(model);

        // Default model
        controller.createCube(2);
    }

    public Model3D getModel() {
        return model;
    }

    public Camera getCamera() {
        return camera;
    }

    public EditorController getController() {
        return controller;
    }
}
