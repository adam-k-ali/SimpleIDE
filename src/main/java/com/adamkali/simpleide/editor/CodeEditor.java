package com.adamkali.simpleide.editor;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.io.Document;
import com.adamkali.simpleide.editor.io.action.ActionsList;
import com.adamkali.simpleide.editor.lang.tokens.NewLineToken;
import com.adamkali.simpleide.editor.lang.tokens.TabToken;
import com.adamkali.simpleide.editor.lang.tokens.Token;
import com.adamkali.simpleide.editor.lang.tokens.WhitespaceToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CodeEditor extends JPanel implements Scrollable, KeyListener {
    private static final int LINE_NUM_WIDTH = 32;
    private static final int MARGIN_LEFT = 8;
    private static final int MARGIN_TOP = 4;
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
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Setup canvas graphics
        expandCanvas(getFontMetrics(getFont()));
        setBackground(Color.WHITE);
    }

    /**
     * Gets the width of the string of the current line, up to a given column.
     * @param column The column to get the width up to.
     * @return The width of the string (in pixels).
     */
    private int getStringWidthUpToColumn(int column) {
        return Global.getStringWidth(Global.getCursor().document.getLine(Global.getCursor().getCurrentLine()).substring(0, column));
    }

    private void expandCanvas(FontMetrics fontMetrics) {
        int numberOfLines = Global.getCursor().document.numberOfLines();

        int documentHeight = (numberOfLines + 6) * Global.getLineHeight();
        int documentWidth = 800;

        for (int i = 0; i < numberOfLines; i++) {
            int lineWidth = Global.getStringWidth(Global.getCursor().document.getLine(i).toString());
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
        for (int i = 0; i < Global.getCursor().document.numberOfLines(); i++) {
            g.drawString(String.valueOf(i + 1), MARGIN_LEFT, MARGIN_TOP + (i + 1) * Global.getLineHeight());
        }
    }

    private void drawCursor(Graphics g) {
        int stringWidth = Global.getStringWidth(Global.getCursor().getTextBeforeCursor());

        g.drawLine(LINE_NUM_WIDTH + MARGIN_LEFT + stringWidth + CURSOR_OFFSET_X,
                MARGIN_TOP + Global.getCursor().getCurrentLine() * Global.getLineHeight() + CURSOR_OFFSET_Y,
                LINE_NUM_WIDTH + MARGIN_LEFT + stringWidth + CURSOR_OFFSET_X,
                MARGIN_TOP + Global.getCursor().getCurrentLine() * Global.getLineHeight() + CURSOR_HEIGHT + CURSOR_OFFSET_Y);
    }

    private void drawText(Graphics g) {
        int pxColumn = 0;
        int pxRow = Global.getLineHeight();
        int stringWidth;
        for (Document.Line line : Global.getCursor().document.getLines()) {
            for (Token token : line.getTokens()) {
                if (token instanceof NewLineToken) {
                    // There shouldn't be a NewLineToken in the middle of a line,
                    // so we can just skip to the next line.
                    break;
                }
                stringWidth = Global.getStringWidth(token.getText());
                g.setColor(token.getBackgroundColor());
                g.fillRect(LINE_NUM_WIDTH + MARGIN_LEFT + pxColumn, MARGIN_TOP + pxRow - Global.getLineHeight() + 2, stringWidth, Global.getLineHeight());

                if (token.isUnderlined()) {
                    g.setColor(Color.RED);
                    g.drawLine(LINE_NUM_WIDTH + MARGIN_LEFT + pxColumn, MARGIN_TOP + pxRow, LINE_NUM_WIDTH + MARGIN_LEFT + pxColumn + stringWidth, MARGIN_TOP + pxRow);
                }

                g.setColor(token.getForegroundColor());
                g.drawString(token.getText(), LINE_NUM_WIDTH + MARGIN_LEFT + pxColumn, MARGIN_TOP + pxRow);
                pxColumn += stringWidth;
            }
            pxRow += Global.getLineHeight();
            pxColumn = 0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
        g.setFont(Global.getFont());
        g.setColor(Color.BLACK);

        drawText(g);

        // Draw cursor
        if (cursorVisible) {
            drawCursor(g);
        }

        // Draw line numbers
        drawLineNumbers(g);
    }


    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {
            case KeyEvent.VK_ENTER:
                ActionsList.NEW_LINE.execute();
                break;
            case KeyEvent.VK_BACK_SPACE:
                ActionsList.BACKSPACE.execute();
                break;
            default:
                 ActionsList.TYPE_CHARACTER.execute(e.getKeyChar());
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
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(getSize().width, getSize().height);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? Global.getLineHeight() : CHARACTER_WIDTH;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? Global.getLineHeight() : CHARACTER_WIDTH;
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