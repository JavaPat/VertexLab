package org.vertex.editor;

import org.vertex.model.Face;
import org.vertex.model.Model3D;
import org.vertex.model.Vertex;

import java.util.ArrayList;
import java.util.List;

public class ExtrudeCommand implements Command {

    private final Model3D model;
    private final List<Vertex> createdVertices;
    private final List<Face> createdFaces;
    private final int vertexInsertIndex;
    private final int faceInsertIndex;
    private boolean firstExecutionPending;

    public ExtrudeCommand(Model3D model,
                          List<Vertex> createdVertices,
                          List<Face> createdFaces,
                          int vertexInsertIndex,
                          int faceInsertIndex,
                          boolean alreadyExecuted) {
        this.model = model;
        this.createdVertices = new ArrayList<>(createdVertices);
        this.createdFaces = new ArrayList<>(createdFaces);
        this.vertexInsertIndex = vertexInsertIndex;
        this.faceInsertIndex = faceInsertIndex;
        this.firstExecutionPending = alreadyExecuted;
    }

    @Override
    public void execute() {
        if (firstExecutionPending) {
            firstExecutionPending = false;
            return;
        }
        if (!model.vertices.containsAll(createdVertices)) {
            model.vertices.addAll(Math.min(vertexInsertIndex, model.vertices.size()), createdVertices);
        }
        if (!model.faces.containsAll(createdFaces)) {
            model.faces.addAll(Math.min(faceInsertIndex, model.faces.size()), createdFaces);
        }
    }

    @Override
    public void undo() {
        model.faces.removeAll(createdFaces);
        model.vertices.removeAll(createdVertices);
    }
}
