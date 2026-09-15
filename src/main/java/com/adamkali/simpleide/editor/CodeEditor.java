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
import java.util.BitSet;

public class CodeEditor extends JPanel implements Scrollable {
    private static final int CHARACTER_WIDTH = 8;

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
        return EditorCoordinates.visualWidth(
                Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).substring(0, column),
                Global::getStringWidth
        );
    }

    private void expandCanvas(FontMetrics fontMetrics) {
        int numberOfLines = Global.getCursor().getDocument().getLineCount();

        int documentHeight = (numberOfLines + 6) * Global.getLineHeight();
        int documentWidth = EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT + 800;

        for (int i = 0; i < numberOfLines; i++) {
            int lineWidth = EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT
                    + EditorCoordinates.visualWidth(Global.getCursor().getDocument().getLine(i).toString(), Global::getStringWidth);
            if (lineWidth > documentWidth) {
                documentWidth = lineWidth;
            }
        }

        int parentHeight = getParent() == null ? Math.max(getHeight(), documentHeight) : getParent().getHeight();
        setPreferredSize(new Dimension(documentWidth, Math.max(parentHeight, documentHeight)));
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
        g.fillRect(0, 0, EditorCoordinates.LINE_NUM_WIDTH, getHeight());
        g.setColor(Color.BLACK);
        for (int i = 0; i < Global.getCursor().getDocument().getLineCount(); i++) {
            g.drawString(String.valueOf(i + 1), EditorCoordinates.MARGIN_LEFT, EditorCoordinates.MARGIN_TOP + (i + 1) * Global.getLineHeight());
        }
    }

    private void drawCursor(Graphics g) {
        int x = EditorCoordinates.cursorX(Global.getCursor().getTextBeforeCursor(), Global::getStringWidth);
        int y = EditorCoordinates.cursorY(Global.getCursor().getLine(), Global.getLineHeight());

        g.setColor(Color.BLACK);
        g.drawLine(x, y, x, y + EditorCoordinates.CURSOR_HEIGHT);
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
                Color background = token.getBackgroundColor();
                if (background != null && !background.equals(EditorColors.PLAINTEXT_BG_COLOR)) {
                    g.setColor(background);
                    g.fillRect(EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT + pxColumn, EditorCoordinates.MARGIN_TOP + pxRow - Global.getLineHeight() + 2, stringWidth, Global.getLineHeight());
                }

                if (!token.getValid()) {
                    g.setColor(Color.RED);
                    g.drawLine(EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT + pxColumn, EditorCoordinates.MARGIN_TOP + pxRow, EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT + pxColumn + stringWidth, EditorCoordinates.MARGIN_TOP + pxRow);
                }

                g.setColor(token.getForegroundColor());
                g.drawString(textToRender, EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT + pxColumn, EditorCoordinates.MARGIN_TOP + pxRow);
                pxColumn += stringWidth;
            }
            pxRow += Global.getLineHeight();
            pxColumn = 0;
        }
    }

    private void highlightCurrentLine(Graphics g) {
        if (Global.getTheme() == null || Global.getTheme().getCurrentLineColor().getColor() == null) {
            return;
        }
        g.setColor(Global.getTheme().getCurrentLineColor().getColor().foregroundColor());
        int y = EditorCoordinates.lineTop(Global.getCursor().getLine(), Global.getLineHeight());
        g.fillRect(EditorCoordinates.textAreaX(), y, Math.max(0, getWidth() - EditorCoordinates.textAreaX()), Global.getLineHeight());
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
            int stringWidth = EditorCoordinates.visualWidth(Global.getCursor().getDocument().getLine(from.getLine()).substring(from.getColumn(), to.getColumn()), Global::getStringWidth);
            g.setColor(EditorColors.SELECTION_COLOR);
            g.fillRect(EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT + EditorCoordinates.visualWidth(Global.getCursor().getDocument().getLine(from.getLine()).substring(0, from.getColumn()), Global::getStringWidth),
                    EditorCoordinates.lineTop(from.getLine(), Global.getLineHeight()), stringWidth, Global.getLineHeight());
        } else {
            int stringWidth = EditorCoordinates.visualWidth(Global.getCursor().getDocument().getLine(from.getLine()).substring(from.getColumn(), Global.getCursor().getDocument().getLine(from.getLine()).length()), Global::getStringWidth);
            g.setColor(EditorColors.SELECTION_COLOR);
            g.fillRect(EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT + EditorCoordinates.visualWidth(Global.getCursor().getDocument().getLine(from.getLine()).substring(0, from.getColumn()), Global::getStringWidth),
                    EditorCoordinates.lineTop(from.getLine(), Global.getLineHeight()), stringWidth, Global.getLineHeight());

            for (int i = from.getLine() + 1; i < to.getLine(); i++) {
                stringWidth = EditorCoordinates.visualWidth(Global.getCursor().getDocument().getLine(i).toString(), Global::getStringWidth);
                g.fillRect(EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT, EditorCoordinates.lineTop(i, Global.getLineHeight()), stringWidth, Global.getLineHeight());
            }

            stringWidth = EditorCoordinates.visualWidth(Global.getCursor().getDocument().getLine(to.getLine()).substring(0, to.getColumn()), Global::getStringWidth);
            g.fillRect(EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT, EditorCoordinates.lineTop(to.getLine(), Global.getLineHeight()), stringWidth, Global.getLineHeight());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(Global.getFont());
        g.setColor(Color.BLACK);

        // Highlight sits behind text so glyphs, selection, and the cursor stay readable.
        highlightCurrentLine(g);
        drawText(g);
        drawSelectionOverlay(g);

        if (cursorVisible) {
            drawCursor(g);
        }

        // Gutter is painted last so the current-line tint never covers line numbers.
        drawLineNumbers(g);
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

    /**
     * Pans the parent viewport only when the caret would leave the visible area.
     */
    private void ensureCursorVisible() {
        int x = EditorCoordinates.cursorX(Global.getCursor().getTextBeforeCursor(), Global::getStringWidth);
        int y = EditorCoordinates.lineTop(Global.getCursor().getLine(), Global.getLineHeight());
        scrollRectToVisible(new Rectangle(x, y, 1, Global.getLineHeight()));
    }

    private class KeyboardHandler implements KeyListener {
        // Keeps track of which keys are pressed. A BitSet grows past 256 so
        // extended key codes cannot throw ArrayIndexOutOfBoundsException.
        private final BitSet keys = new BitSet();

        // The number of spaces to insert when the tab key is pressed
        private static final int TAB_WIDTH = 4;

        private void setKey(int keyCode, boolean pressed) {
            if (keyCode < 0) {
                return;
            }
            keys.set(keyCode, pressed);
        }

        private boolean isDown(int keyCode) {
            return keyCode >= 0 && keys.get(keyCode);
        }

        /**
         * True when Ctrl or Cmd is held. Avoids Toolkit.getMenuShortcutKeyMaskEx(),
         * which throws HeadlessException in tests/CI.
         */
        private boolean isMenuShortcut(KeyEvent e) {
            int mods = e.getModifiersEx();
            return (mods & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) != 0;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (isMenuShortcut(e)) {
                return;
            }
            Global.getCursor().clearSelection();
            switch (e.getKeyChar()) {
                case KeyEvent.VK_ENTER:
                    ActionsList.NEW_LINE.execute();
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    ActionsList.BACKSPACE.execute();
                    break;
                case KeyEvent.VK_DELETE:
                    ActionsList.DELETE.execute();
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
            setKey(e.getKeyCode(), true);
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    Global.getCursor().clearSelection();
                    Global.getCursor().moveLeft();
                    ensureCursorVisible();
                    e.consume();
                    break;
                case KeyEvent.VK_RIGHT:
                    Global.getCursor().clearSelection();
                    Global.getCursor().moveRight();
                    ensureCursorVisible();
                    e.consume();
                    break;
                case KeyEvent.VK_UP:
                    Global.getCursor().clearSelection();
                    Global.getCursor().moveUp();
                    ensureCursorVisible();
                    e.consume();
                    break;
                case KeyEvent.VK_DOWN:
                    Global.getCursor().clearSelection();
                    Global.getCursor().moveDown();
                    ensureCursorVisible();
                    e.consume();
                    break;

                case KeyEvent.VK_D:
                    if (isDown(KeyEvent.VK_CONTROL)) {
                        ActionsList.DUPLICATE_LINE.execute();
                    }
                    break;
                case KeyEvent.VK_C:
                    if (isMenuShortcut(e)) {
                        ActionsList.COPY.execute();
                        e.consume();
                    }
                    break;
                case KeyEvent.VK_X:
                    if (isMenuShortcut(e)) {
                        ActionsList.CUT.execute();
                        e.consume();
                    }
                    break;
                case KeyEvent.VK_V:
                    if (isMenuShortcut(e)) {
                        ActionsList.PASTE.execute();
                        e.consume();
                    }
                    break;
                case KeyEvent.VK_S:
                    if (isMenuShortcut(e)) {
                        ActionsList.SAVE.execute();
                        e.consume();
                    }
                    break;
                case KeyEvent.VK_R:
                    if (isMenuShortcut(e)) {
                        ActionsList.RELOAD.execute();
                        e.consume();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            setKey(e.getKeyCode(), false);
        }
    }

    public class MouseHandler implements MouseListener, MouseMotionListener {
        private int firstX;
        private int firstY;
        private int lastX;
        private int lastY;

        private int getLine(int y) {
            return EditorCoordinates.lineAt(y, Global.getLineHeight(), Global.getCursor().getDocument().getLineCount());
        }

        private int getColumn(int x, int line) {
            return EditorCoordinates.columnAt(
                    x,
                    Global.getCursor().getDocument().getLine(line).toString(),
                    Global::getStringWidth
            );
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

            int line = getLine(firstY);
            int column = getColumn(firstX, line);
            Global.getCursor().clearSelection();
            Global.getCursor().moveTo(line, column);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
            setCursor(cursor);
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
            int firstLine = getLine(firstY);
            int firstColumn = getColumn(firstX, firstLine);

            int lastLine = getLine(lastY);
            int lastColumn = getColumn(lastX, lastLine);

            Global.getCursor().setSelection(new TextPosition(firstLine, firstColumn), new TextPosition(lastLine, lastColumn));
            Global.getCursor().moveTo(lastLine, lastColumn);


            update();
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }


}
