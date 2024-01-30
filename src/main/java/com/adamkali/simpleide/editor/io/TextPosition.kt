package com.adamkali.simpleide.editor.io

class TextPosition(val line: Int, val column: Int) : Comparable<TextPosition> {
    override fun compareTo(other: TextPosition): Int {
        if (this.line < other.line) {
            return -1
        } else if (this.line > other.line) {
            return 1
        } else if (this.column < other.column) {
            return -1
        } else if (this.column > other.column) {
            return 1
        }
        return 0
    }

    override fun toString(): String {
        return "TextPosition(line=$line, column=$column)"
    }

}
