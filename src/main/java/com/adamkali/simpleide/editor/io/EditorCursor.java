package com.adamkali.simpleide.editor.io;

/**
 * The cursor is responsible for moving around the document.
 * Cursor keeps track of three things:
 * 1. The current column
 * 2. The current line
 * 3. The document
 */
public class EditorCursor {
    private int currentColumn;
    private int currentLine;

    public Document document;

    private TextPosition selectionStart;
    private TextPosition selectionEnd;


    public EditorCursor(Document document) {
        this(document, 0, 0);
    }

    public EditorCursor(Document document, int column, int currentLine) {
        this.document = new Document();
        this.currentColumn = column;
        this.currentLine = currentLine;
    }

    /**
     * Moves the cursor up one line.
     * If the cursor is already on the first line, it will not move.
     * If the cursor is past the end of the new line, it will move to the end of the line.
     */
    public void moveUp() {
        if (currentLine > 0) {
            // Move up a line
            currentLine--;

            // Move to the end of the line if the current column is past the end of the line
            if (currentColumn > document.getLine(currentLine).length()) {
                moveToEndOfLine();
            }
        }
    }

    /**
     * Moves the cursor down one line.
     * If the cursor is already on the last line, it will not move.
     * If the cursor is past the end of the new line, it will move to the end of the line.
     */
    public void moveDown() {
        if (currentLine < document.numberOfLines() - 1) {
            // Move down a line
            currentLine++;

            // Move to the end of the line if the current column is past the end of the line
            if (currentColumn > document.getLine(currentLine).length()) {
                moveToEndOfLine();
            }
        }
    }

    /**
     * Moves the cursor left one column,
     * or to the end of the previous line if the cursor is on the first column of the current line.
     */
    public void moveLeft() {
        if (currentColumn > 0) {
            currentColumn--;
        } else {
            if (currentLine == 0) {
                return;
            }

            moveUp();
            moveToEndOfLine();
        }
    }

    /**
     * Moves the cursor right one column,
     * or to the start of the next line if the cursor is on the last column of the current line.
     */
    public void moveRight() {
        if (currentColumn < document.getLine(currentLine).length()) {
            currentColumn++;
        } else {
            if (currentLine == document.numberOfLines() - 1) {
                return;
            }
            moveDown();
            moveToStartOfLine();
        }
    }

    public void moveToStartOfLine() {
        currentColumn = 0;
    }

    public void moveToEndOfLine() {
        currentColumn = document.getLine(currentLine).length();
    }

    public void moveTo(int column, int row) {
        if (column < 0 || row < 0) {
            throw new IllegalArgumentException("Column and row must be positive");
        }
        this.currentColumn = column;
        this.currentLine = row;
    }

    public void moveBy(int column, int row) {
        if (column < 0 || row < 0) {
            throw new IllegalArgumentException("Column and row must be positive");
        }
        this.currentColumn += column;
        this.currentLine += row;
    }

    public int getCurrentColumn() {
        return currentColumn;
    }

    public int getCurrentLine() {
        return currentLine;
    }

    public String getTextBeforeCursor() {
        return document.getLine(currentLine).substring(0, currentColumn);
    }

    public String getTextAfterCursor() {
        return document.getLine(currentLine).substring(currentColumn, document.getLine(currentLine).length());
    }

    public void setSelection(TextPosition start, TextPosition end) {
        this.selectionStart = start;
        this.selectionEnd = end;
    }

    public void clearSelection() {
        this.selectionStart = null;
        this.selectionEnd = null;
    }

    public String getSelectedText() {
        if (selectionStart == null || selectionEnd == null) {
            return null;
        }
        if (selectionStart.line == selectionEnd.line) {
            return document.getLine(selectionStart.line).substring(selectionStart.column, selectionEnd.column);
        } else {
            TextPosition from = selectionStart.compareTo(selectionEnd) < 0 ? selectionStart : selectionEnd;
            TextPosition to = selectionStart.compareTo(selectionEnd) < 0 ? selectionEnd : selectionStart;

            String text = document.getLine(from.line).substring(from.column, document.getLine(from.line).length());
            for (int i = from.line + 1; i < to.line; i++) {
                text += document.getLine(i);
                text += "\n";
            }
            text += document.getLine(to.line).substring(0, to.column);
            return text;
        }
    }

    public TextPosition getSelectionStart() {
        return selectionStart;
    }

    public TextPosition getSelectionEnd() {
        return selectionEnd;
    }

    public static class TextPosition implements Comparable<TextPosition> {
        public int column;
        public int line;

        public TextPosition(int column, int line) {
            this.column = column;
            this.line = line;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof TextPosition)) {
                return false;
            }
            TextPosition other = (TextPosition) o;
            return this.column == other.column && this.line == other.line;
        }

        @Override
        public String toString() {
            return "TextPosition{" +
                    "column=" + column +
                    ", line=" + line +
                    '}';
        }

        @Override
        public int compareTo(TextPosition o) {
            if (this.line < o.line) {
                return -1;
            } else if (this.line > o.line) {
                return 1;
            } else if (this.column < o.column) {
                return -1;
            } else if (this.column > o.column) {
                return 1;
            }
            return 0;
        }
    }

}
