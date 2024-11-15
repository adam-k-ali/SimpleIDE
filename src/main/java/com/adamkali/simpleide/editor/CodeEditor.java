package com.adamkali.simpleide.editor;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.io.Line;
import com.adamkali.simpleide.editor.io.TextPosition;
import com.adamkali.simpleide.editor.io.action.ActionsList;
import com.adamkali.simpleide.preferences.EditorColors;
import com.adamkali.simpleide.project.lang.tokens.NewLineToken;
import com.adamkali.simpleide.project.lang.tokens.TabToken;
import com.adamkali.simpleide.project.lang.tokens.Token;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CodeEditor extends JPanel implements Scrollable {
    private static final int LINE_NUM_WIDTH = 64;
    private static final int MARGIN_LEFT = 8;
    private static final int MARGIN_TOP = 4;
    private static final int CHARACTER_WIDTH = 8;

    private static final int CURSOR_OFFSET_X = 0;
    private static final int CURSOR_OFFSET_Y = 4;
    private static final int CURSOR_HEIGHT = 16;
    private static final long CURSOR_PERIOD = 500L;

    private boolean cursorVisible;

    // The time in milliseconds between cursor blinks
    private long lastCursorBlinkTime;

    public CodeEditor() {
        super();
        // Setup listeners
        addKeyListener(new KeyboardHandler());
        MouseHandler mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        this.setFont(Global.getFont());

        // Setup canvas graphics
        setBackground(Color.WHITE);
    }

    /**
     * Gets the width of the string of the current line, up to a given column.
     *
     * @param column The column to get the width up to.
     * @return The width of the string (in pixels).
     */
    private int getStringWidthUpToColumn(int column) {
        return Global.getStringWidth(Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).substring(0, column));
    }

    private void expandCanvas(FontMetrics fontMetrics) {
        int numberOfLines = Global.getCursor().getDocument().getLineCount();

        int documentHeight = (numberOfLines + 6) * Global.getLineHeight();
        int documentWidth = 800;

        for (int i = 0; i < numberOfLines; i++) {
            int lineWidth = Global.getStringWidth(Global.getCursor().getDocument().getLine(i).toString());
            if (lineWidth > documentWidth) {
                documentWidth = lineWidth;
            }
        }

        setPreferredSize(new Dimension(documentWidth, Math.max(this.getParent().getHeight(), documentHeight)));
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
        expandCanvas(getFontMetrics(getFont()));
        repaint();
    }

    private void drawLineNumbers(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, LINE_NUM_WIDTH, getHeight());
        g.setColor(Color.BLACK);
        for (int i = 0; i < Global.getCursor().getDocument().getLineCount(); i++) {
            g.drawString(String.valueOf(i + 1), MARGIN_LEFT, MARGIN_TOP + (i + 1) * Global.getLineHeight());
        }
    }

    private void drawCursor(Graphics g) {
        int stringWidth = Global.getStringWidth(Global.getCursor().getTextBeforeCursor());

        g.drawLine(LINE_NUM_WIDTH + MARGIN_LEFT + stringWidth + CURSOR_OFFSET_X,
                MARGIN_TOP + Global.getCursor().getLine() * Global.getLineHeight() + CURSOR_OFFSET_Y,
                LINE_NUM_WIDTH + MARGIN_LEFT + stringWidth + CURSOR_OFFSET_X,
                MARGIN_TOP + Global.getCursor().getLine() * Global.getLineHeight() + CURSOR_HEIGHT + CURSOR_OFFSET_Y);
    }

    private void drawText(Graphics g) {
        int pxColumn = 0;
        int pxRow = Global.getLineHeight();
        String textToRender;
        int stringWidth;
        for (Line line : Global.getCursor().getDocument().getLines()) {
            for (Token token : line.getTokens()) {
                if (token instanceof NewLineToken) {
                    // There shouldn't be a NewLineToken in the middle of a line,
                    // so we can just skip to the next line.
                    break;
                }
                if (token instanceof TabToken) {
                    textToRender = "    ";
                } else {
                    textToRender = token.getText();
                }

                stringWidth = Global.getStringWidth(textToRender);
                g.setColor(token.getBackgroundColor());
                g.fillRect(LINE_NUM_WIDTH + MARGIN_LEFT + pxColumn, MARGIN_TOP + pxRow - Global.getLineHeight() + 2, stringWidth, Global.getLineHeight());

                if (!token.getValid()) {
                    g.setColor(Color.RED);
                    g.drawLine(LINE_NUM_WIDTH + MARGIN_LEFT + pxColumn, MARGIN_TOP + pxRow, LINE_NUM_WIDTH + MARGIN_LEFT + pxColumn + stringWidth, MARGIN_TOP + pxRow);
                }

                g.setColor(token.getForegroundColor());
                g.drawString(textToRender, LINE_NUM_WIDTH + MARGIN_LEFT + pxColumn, MARGIN_TOP + pxRow);
                pxColumn += stringWidth;
            }
            pxRow += Global.getLineHeight();
            pxColumn = 0;
        }
    }

    private void highlightCurrentLine(Graphics g) {
        g.setColor(Global.getTheme().getCurrentLineColor().getColor().foregroundColor());
        g.fillRect(0, MARGIN_TOP + Global.getCursor().getLine() * Global.getLineHeight() + 2, getWidth(), Global.getLineHeight());
    }

    private void drawSelectionOverlay(Graphics g) {
        TextPosition selectionStart = Global.getCursor().getSelectionStart();
        TextPosition selectionEnd = Global.getCursor().getSelectionEnd();

        if (selectionStart == null || selectionEnd == null) {
            return;
        }

        TextPosition from = selectionStart.compareTo(selectionEnd) < 0 ? selectionStart : selectionEnd;
        TextPosition to = selectionStart.compareTo(selectionEnd) < 0 ? selectionEnd : selectionStart;

        if (from.getLine() == to.getLine()) {
            int stringWidth = Global.getStringWidth(Global.getCursor().getDocument().getLine(from.getLine()).substring(from.getColumn(), to.getColumn()));
            g.setColor(EditorColors.SELECTION_COLOR);
            g.fillRect(LINE_NUM_WIDTH + MARGIN_LEFT + Global.getStringWidth(Global.getCursor().getDocument().getLine(from.getLine()).substring(0, from.getColumn())),
                    MARGIN_TOP + from.getLine() * Global.getLineHeight() + 2, stringWidth, Global.getLineHeight());
        } else {
            int stringWidth = Global.getStringWidth(Global.getCursor().getDocument().getLine(from.getLine()).substring(from.getColumn(), Global.getCursor().getDocument().getLine(from.getLine()).length()));
            g.setColor(EditorColors.SELECTION_COLOR);
            g.fillRect(LINE_NUM_WIDTH + MARGIN_LEFT + Global.getStringWidth(Global.getCursor().getDocument().getLine(from.getLine()).substring(0, from.getColumn())),
                    MARGIN_TOP + from.getLine() * Global.getLineHeight() + 2, stringWidth, Global.getLineHeight());

            for (int i = from.getLine() + 1; i < to.getLine(); i++) {
                stringWidth = Global.getStringWidth(Global.getCursor().getDocument().getLine(i).toString());
                g.fillRect(LINE_NUM_WIDTH + MARGIN_LEFT, MARGIN_TOP + i * Global.getLineHeight() + 2, stringWidth, Global.getLineHeight());
            }

            stringWidth = Global.getStringWidth(Global.getCursor().getDocument().getLine(to.getLine()).substring(0, to.getColumn()));
            g.fillRect(LINE_NUM_WIDTH + MARGIN_LEFT, MARGIN_TOP + to.getLine() * Global.getLineHeight() + 2, stringWidth, Global.getLineHeight());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(Global.getFont());
        g.setColor(Color.BLACK);

        drawText(g);
        drawSelectionOverlay(g);

        // Draw cursor
        if (cursorVisible) {
            drawCursor(g);
        }

        // Draw line numbers
        drawLineNumbers(g);

        highlightCurrentLine(g);
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

    private static class KeyboardHandler implements KeyListener {
        // Keeps track of which keys are pressed
        private static boolean[] keys = new boolean[256];

        // The number of spaces to insert when the tab key is pressed
        private static final int TAB_WIDTH = 4;

        @Override
        public void keyTyped(KeyEvent e) {
            Global.getCursor().clearSelection();
            switch (e.getKeyChar()) {
                case KeyEvent.VK_ENTER:
                    ActionsList.NEW_LINE.execute();
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    ActionsList.BACKSPACE.execute();
                    break;
                case KeyEvent.VK_TAB:
                    for (int i = 0; i < TAB_WIDTH; i++) {
                        ActionsList.TYPE_CHARACTER.execute(' ');
                    }
                    break;
                default:
                    if (e.getKeyChar() < 32) {
                        break;
                    }
                    ActionsList.TYPE_CHARACTER.execute(e.getKeyChar());
                    break;
            }

        }

        @Override
        public void keyPressed(KeyEvent e) {
            keys[e.getKeyCode()] = true;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    Global.getCursor().clearSelection();
                    Global.getCursor().moveLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    Global.getCursor().clearSelection();
                    Global.getCursor().moveRight();
                    break;
                case KeyEvent.VK_UP:
                    Global.getCursor().clearSelection();
                    Global.getCursor().moveUp();
                    break;
                case KeyEvent.VK_DOWN:
                    Global.getCursor().clearSelection();
                    Global.getCursor().moveDown();
                    break;

                case KeyEvent.VK_D:
                    if (keys[KeyEvent.VK_CONTROL]) {
                        ActionsList.DUPLICATE_LINE.execute();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            keys[e.getKeyCode()] = false;
        }
    }

    public class MouseHandler implements MouseListener, MouseMotionListener {
        private int firstX;
        private int firstY;
        private int lastX;
        private int lastY;

        private int getLine(int y) {
            int line = (y - MARGIN_TOP) / Global.getLineHeight();
            if (line < 0) {
                return 0;
            }
            return Math.min(line, Global.getCursor().getDocument().getLineCount() - 1);
        }

        private int getColumn(int x, int line) {
            int xp = x - LINE_NUM_WIDTH - MARGIN_LEFT;
            if (xp < CHARACTER_WIDTH) {
                return 0;
            }
            int column = xp / CHARACTER_WIDTH + 1;
            return Math.min(column, Global.getCursor().getDocument().getLine(line).length());
        }


        @Override
        public void mouseClicked(MouseEvent e) {
            Global.getCursor().clearSelection();

            int x = e.getX();
            int y = e.getY();
            int line = getLine(y);
            int column = getColumn(x, line);
            Global.getCursor().moveTo(line, column);
            update();
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            firstX = e.getX();
            firstY = e.getY();
            lastX = e.getX();
            lastY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
            setCursor(cursor);
//            c.setBorder(highlighted);
//            entered = c;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            setCursor(cursor);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
            // Print range of selected text
            int firstLine = getLine(firstY);
            int firstColumn = getColumn(firstX, firstLine);

            int lastLine = getLine(lastY);
            int lastColumn = getColumn(lastX, lastLine);

            Global.getCursor().setSelection(new TextPosition(firstColumn, firstLine), new TextPosition(lastColumn, lastLine));
            Global.getCursor().moveTo(lastLine, lastColumn);


            update();
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }


}
