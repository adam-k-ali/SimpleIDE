package com.adamkali.simpleide.codearea;

import javax.tools.Tool;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class CodeArea extends Canvas implements KeyListener, MouseListener {
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
        addMouseListener(this);
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
        String textBeforeCursor = lines.get(cursorRow).substring(0, cursorColumn);
        String textAfterCursor = lines.get(cursorRow).substring(cursorColumn);

        switch (e.getKeyChar()) {
            case KeyEvent.VK_ENTER:
                lines.set(cursorRow, textBeforeCursor);
                lines.add(cursorRow + 1, textAfterCursor);
                cursorColumn = 0;
                cursorRow++;
                break;
            case KeyEvent.VK_BACK_SPACE:
                // Don't allow backspace if we're on the first, empty line
                if (lines.size() == 1 && lines.get(0).isEmpty() || lines.isEmpty()) {
                    break;
                }

                if (!textBeforeCursor.isEmpty()) {
                    lines.set(cursorRow, textBeforeCursor.substring(0, textBeforeCursor.length() - 1) + textAfterCursor);
                    cursorColumn--;
                } else {
                    // Move the text from the current line to the previous line, and delete the current line
                    lines.remove(lines.size() - 1);

                    lines.set(cursorRow - 1, lines.get(cursorRow - 1) + textAfterCursor);
                    cursorColumn = lines.get(lines.size() - 1).length() - textAfterCursor.length();
                    cursorRow--;
                }
                break;

            default:
                // If the file is empty, add a new line
                if (lines.isEmpty()) {
                    lines.add("");
                    cursorRow = 0;
                }

                lines.set(cursorRow, textBeforeCursor + e.getKeyChar() + textAfterCursor);
                cursorColumn++;
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        String textBeforeCursor = lines.get(cursorRow).substring(0, cursorColumn);
        String textAfterCursor = lines.get(cursorRow).substring(cursorColumn);

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (textBeforeCursor.isEmpty()) {
                    if (cursorRow > 0) {
                        cursorRow--;
                        cursorColumn = lines.get(cursorRow).length();
                    }
                } else {
                    cursorColumn--;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (textAfterCursor.isEmpty()) {
                    if (cursorRow < lines.size() - 1) {
                        cursorRow++;
                        cursorColumn = 0;
                    }
                } else {
                    cursorColumn++;
                }
                break;
            case KeyEvent.VK_UP:
                if (cursorRow > 0) {
                    cursorRow--;
                    if (cursorColumn > lines.get(cursorRow).length()) {
                        cursorColumn = lines.get(cursorRow).length();
                    }
                }
                break;
            case KeyEvent.VK_DOWN:
                if (cursorRow < lines.size() - 1) {
                    cursorRow++;
                    if (cursorColumn > lines.get(cursorRow).length()) {
                        cursorColumn = lines.get(cursorRow).length();
                    }
                }
                break;
                default:
                    break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        requestFocus();
        cursorVisible = true;

        int column, row;

        // Calculate cursor position
        column = (e.getX() - LINE_NUM_WIDTH - MARGIN) / CHARACTER_WIDTH;
        row = (e.getY() - MARGIN) / LINE_HEIGHT;

        // If the cursor is out of bounds, set it to the last line
        if (row > lines.size() - 1) {
            row = lines.size() - 1;
        }

        // If the cursor is out of bounds, set it to the end of the line
        if (column > lines.get(row).length()) {
            column = lines.get(row).length();
        }

        cursorColumn = column;
        cursorRow = row;
        System.out.println(cursorColumn + ", " + cursorRow);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
