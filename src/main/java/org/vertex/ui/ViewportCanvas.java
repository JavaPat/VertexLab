package org.vertex.ui;

import org.vertex.editor.CreateFaceCommand;
import org.vertex.editor.DeleteCommand;
import org.vertex.editor.ExtrudeCommand;
import org.vertex.editor.MoveVertexCommand;
import org.vertex.editor.SelectionManager;
import org.vertex.editor.UndoManager;
import org.vertex.model.Face;
import org.vertex.model.Model3D;
import org.vertex.model.Vertex;
import org.vertex.renderer.Camera;
import org.vertex.renderer.Renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ViewportCanvas extends JPanel {

    private final UndoManager undoManager = new UndoManager();
    private final Renderer renderer;
    private final Model3D model;
    private final Camera camera;
    private final SelectionManager selection = new SelectionManager();

    private int lastMouseX;
    private int lastMouseY;
    private boolean rotatingCamera;
    private boolean movingVertices;
    private boolean movedDuringDrag;

    private boolean snapEnabled = false;
    private float gridSize = 0.5f;

    private boolean boxSelecting;
    private int boxStartX;
    private int boxStartY;
    private int boxEndX;
    private int boxEndY;

    private boolean extruding;
    private int extrudeStartMouseY;
    private int[] extrudeVertexIndices = new int[0];
    private int extrudeVertexInsertIndex;
    private int extrudeFaceInsertIndex;
    private float extrudeNX;
    private float extrudeNY;
    private float extrudeNZ;
    private final List<float[]> extrudeBasePositions = new ArrayList<>();
    private final List<Vertex> extrudeCreatedVertices = new ArrayList<>();
    private final List<Face> extrudeCreatedFaces = new ArrayList<>();

    private final List<Integer> dragVertexIndices = new ArrayList<>();
    private final List<float[]> dragStartPositions = new ArrayList<>();

    private enum Axis {
        NONE, X, Y, Z
    }

    private Axis activeAxis = Axis.NONE;

    public ViewportCanvas(Model3D model, Camera camera) {
        this.model = model;
        this.camera = camera;
        this.renderer = new Renderer();

        setBackground(Color.BLACK);
        setFocusable(true);

        setupMouseControls();
        setupKeyControls();
    }

    private void setupMouseControls() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();

                lastMouseX = e.getX();
                lastMouseY = e.getY();

                if (SwingUtilities.isRightMouseButton(e)) {
                    rotatingCamera = true;
                    return;
                }

                if (extruding) {
                    extrudeStartMouseY = e.getY();
                    return;
                }

                boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
                if (pickAxis(e.getX(), e.getY())) {
                    return;
                }

                int pickedFace = pickFaceIndex(e.getX(), e.getY());
                if (pickedFace != -1) {
                    if (shift) {
                        selection.toggleFace(pickedFace);
                    } else {
                        selection.selectSingleFace(pickedFace);
                    }
                    return;
                }

                int pickedVertex = pickVertexIndex(e.getX(), e.getY());
                if (pickedVertex != -1) {
                    if (shift) {
                        selection.toggleVertex(pickedVertex);
                    } else if (!selection.getSelectedVertices().contains(pickedVertex)) {
                        selection.selectSingleVertex(pickedVertex);
                    }

                    if (selection.hasVertexSelection()) {
                        movingVertices = true;
                        movedDuringDrag = false;
                        cacheVertexDragState();
                    }
                    return;
                }

                boxSelecting = true;
                boxStartX = e.getX();
                boxStartY = e.getY();
                boxEndX = boxStartX;
                boxEndY = boxStartY;
                if (!shift) {
                    selection.clearAll();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (extruding) {
                    finishExtrusion();
                    return;
                }

                if (boxSelecting) {
                    Rectangle selectionRectangle = getSelectionRectangle();
                    boxSelecting = false;
                    applyBoxSelection(selectionRectangle);
                }

                if (movingVertices && movedDuringDrag) {
                    commitVertexMove();
                }

                rotatingCamera = false;
                movingVertices = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;

                if (boxSelecting) {
                    boxEndX = e.getX();
                    boxEndY = e.getY();
                } else if (extruding) {
                    updateExtrusion(e.getY());
                } else if (movingVertices) {
                    moveSelectedVertices(dx, dy);
                } else if (rotatingCamera) {
                    camera.rotY += dx * 0.5f;
                    camera.rotX += dy * 0.5f;
                }

                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        addMouseWheelListener(e -> {
            camera.zoom += e.getWheelRotation() * 0.5f;
            if (camera.zoom < 1f) {
                camera.zoom = 1f;
            }
        });
    }

    private void setupKeyControls() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean ctrl = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
                if (ctrl) {
                    if (e.getKeyCode() == KeyEvent.VK_Z) {
                        undoManager.undo();
                        selection.pruneInvalidSelections(model.vertices.size(), model.faces.size());
                    }
                    if (e.getKeyCode() == KeyEvent.VK_Y) {
                        undoManager.redo();
                        selection.pruneInvalidSelections(model.vertices.size(), model.faces.size());
                    }
                    repaint();
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_E -> extrudeSelection();
                    case KeyEvent.VK_F -> createFaceFromSelection();
                    case KeyEvent.VK_DELETE -> deleteSelection();
                    case KeyEvent.VK_G -> snapEnabled = !snapEnabled;
                    case KeyEvent.VK_X -> activeAxis = Axis.X;
                    case KeyEvent.VK_Y -> activeAxis = Axis.Y;
                    case KeyEvent.VK_Z -> activeAxis = Axis.Z;
                    case KeyEvent.VK_ESCAPE -> activeAxis = Axis.NONE;
                    default -> {
                    }
                }
            }
        });
    }

    private void moveSelectedVertices(int dx, int dy) {
        float moveScale = 0.01f * camera.zoom;
        float moveX = dx * moveScale;
        float moveY = -dy * moveScale;

        for (int vertexIndex : selection.getSelectedVertices()) {
            if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) {
                continue;
            }
            Vertex vertex = model.vertices.get(vertexIndex);

            if (activeAxis == Axis.X) {
                vertex.x += moveX;
            } else if (activeAxis == Axis.Y) {
                vertex.y += moveY;
            } else if (activeAxis == Axis.Z) {
                vertex.z += moveY;
            } else {
                vertex.x += moveX;
                vertex.y += moveY;
            }

            if (snapEnabled) {
                vertex.x = snap(vertex.x);
                vertex.y = snap(vertex.y);
                vertex.z = snap(vertex.z);
            }
        }

        movedDuringDrag = true;
    }

    private float snap(float value) {
        return Math.round(value / gridSize) * gridSize;
    }

    private int pickVertexIndex(int mouseX, int mouseY) {
        int bestVertex = -1;
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i < model.vertices.size(); i++) {
            Point point = renderer.project(model.vertices.get(i), camera, getWidth(), getHeight());
            double distance = point.distance(mouseX, mouseY);
            if (distance < 10 && distance < bestDistance) {
                bestVertex = i;
                bestDistance = distance;
            }
        }

        return bestVertex;
    }

    private boolean pickAxis(int mouseX, int mouseY) {
        return false;
    }

    private void createFaceFromSelection() {
        Set<Integer> selectedVertices = selection.getSelectedVertices();
        if (selectedVertices.size() < 3 || selectedVertices.size() > 4) {
            return;
        }

        int[] indices = selectedVertices.stream().mapToInt(Integer::intValue).toArray();
        undoManager.execute(new CreateFaceCommand(model, new Face(indices, Color.LIGHT_GRAY.getRGB())));

        selection.clearAll();
        selection.selectSingleFace(model.faces.size() - 1);
    }

    private void extrudeSelection() {
        if (extruding || !selection.hasFaceSelection()) {
            return;
        }

        int faceIndex = selection.getSelectedFaces().iterator().next();
        if (faceIndex < 0 || faceIndex >= model.faces.size()) {
            return;
        }

        Face selectedFace = model.faces.get(faceIndex);
        if (selectedFace.indices.length < 3) {
            return;
        }

        extrudeCreatedVertices.clear();
        extrudeCreatedFaces.clear();
        extrudeBasePositions.clear();

        Vertex v0 = model.vertices.get(selectedFace.indices[0]);
        Vertex v1 = model.vertices.get(selectedFace.indices[1]);
        Vertex v2 = model.vertices.get(selectedFace.indices[2]);

        float ux = v1.x - v0.x;
        float uy = v1.y - v0.y;
        float uz = v1.z - v0.z;
        float vx = v2.x - v0.x;
        float vy = v2.y - v0.y;
        float vz = v2.z - v0.z;

        extrudeNX = uy * vz - uz * vy;
        extrudeNY = uz * vx - ux * vz;
        extrudeNZ = ux * vy - uy * vx;

        float normalLength = (float) Math.sqrt(extrudeNX * extrudeNX + extrudeNY * extrudeNY + extrudeNZ * extrudeNZ);
        if (normalLength == 0f) {
            return;
        }

        extrudeNX /= normalLength;
        extrudeNY /= normalLength;
        extrudeNZ /= normalLength;

        extrudeVertexInsertIndex = model.vertices.size();
        extrudeFaceInsertIndex = model.faces.size();
        extrudeVertexIndices = new int[selectedFace.indices.length];

        for (int i = 0; i < selectedFace.indices.length; i++) {
            Vertex source = model.vertices.get(selectedFace.indices[i]);
            Vertex duplicate = new Vertex(source.x, source.y, source.z);
            model.vertices.add(duplicate);
            extrudeCreatedVertices.add(duplicate);
            extrudeBasePositions.add(new float[]{duplicate.x, duplicate.y, duplicate.z});
            extrudeVertexIndices[i] = model.vertices.size() - 1;
        }

        Face topFace = new Face(extrudeVertexIndices.clone(), selectedFace.color);
        model.faces.add(topFace);
        extrudeCreatedFaces.add(topFace);

        for (int i = 0; i < selectedFace.indices.length; i++) {
            int next = (i + 1) % selectedFace.indices.length;
            Face sideFace = new Face(new int[]{
                    selectedFace.indices[i],
                    selectedFace.indices[next],
                    extrudeVertexIndices[next],
                    extrudeVertexIndices[i]
            }, darken(selectedFace.color, 0.82f));
            model.faces.add(sideFace);
            extrudeCreatedFaces.add(sideFace);
        }

        extruding = true;
        extrudeStartMouseY = lastMouseY;
        selection.selectSingleFace(extrudeFaceInsertIndex);
    }

    private int darken(int color, float factor) {
        Color base = new Color(color);
        int red = Math.max(0, Math.min(255, Math.round(base.getRed() * factor)));
        int green = Math.max(0, Math.min(255, Math.round(base.getGreen() * factor)));
        int blue = Math.max(0, Math.min(255, Math.round(base.getBlue() * factor)));
        return new Color(red, green, blue).getRGB();
    }

    private void deleteSelection() {
        Set<Integer> selectedVertices = selection.getSelectedVertices();
        Set<Integer> selectedFaces = selection.getSelectedFaces();
        if (selectedVertices.isEmpty() && selectedFaces.isEmpty()) {
            return;
        }

        undoManager.execute(new DeleteCommand(model, selectedVertices, selectedFaces));
        selection.clearAll();
    }

    private int pickFaceIndex(int mouseX, int mouseY) {
        List<Renderer.ProjectedFace> projectedFaces = new ArrayList<>(renderer.getProjectedFaces(model, camera, getWidth(), getHeight()));
        projectedFaces.sort(Comparator.comparingDouble(Renderer.ProjectedFace::averageDepth).reversed());

        for (Renderer.ProjectedFace projectedFace : projectedFaces) {
            if (projectedFace.polygon().contains(mouseX, mouseY)) {
                return projectedFace.faceIndex();
            }
        }

        return -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        selection.pruneInvalidSelections(model.vertices.size(), model.faces.size());
        renderer.render((Graphics2D) g, model, camera, getWidth(), getHeight(), selection, getSelectionRectangle());
    }

    private void cacheVertexDragState() {
        dragVertexIndices.clear();
        dragStartPositions.clear();

        List<Integer> orderedSelection = new ArrayList<>(selection.getSelectedVertices());
        orderedSelection.sort(Integer::compareTo);

        for (int vertexIndex : orderedSelection) {
            if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) {
                continue;
            }
            Vertex vertex = model.vertices.get(vertexIndex);
            dragVertexIndices.add(vertexIndex);
            dragStartPositions.add(new float[]{vertex.x, vertex.y, vertex.z});
        }
    }

    private void commitVertexMove() {
        List<float[]> endPositions = new ArrayList<>(dragVertexIndices.size());
        for (int vertexIndex : dragVertexIndices) {
            if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) {
                continue;
            }
            Vertex vertex = model.vertices.get(vertexIndex);
            endPositions.add(new float[]{vertex.x, vertex.y, vertex.z});
        }

        undoManager.execute(new MoveVertexCommand(model, dragVertexIndices, dragStartPositions, endPositions));
    }

    private void updateExtrusion(int mouseY) {
        float amount = -(mouseY - extrudeStartMouseY) * 0.01f * camera.zoom;
        for (int i = 0; i < extrudeVertexIndices.length; i++) {
            int vertexIndex = extrudeVertexIndices[i];
            if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) {
                continue;
            }
            Vertex vertex = model.vertices.get(vertexIndex);
            float[] basePosition = extrudeBasePositions.get(i);
            vertex.x = basePosition[0] + extrudeNX * amount;
            vertex.y = basePosition[1] + extrudeNY * amount;
            vertex.z = basePosition[2] + extrudeNZ * amount;
        }
    }

    private void finishExtrusion() {
        extruding = false;
        undoManager.execute(new ExtrudeCommand(
                model,
                extrudeCreatedVertices,
                extrudeCreatedFaces,
                extrudeVertexInsertIndex,
                extrudeFaceInsertIndex,
                true
        ));
        selection.selectSingleFace(extrudeFaceInsertIndex);
    }

    private void applyBoxSelection(Rectangle selectionRectangle) {
        if (selectionRectangle == null || selectionRectangle.width <= 0 || selectionRectangle.height <= 0) {
            return;
        }

        for (int i = 0; i < model.vertices.size(); i++) {
            Point point = renderer.project(model.vertices.get(i), camera, getWidth(), getHeight());
            if (selectionRectangle.contains(point)) {
                selection.addVertex(i);
            }
        }
    }

    private Rectangle getSelectionRectangle() {
        if (!boxSelecting) {
            return null;
        }

        return new Rectangle(
                Math.min(boxStartX, boxEndX),
                Math.min(boxStartY, boxEndY),
                Math.abs(boxEndX - boxStartX),
                Math.abs(boxEndY - boxStartY)
        );
    }
}
