package com.adamkali.simpleide.editor.io;

import com.adamkali.simpleide.editor.lang.tokens.*;

/**
 * The cursor is responsible for moving around the document.
 * Cursor keeps track of three things:
 * 1. The current column
 * 2. The current line
 * 3. The document
 */
public class Cursor {
    private int currentColumn;
    private int currentLine;

    public Document document;

    public Cursor(Document document) {
        this(document, 0, 0);
    }

    public Cursor(Document document, int column, int currentLine) {
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

}
