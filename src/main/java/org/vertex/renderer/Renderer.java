package org.vertex.renderer;

import org.vertex.editor.SelectionManager;
import org.vertex.model.Face;
import org.vertex.model.Model3D;
import org.vertex.model.Vertex;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Renderer {

    public void render(Graphics2D g,
                       Model3D model,
                       Camera cam,
                       int width,
                       int height,
                       SelectionManager selection,
                       Rectangle selectionBox) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, width, height);

        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (ProjectedFace projectedFace : getProjectedFaces(model, cam, width, height)) {
            graphics.setColor(new Color(projectedFace.face().color));
            graphics.fillPolygon(projectedFace.polygon());
            graphics.setColor(Color.BLACK);
            graphics.drawPolygon(projectedFace.polygon());

            if (selection.getSelectedFaces().contains(projectedFace.faceIndex())) {
                graphics.setColor(new Color(255, 255, 0, 110));
                graphics.fillPolygon(projectedFace.polygon());
                graphics.setColor(new Color(255, 220, 0));
                graphics.drawPolygon(projectedFace.polygon());
            }
        }

        for (int i = 0; i < model.vertices.size(); i++) {
            Point point = project(model.vertices.get(i), cam, width, height);
            boolean selected = selection.getSelectedVertices().contains(i);
            int radius = selected ? 6 : 4;

            graphics.setColor(selected ? Color.YELLOW : new Color(235, 235, 235));
            graphics.fillOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
            graphics.setColor(Color.BLACK);
            graphics.drawOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
        }

        if (selectionBox != null) {
            graphics.setColor(new Color(255, 255, 255, 40));
            graphics.fill(selectionBox);
            graphics.setColor(new Color(255, 255, 255, 180));
            graphics.draw(selectionBox);
        }

        graphics.dispose();
    }

    public Point project(Vertex vertex, Camera camera, int width, int height) {
        ProjectedVertex projectedVertex = projectVertex(vertex, camera, width, height);
        return new Point(projectedVertex.x(), projectedVertex.y());
    }

    public List<ProjectedFace> getProjectedFaces(Model3D model, Camera camera, int width, int height) {
        List<ProjectedFace> projectedFaces = new ArrayList<>();
        for (int faceIndex = 0; faceIndex < model.faces.size(); faceIndex++) {
            Face face = model.faces.get(faceIndex);
            Polygon polygon = new Polygon();
            float depthSum = 0f;
            boolean valid = true;

            for (int vertexIndex : face.indices) {
                if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) {
                    valid = false;
                    break;
                }
                ProjectedVertex projectedVertex = projectVertex(model.vertices.get(vertexIndex), camera, width, height);
                polygon.addPoint(projectedVertex.x(), projectedVertex.y());
                depthSum += projectedVertex.depth();
            }

            if (valid) {
                projectedFaces.add(new ProjectedFace(faceIndex, face, polygon, depthSum / face.indices.length));
            }
        }

        projectedFaces.sort(Comparator.comparingDouble(ProjectedFace::averageDepth));
        return projectedFaces;
    }

    private ProjectedVertex projectVertex(Vertex vertex, Camera camera, int width, int height) {
        double radX = Math.toRadians(camera.rotX);
        double radY = Math.toRadians(camera.rotY);

        double cosY = Math.cos(radY);
        double sinY = Math.sin(radY);
        double cosX = Math.cos(radX);
        double sinX = Math.sin(radX);

        double x1 = vertex.x * cosY - vertex.z * sinY;
        double z1 = vertex.x * sinY + vertex.z * cosY;
        double y1 = vertex.y * cosX - z1 * sinX;
        double z2 = vertex.y * sinX + z1 * cosX;

        float scale = (float) (100 / (camera.zoom + z2 * 0.1));
        int screenX = (int) (x1 * scale + width / 2f);
        int screenY = (int) (-y1 * scale + height / 2f);

        return new ProjectedVertex(screenX, screenY, (float) z2);
    }

    public record ProjectedFace(int faceIndex, Face face, Polygon polygon, float averageDepth) {
    }

    private record ProjectedVertex(int x, int y, float depth) {
    }
}
