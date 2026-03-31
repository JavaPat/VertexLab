package org.vertex.editor;

import org.vertex.model.Face;
import org.vertex.model.Model3D;

import java.util.Arrays;

public class CreateFaceCommand implements Command {

    private final Model3D model;
    private final Face face;
    private final int insertionIndex;

    public CreateFaceCommand(Model3D model, Face face) {
        this.model = model;
        this.face = new Face(Arrays.copyOf(face.indices, face.indices.length), face.color);
        this.insertionIndex = model.faces.size();
    }

    @Override
    public void execute() {
        if (!model.faces.contains(face)) {
            model.faces.add(Math.min(insertionIndex, model.faces.size()), face);
        }
    }

    @Override
    public void undo() {
        model.faces.remove(face);
    }
}
