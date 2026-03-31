package org.vertex.ui;

import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel {

    public HelpPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 0));
        setBackground(new Color(40, 40, 40));

        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setForeground(Color.WHITE);
        text.setBackground(new Color(40, 40, 40));
        text.setFont(new Font("Monospaced", Font.PLAIN, 12));

        text.setText("""
VERTEX LAB CONTROLS

MOUSE:
- Left Click: Select vertex
- Shift + Click: Multi-select
- Left Drag (vertex): Move selection
- Left Drag (empty): Box select
- Right Drag: Rotate camera
- Scroll: Zoom

KEYS:
- X / Y / Z: Lock movement axis
- ESC: Clear axis lock
- G: Toggle grid snapping
- F: Create face (3–4 vertices)
- C: Color faces
- E: Extrude selection
- DELETE: Delete selection
- Ctrl + Z: Undo
- Ctrl + Y: Redo

TOOLS:
- Gizmo: Click axis to constrain movement
- Grid: Helps align models (snapping)
- Face Creation: Select vertices → press F
- Extrude: Select face → press E
- Color: Select vertices → press C

TIPS:
- Use Shift for multi-select
- Use snapping for clean geometry
- Right-click drag to rotate camera
- Box select speeds up workflows
- Undo is your safety net :)
""");

        add(new JScrollPane(text), BorderLayout.CENTER);
    }
}