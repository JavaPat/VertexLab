package org.vertex.model;

public class Face {
    public int[] indices; // 3 or 4 vertices
    public int color;     // RGB packed (0xRRGGBB)

    public Face(int[] indices, int color) {
        this.indices = indices;
        this.color = color;
    }
}