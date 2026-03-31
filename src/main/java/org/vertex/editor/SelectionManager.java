package org.vertex.editor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class SelectionManager {

    private final LinkedHashSet<Integer> selectedVertices = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedFaces = new LinkedHashSet<>();

    public void selectSingleVertex(int index) {
        selectedFaces.clear();
        selectedVertices.clear();
        if (index >= 0) {
            selectedVertices.add(index);
        }
    }

    public void toggleVertex(int index) {
        if (index < 0) {
            return;
        }
        selectedFaces.clear();
        if (!selectedVertices.remove(index)) {
            selectedVertices.add(index);
        }
    }

    public void addVertex(int index) {
        if (index < 0) {
            return;
        }
        selectedFaces.clear();
        selectedVertices.add(index);
    }

    public Set<Integer> getSelectedVertices() {
        return Collections.unmodifiableSet(selectedVertices);
    }

    public boolean hasVertexSelection() {
        return !selectedVertices.isEmpty();
    }

    public void clearVertexSelection() {
        selectedVertices.clear();
    }

    public void selectSingleFace(int index) {
        selectedVertices.clear();
        selectedFaces.clear();
        if (index >= 0) {
            selectedFaces.add(index);
        }
    }

    public void toggleFace(int index) {
        if (index < 0) {
            return;
        }
        selectedVertices.clear();
        if (!selectedFaces.remove(index)) {
            selectedFaces.add(index);
        }
    }

    public Set<Integer> getSelectedFaces() {
        return Collections.unmodifiableSet(selectedFaces);
    }

    public boolean hasFaceSelection() {
        return !selectedFaces.isEmpty();
    }

    public void clearFaceSelection() {
        selectedFaces.clear();
    }

    public void clearAll() {
        clearVertexSelection();
        clearFaceSelection();
    }

    public void pruneInvalidSelections(int vertexCount, int faceCount) {
        selectedVertices.removeIf(index -> index < 0 || index >= vertexCount);
        selectedFaces.removeIf(index -> index < 0 || index >= faceCount);
    }
}
