package org.vertex.model;

import java.util.ArrayList;
import java.util.List;

public class Model3D {
    public List<Vertex> vertices = new ArrayList<>();
    public List<Face> faces = new ArrayList<>();

    public void addVertex(float x, float y, float z) {
        vertices.add(new Vertex(x, y, z));
    }

    public void addFace(int[] indices, int color) {
        faces.add(new Face(indices, color));
    }

    public void clear() {
        vertices.clear();
        faces.clear();
    }
}