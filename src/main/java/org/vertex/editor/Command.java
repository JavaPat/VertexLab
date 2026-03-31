package org.vertex.editor;

public interface Command {
    void execute();
    void undo();
}
