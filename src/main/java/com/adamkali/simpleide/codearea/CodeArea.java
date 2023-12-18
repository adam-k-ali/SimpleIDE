package com.adamkali.simpleide.codearea;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class CodeArea extends Canvas implements KeyListener {
    private static final int LINE_NUM_WIDTH = 32;
    private static final int STATUS_BAR_HEIGHT = 32;
    private static final int MARGIN = 8;
    private static final int LINE_HEIGHT = 20;
    private static final int CHARACTER_WIDTH = 8;

    private static final int CURSOR_OFFSET_X = 1;
    private static final int CURSOR_OFFSET_Y = 4;
    private static final int CURSOR_HEIGHT = 16;
    private static final long CURSOR_PERIOD = 500L;

    // The position of the cursor in the codeArea
    private int cursorColumn;
    private int cursorRow;

    private boolean cursorVisible;

    // The time in milliseconds between cursor blinks
    private long lastCursorBlinkTime;

    private ArrayList<String> lines;

    public CodeArea() {
        // Setup CodeArea properties
        lines = new ArrayList<>() {{
            add("");
        }};

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
        if (System.currentTimeMillis() - lastCursorBlinkTime > CURSOR_PERIOD) {
            lastCursorBlinkTime = System.currentTimeMillis();
            cursorVisible = !cursorVisible;
        }

        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.BLACK);

        // Draw text lines
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), LINE_NUM_WIDTH + MARGIN, MARGIN + (i + 1) * LINE_HEIGHT);
        }

        // Draw cursor
        if (cursorVisible) {
            int stringWidth = g.getFontMetrics().stringWidth(lines.get(cursorRow).substring(0, cursorColumn));
            g.drawLine(LINE_NUM_WIDTH + MARGIN + stringWidth + CURSOR_OFFSET_X,
                    MARGIN + cursorRow * LINE_HEIGHT + CURSOR_OFFSET_Y,
                    LINE_NUM_WIDTH + MARGIN + stringWidth + CURSOR_OFFSET_X,
                    MARGIN + cursorRow * LINE_HEIGHT + CURSOR_HEIGHT + CURSOR_OFFSET_Y);
        }

        // Draw line numbers
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, LINE_NUM_WIDTH, getHeight());
        g.setColor(Color.BLACK);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(String.valueOf(i + 1), MARGIN , MARGIN + (i + 1) * LINE_HEIGHT);
        }

        // Draw status bar
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, getHeight() - STATUS_BAR_HEIGHT, getWidth(), STATUS_BAR_HEIGHT);
        g.setColor(Color.BLACK);
        g.drawString("SimpleIDE", MARGIN, getHeight() - MARGIN);
        g.drawString("Line: " + (cursorRow + 1) + ", Column: " + (cursorColumn + 1), getWidth() - 200, getHeight() - MARGIN);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {
            case KeyEvent.VK_ENTER:
                lines.add("");
                cursorColumn = 0;
                cursorRow++;
                break;
            case KeyEvent.VK_BACK_SPACE:
                // Don't allow backspace if we're on the first, empty line
                if (lines.size() == 1 && lines.get(0).isEmpty() || lines.isEmpty()) {
                    break;
                }

                if (!lines.get(lines.size() - 1).isEmpty()) {
                    lines.set(lines.size() - 1, lines.get(lines.size() - 1).substring(0, lines.get(lines.size() - 1).length() - 1));
                    cursorColumn--;
                } else {
                    lines.remove(lines.size() - 1);
                    cursorColumn = lines.get(lines.size() - 1).length();
                    cursorRow--;
                }
                break;
            default:
                // If the file is empty, add a new line
                if (lines.isEmpty()) {
                    lines.add("");
                }

                lines.set(lines.size() - 1, lines.get(lines.size() - 1) + e.getKeyChar());
                cursorColumn++;
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
