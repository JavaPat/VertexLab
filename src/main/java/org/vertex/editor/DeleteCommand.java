package org.vertex.editor;

import org.vertex.model.Face;
import org.vertex.model.Model3D;
import org.vertex.model.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeleteCommand implements Command {

    private final Model3D model;
    private final Set<Integer> vertexIndicesToDelete;
    private final Set<Integer> faceIndicesToDelete;
    private List<Vertex> beforeVertices;
    private List<Face> beforeFaces;
    private List<Vertex> afterVertices;
    private List<Face> afterFaces;

    public DeleteCommand(Model3D model, Set<Integer> vertexIndicesToDelete, Set<Integer> faceIndicesToDelete) {
        this.model = model;
        this.vertexIndicesToDelete = Set.copyOf(vertexIndicesToDelete);
        this.faceIndicesToDelete = Set.copyOf(faceIndicesToDelete);
    }

    @Override
    public void execute() {
        if (beforeVertices == null) {
            beforeVertices = copyVertices(model.vertices);
            beforeFaces = copyFaces(model.faces);
            buildAfterState();
        }
        restore(afterVertices, afterFaces);
    }

    @Override
    public void undo() {
        restore(beforeVertices, beforeFaces);
    }

    private void buildAfterState() {
        int[] remap = new int[beforeVertices.size()];
        int nextVertexIndex = 0;
        for (int i = 0; i < beforeVertices.size(); i++) {
            if (vertexIndicesToDelete.contains(i)) {
                remap[i] = -1;
            } else {
                remap[i] = nextVertexIndex++;
            }
        }

        afterVertices = new ArrayList<>(nextVertexIndex);
        for (int i = 0; i < beforeVertices.size(); i++) {
            if (!vertexIndicesToDelete.contains(i)) {
                afterVertices.add(copyVertex(beforeVertices.get(i)));
            }
        }

        afterFaces = new ArrayList<>();
        for (int faceIndex = 0; faceIndex < beforeFaces.size(); faceIndex++) {
            if (faceIndicesToDelete.contains(faceIndex)) {
                continue;
            }
            Face face = beforeFaces.get(faceIndex);
            int[] remappedIndices = new int[face.indices.length];
            boolean valid = true;
            for (int i = 0; i < face.indices.length; i++) {
                int oldVertexIndex = face.indices[i];
                if (oldVertexIndex < 0 || oldVertexIndex >= remap.length || remap[oldVertexIndex] == -1) {
                    valid = false;
                    break;
                }
                remappedIndices[i] = remap[oldVertexIndex];
            }
            if (valid) {
                afterFaces.add(new Face(remappedIndices, face.color));
            }
        }
    }

    private void restore(List<Vertex> vertices, List<Face> faces) {
        model.vertices.clear();
        model.faces.clear();
        model.vertices.addAll(copyVertices(vertices));
        model.faces.addAll(copyFaces(faces));
    }

    private static List<Vertex> copyVertices(List<Vertex> vertices) {
        List<Vertex> copies = new ArrayList<>(vertices.size());
        for (Vertex vertex : vertices) {
            copies.add(copyVertex(vertex));
        }
        return copies;
    }

    private static List<Face> copyFaces(List<Face> faces) {
        List<Face> copies = new ArrayList<>(faces.size());
        for (Face face : faces) {
            copies.add(new Face(face.indices.clone(), face.color));
        }
        return copies;
    }

    private static Vertex copyVertex(Vertex vertex) {
        return new Vertex(vertex.x, vertex.y, vertex.z);
    }
}
