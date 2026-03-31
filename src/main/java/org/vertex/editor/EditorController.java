package org.vertex.editor;

import org.vertex.model.Model3D;

public class EditorController {

    private Model3D model;

    public EditorController(Model3D model) {
        this.model = model;
    }

    // Example: create cube
    public void createCube(float size) {
        model.clear();

        float s = size / 2;

        model.addVertex(-s, -s, -s);
        model.addVertex(s, -s, -s);
        model.addVertex(s, s, -s);
        model.addVertex(-s, s, -s);

        model.addVertex(-s, -s, s);
        model.addVertex(s, -s, s);
        model.addVertex(s, s, s);
        model.addVertex(-s, s, s);

        int color = 0xAAAAAA;

        model.addFace(new int[]{0,1,2,3}, color);
        model.addFace(new int[]{4,5,6,7}, color);
        model.addFace(new int[]{0,1,5,4}, color);
        model.addFace(new int[]{2,3,7,6}, color);
        model.addFace(new int[]{1,2,6,5}, color);
        model.addFace(new int[]{0,3,7,4}, color);
    }
}