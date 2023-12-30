package com.adamkali.simpleide.codearea;

import com.adamkali.simpleide.Global;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

    private boolean cursorVisible;

    // The time in milliseconds between cursor blinks
    private long lastCursorBlinkTime;

    public CodeEditor() {
        super();
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
        int documentHeight = (Global.getCursor().lines.size() + 6) * LINE_HEIGHT;
        int documentWidth = 800;

        for (String line : Global.getCursor().lines) {
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
        for (int i = 0; i < Global.getCursor().lines.size(); i++) {
            g.drawString(String.valueOf(i + 1), MARGIN_LEFT, MARGIN_TOP + (i + 1) * LINE_HEIGHT);
        }
    }

    private void drawCursor(Graphics g) {
        int stringWidth = g.getFontMetrics().stringWidth(Global.getCursor().lines.get(Global.getCursor().getRow()).substring(0, Global.getCursor().getColumn()));
        g.drawLine(LINE_NUM_WIDTH + MARGIN_LEFT + stringWidth + CURSOR_OFFSET_X,
                MARGIN_TOP + Global.getCursor().getRow() * LINE_HEIGHT + CURSOR_OFFSET_Y,
                LINE_NUM_WIDTH + MARGIN_LEFT + stringWidth + CURSOR_OFFSET_X,
                MARGIN_TOP + Global.getCursor().getRow() * LINE_HEIGHT + CURSOR_HEIGHT + CURSOR_OFFSET_Y);
    }


    @Override
    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
        g.setColor(Color.BLACK);

        // Draw text lines
        for (int i = 0; i < Global.getCursor().lines.size(); i++) {
            g.drawString(Global.getCursor().lines.get(i), LINE_NUM_WIDTH + MARGIN_LEFT, MARGIN_TOP + (i + 1) * LINE_HEIGHT);
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
        String textBeforeCursor = Global.getCursor().getTextBeforeCursor();
        String textAfterCursor = Global.getCursor().getTextAfterCursor();
        switch (e.getKeyChar()) {
            case KeyEvent.VK_ENTER:
                Global.getCursor().lines.set(Global.getCursor().getRow(), textBeforeCursor);
                Global.getCursor().lines.add(Global.getCursor().getRow() + 1, textAfterCursor);
                Global.getCursor().moveDown();
                break;
            case KeyEvent.VK_BACK_SPACE:
                // Don't allow backspace if we're on the first, empty line
                if (Global.getCursor().lines.size() == 1 && Global.getCursor().lines.get(0).isEmpty() || Global.getCursor().lines.isEmpty()) {
                    break;
                }

                if (!textBeforeCursor.isEmpty()) {
                    Global.getCursor().lines.set(Global.getCursor().getRow(), textBeforeCursor.substring(0, textBeforeCursor.length() - 1) + textAfterCursor);
                    Global.getCursor().moveLeft();
                } else {
                    if (Global.getCursor().getRow() == 0) {
                        break;
                    }

                    // Move the text from the current line to the previous line, and delete the current line
                    Global.getCursor().lines.remove(Global.getCursor().getRow());
                    int previousLineLength = Global.getCursor().lines.get(Global.getCursor().getRow() - 1).length();
                    Global.getCursor().lines.set(Global.getCursor().getRow() - 1, Global.getCursor().lines.get(Global.getCursor().getRow() - 1) + textAfterCursor);
                    Global.getCursor().moveTo(previousLineLength, Global.getCursor().getRow() - 1);
                }
                break;

            default:
                // If the file is empty, add a new line
                if (Global.getCursor().lines.isEmpty()) {
                    Global.getCursor().lines.add("");
                    Global.getCursor().moveTo(0, 0);
                }

                Global.getCursor().lines.set(Global.getCursor().getRow(), textBeforeCursor + e.getKeyChar() + textAfterCursor);
                Global.getCursor().moveRight();
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                Global.getCursor().moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                Global.getCursor().moveRight();
                break;
            case KeyEvent.VK_UP:
                Global.getCursor().moveUp();
                break;
            case KeyEvent.VK_DOWN:
                Global.getCursor().moveDown();
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
        int column, row;

        // Calculate cursor position
        row = (e.getY() - Global.getMarginTop()) / Global.getLineHeight();
        if (row > Global.getCursor().lines.size() - 1) {
            row = Global.getCursor().lines.size() - 1;
        }
        // Column is the number of characters from the left of the screen,
        // as each character is of a different width, this will need to be done iteratively
        // If the cursor is out of bounds, set it to the last line
        column = 0;
        for (int i = 0; i < Global.getCursor().lines.get(row).length(); i++) {
            column += Global.getStringWidth(Global.getCursor().lines.get(row).substring(i, i + 1));
            if (column > e.getX()) {
                column = i;
                break;
            }
        }

        // If the cursor is out of bounds, set it to the end of the line
        if (column > Global.getCursor().lines.get(row).length()) {
            column = Global.getCursor().lines.get(row).length();
        }

        Global.getCursor().moveTo(column, row);
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
