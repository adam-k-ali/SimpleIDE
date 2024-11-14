package com.adamkali.simpleide.editor.io


class Document {
    var lines: MutableList<Line> = mutableListOf()
        private set

    init {
        lines.add(Line())
    }

    fun insertLine(lineIndex: Int, line: Line) {
        if (lineIndex < 0 || lineIndex > lines.size) {
            throw IndexOutOfBoundsException("Index out of bounds (lineIndex: $lineIndex)")
        }
        lines.add(lineIndex, line)
    }

    /**
     * Adds a new line after a line.
     * @param lineIndex The index of the line to add a new line after.
     */
    fun newLine(lineIndex: Int) {
        if (lineIndex < 0 || lineIndex > lines.size) {
            throw IndexOutOfBoundsException("Index out of bounds (lineIndex: $lineIndex)")
        }
        lines.add(lineIndex + 1, Line())
    }

    /**
     * Gets a line.
     * @param lineIndex The index of the line to get.
     */
    fun getLine(lineIndex: Int): Line {
        if (lineIndex < 0 || lineIndex >= lines.size) {
            throw IndexOutOfBoundsException("Index out of bounds (lineIndex: $lineIndex)")
        }
        return lines[lineIndex]
    }

    /**
     * Removes a token from a line
     * @param line The index of the line to remove the token from.
     * @param index The index of the token to remove.
     */
    fun removeToken(line: Int, index: Int) {
        if (line < 0 || line >= lines.size) {
            throw java.lang.IndexOutOfBoundsException("Line index out of bounds")
        }
        lines[line].removeToken(index)
    }

    /**
     * Removes a line from the document.
     * @param line Line number
     */
    fun removeLine(line: Int) {
        if (line < 0 || line >= lines.size) {
            throw java.lang.IndexOutOfBoundsException("Line index out of bounds")
        }
        lines.removeAt(line)
    }

    /**
     * Returns the number of lines in the document.
     * @return The number of lines in the document.
     */
    fun getLineCount(): Int {
        return lines.size
    }


}