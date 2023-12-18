package com.adamkali.simpleide.codearea;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class CodeArea extends Canvas implements KeyListener {
    private static final int MARGIN = 8;
    private static final int LINE_HEIGHT = 20;

    private ArrayList<String> lines;

    public CodeArea() {
        // Setup CodeArea properties
        lines = new ArrayList<>();

        // Setup listeners
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Setup canvas graphics
        setBackground(Color.WHITE);
    }

    /**
     * Updates CodeArea properties,
     * such as cursor position, text, etc.
     */
    public void update() {
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), MARGIN, MARGIN + (i + 1) * LINE_HEIGHT);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println(e.getKeyChar());
        switch (e.getKeyChar()) {
            case KeyEvent.VK_ENTER:
                lines.add("");
                break;
            case KeyEvent.VK_BACK_SPACE:
                // Don't allow backspace if we're on the first, empty line
                if (lines.size() == 1 && lines.get(0).isEmpty() || lines.isEmpty()) {
                    break;
                }

                if (!lines.get(lines.size() - 1).isEmpty()) {
                    lines.set(lines.size() - 1, lines.get(lines.size() - 1).substring(0, lines.get(lines.size() - 1).length() - 1));
                } else {
                    lines.remove(lines.size() - 1);
                }
                break;
            default:
                // If the file is empty, add a new line
                if (lines.isEmpty()) {
                    lines.add("");
                }

                lines.set(lines.size() - 1, lines.get(lines.size() - 1) + e.getKeyChar());
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
