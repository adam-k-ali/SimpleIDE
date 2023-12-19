package com.adamkali.simpleide.codearea;

public class Cursor {
    private int column;
    private int row;

    public Cursor() {
        column = 0;
        row = 0;
    }

    public Cursor(int column, int row) {
        this.column = column;
        this.row = row;
    }

    public void moveUp() {
        if (row > 0) {
            row--;
        }
    }

    public void moveDown() {
        row++;
    }

    public void moveLeft() {
        if (column > 0) {
            column--;
        }
    }

    public void moveRight() {
        column++;
    }

    public void moveToStartOfLine() {
        column = 0;
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
}
