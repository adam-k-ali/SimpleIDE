package com.adamkali.simpleide.editor.io;

import com.adamkali.simpleide.editor.lang.tokens.NewLineToken;
import com.adamkali.simpleide.editor.lang.tokens.Token;

import java.util.ArrayList;

public class Cursor {
    private int column;
    private int row;

    public ArrayList<String> lines;
    public ArrayList<Token> tokens;

    public Cursor() {
        this(0, 0);
    }

    public Cursor(int column, int row) {
//        this.lines = new ArrayList<>();
        this.column = column;
        this.row = row;


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
    }

    public void moveUp() {
        if (row > 0) {
            row--;

            if (column > lines.get(row).length()) {
                column = lines.get(row).length();
            }
        }
    }

    public void moveDown() {
        if (row < lines.size() - 1) {
            row++;

            if (column > lines.get(row).length()) {
                column = lines.get(row).length();
            }
        }
    }

    public void moveLeft() {
        if (column > 0) {
            column--;
        } else if (row > 0) {
            row--;
            column = lines.get(row).length();
        }
    }

    public void moveRight() {
        if (column < lines.get(row).length()) {
            column++;
        } else if (row < lines.size() - 1) {
            row++;
            column = 0;
        }
    }

    public void moveToStartOfLine() {
        column = 0;
    }

    public void moveToEndOfLine() {
        column = lines.get(row).length();
    }

    public void moveTo(int column, int row) {
        if (column < 0 || row < 0) {
            throw new IllegalArgumentException("Column and row must be positive");
        }
        this.column = column;
        this.row = row;
    }

    public void moveBy(int column, int row) {
        if (column < 0 || row < 0) {
            throw new IllegalArgumentException("Column and row must be positive");
        }
        this.column += column;
        this.row += row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public String getTextBeforeCursor() {
        return lines.get(row).substring(0, column);
    }

    public String getTextAfterCursor() {
        return lines.get(row).substring(column);
    }

}
