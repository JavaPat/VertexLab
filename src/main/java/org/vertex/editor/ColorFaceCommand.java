package org.vertex.editor;

import org.vertex.model.Face;

public class ColorFaceCommand implements Command {

    private final Face face;
    private final int oldColor;
    private final int newColor;

    public ColorFaceCommand(Face face, int newColor) {
        this.face = face;
        this.oldColor = face.color;
        this.newColor = newColor;
    }

    @Override
    public void execute() {
        face.color = newColor;
    }

    @Override
    public void undo() {
        face.color = oldColor;
    }
}