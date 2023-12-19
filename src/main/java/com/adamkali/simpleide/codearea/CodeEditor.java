package com.adamkali.simpleide.codearea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class CodeEditor extends JPanel implements Scrollable, KeyListener, MouseListener {
    private static final int LINE_NUM_WIDTH = 32;
    private static final int MARGIN_LEFT = 8;
    private static final int MARGIN_TOP = 4;
    private static final int LINE_HEIGHT = 20;
    private static final int CHARACTER_WIDTH = 8;

    private static final int CURSOR_OFFSET_X = 1;
    private static final int CURSOR_OFFSET_Y = 4;
    private static final int CURSOR_HEIGHT = 16;
    private static final long CURSOR_PERIOD = 500L;

    public static final Cursor CURSOR = new Cursor();

    private boolean cursorVisible;

    // The time in milliseconds between cursor blinks
    private long lastCursorBlinkTime;

    private ArrayList<String> lines;

    public CodeEditor() {
        super();
        // Setup CodeArea properties
        lines = new ArrayList<>() {{
            add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add("");
            add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add("");
            add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add("");
            add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add("");
            add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add("");
            add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add("");
            add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add("");
            add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add(""); add("");
        }};

        // Setup listeners
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Setup canvas graphics
        expandCanvas(getFontMetrics(getFont()));
        setBackground(Color.WHITE);
    }

    private void expandCanvas(FontMetrics fontMetrics) {
        int documentHeight = (lines.size() + 6) * LINE_HEIGHT;
        int documentWidth = 800;

        for (String line : lines) {
            int lineWidth = fontMetrics.stringWidth(line);
            if (lineWidth > documentWidth) {
                documentWidth = lineWidth;
            }
        }

        setSize(documentWidth, documentHeight);
        setPreferredSize(new Dimension(documentWidth, documentHeight));
        revalidate();
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

    private void drawLineNumbers(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, LINE_NUM_WIDTH, getHeight());
        g.setColor(Color.BLACK);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(String.valueOf(i + 1), MARGIN_LEFT, MARGIN_TOP + (i + 1) * LINE_HEIGHT);
        }
    }

    private void drawCursor(Graphics g) {
        int stringWidth = g.getFontMetrics().stringWidth(lines.get(CURSOR.getRow()).substring(0, CURSOR.getColumn()));
        g.drawLine(LINE_NUM_WIDTH + MARGIN_LEFT + stringWidth + CURSOR_OFFSET_X,
                MARGIN_TOP + CURSOR.getRow() * LINE_HEIGHT + CURSOR_OFFSET_Y,
                LINE_NUM_WIDTH + MARGIN_LEFT + stringWidth + CURSOR_OFFSET_X,
                MARGIN_TOP + CURSOR.getRow() * LINE_HEIGHT + CURSOR_HEIGHT + CURSOR_OFFSET_Y);
    }


    @Override
    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
        g.setColor(Color.BLACK);

        // Draw text lines
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), LINE_NUM_WIDTH + MARGIN_LEFT, MARGIN_TOP + (i + 1) * LINE_HEIGHT);
        }

        // Draw cursor
        if (cursorVisible) {
            drawCursor(g);
        }

        // Draw line numbers
        drawLineNumbers(g);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        expandCanvas(getFontMetrics(getFont()));

        String textBeforeCursor = lines.get(CURSOR.getRow()).substring(0, CURSOR.getColumn());
        String textAfterCursor = lines.get(CURSOR.getRow()).substring(CURSOR.getColumn());

        switch (e.getKeyChar()) {
            case KeyEvent.VK_ENTER:
                lines.set(CURSOR.getRow(), textBeforeCursor);
                lines.add(CURSOR.getRow() + 1, textAfterCursor);
                CURSOR.moveBy(-textBeforeCursor.length(), 1);
                break;
            case KeyEvent.VK_BACK_SPACE:
                // Don't allow backspace if we're on the first, empty line
                if (lines.size() == 1 && lines.get(0).isEmpty() || lines.isEmpty()) {
                    break;
                }

                if (!textBeforeCursor.isEmpty()) {
                    lines.set(CURSOR.getRow(), textBeforeCursor.substring(0, textBeforeCursor.length() - 1) + textAfterCursor);
                    CURSOR.moveLeft();
                } else {
                    // Move the text from the current line to the previous line, and delete the current line
                    lines.remove(CURSOR.getRow());
                    lines.set(CURSOR.getRow() - 1, lines.get(CURSOR.getRow() - 1) + textAfterCursor);

                    CURSOR.moveUp();
                    CURSOR.moveTo(lines.get(lines.size() - 1).length() - textAfterCursor.length(), CURSOR.getRow() - 1);

                }
                break;

            default:
                // If the file is empty, add a new line
                if (lines.isEmpty()) {
                    lines.add("");
                    CURSOR.moveTo(0, 0);
                }

                lines.set(CURSOR.getRow(), textBeforeCursor + e.getKeyChar() + textAfterCursor);
                CURSOR.moveRight();
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        String textBeforeCursor = lines.get(CURSOR.getRow()).substring(0, CURSOR.getColumn());
        String textAfterCursor = lines.get(CURSOR.getRow()).substring(CURSOR.getColumn());

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (textBeforeCursor.isEmpty()) {
                    if (CURSOR.getRow() > 0) {
                        CURSOR.moveTo(lines.get(CURSOR.getRow()).length(), CURSOR.getRow() - 1);
                    }
                } else {
                    CURSOR.moveLeft();
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (textAfterCursor.isEmpty()) {
                    if (CURSOR.getRow() < lines.size() - 1) {
                        CURSOR.moveTo(0, CURSOR.getRow() + 1);
                    }
                } else {
                    CURSOR.moveRight();
                }
                break;
            case KeyEvent.VK_UP:
                if (CURSOR.getRow() > 0) {
                    CURSOR.moveUp();
                    if (CURSOR.getColumn() > lines.get(CURSOR.getRow()).length()) {
                        CURSOR.moveTo(lines.get(CURSOR.getRow()).length(), CURSOR.getRow());
                    }
                }
                break;
            case KeyEvent.VK_DOWN:
                if (CURSOR.getRow() < lines.size() - 1) {
                    CURSOR.moveDown();
                    if (CURSOR.getColumn() > lines.get(CURSOR.getRow()).length()) {
                        CURSOR.moveTo(lines.get(CURSOR.getRow()).length(), CURSOR.getRow());
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
        column = (e.getX() - LINE_NUM_WIDTH - MARGIN_LEFT) / CHARACTER_WIDTH;
        row = (e.getY() - MARGIN_TOP) / LINE_HEIGHT;

        // If the cursor is out of bounds, set it to the last line
        if (row > lines.size() - 1) {
            row = lines.size() - 1;
        }

        // If the cursor is out of bounds, set it to the end of the line
        if (column > lines.get(row).length()) {
            column = lines.get(row).length();
        }

        CURSOR.moveTo(column, row);
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

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(getSize().width, getSize().height);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? LINE_HEIGHT : CHARACTER_WIDTH;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? LINE_HEIGHT : CHARACTER_WIDTH;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
