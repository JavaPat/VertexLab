package org.vertex.editor;

import org.vertex.model.Model3D;
import org.vertex.model.Vertex;

import java.util.ArrayList;
import java.util.List;

public class MoveVertexCommand implements Command {

    private final Model3D model;
    private final List<Integer> vertexIndices;
    private final List<float[]> oldPositions;
    private final List<float[]> newPositions;

    public MoveVertexCommand(Model3D model,
                             List<Integer> vertexIndices,
                             List<float[]> oldPositions,
                             List<float[]> newPositions) {
        this.model = model;
        this.vertexIndices = new ArrayList<>(vertexIndices);
        this.oldPositions = copyPositions(oldPositions);
        this.newPositions = copyPositions(newPositions);
    }

    @Override
    public void execute() {
        apply(newPositions);
    }

    @Override
    public void undo() {
        apply(oldPositions);
    }

    private void apply(List<float[]> positions) {
        for (int i = 0; i < vertexIndices.size(); i++) {
            int vertexIndex = vertexIndices.get(i);
            if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) {
                continue;
            }
            Vertex vertex = model.vertices.get(vertexIndex);
            float[] position = positions.get(i);
            vertex.x = position[0];
            vertex.y = position[1];
            vertex.z = position[2];
        }
    }

    private static List<float[]> copyPositions(List<float[]> positions) {
        List<float[]> copies = new ArrayList<>(positions.size());
        for (float[] position : positions) {
            copies.add(position.clone());
        }
        return copies;
    }
}
