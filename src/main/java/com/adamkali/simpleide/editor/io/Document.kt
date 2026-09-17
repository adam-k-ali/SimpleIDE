package com.adamkali.simpleide.editor.io


class Document {
    var lines: MutableList<Line> = mutableListOf()
        private set

    /**
     * Whether [toText] should end with a newline. Loaded from the original file
     * so a trailing newline is not painted as an extra empty line.
     */
    var trailingNewline: Boolean = false

    init {
        lines.add(Line())
    }

    /**
     * Replaces the document with [text], normalizing CR/CRLF to LF.
     * An empty file becomes one empty line. A trailing newline is stored in
     * [trailingNewline] rather than as an extra painted line.
     */
    fun replaceText(text: String) {
        val normalized = text.replace("\r\n", "\n").replace("\r", "\n")
        trailingNewline = normalized.endsWith("\n")
        val body = if (trailingNewline) normalized.removeSuffix("\n") else normalized
        val parts = if (body.isEmpty()) {
            listOf("")
        } else {
            body.split("\n")
        }

        lines.clear()
        for (part in parts) {
            val line = Line()
            line.rewrite(part)
            lines.add(line)
        }
        if (lines.isEmpty()) {
            lines.add(Line())
        }
    }

    /**
     * Serializes the document to a single string. Lines are joined with `\n`.
     * When [trailingNewline] is set, the result ends with `\n`.
     */
    fun toText(): String {
        val body = lines.joinToString("\n") { it.toString() }
        return if (trailingNewline) "$body\n" else body
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
     * Swaps the lines at [a] and [b].
     */
    fun swapLines(a: Int, b: Int) {
        if (a < 0 || a >= lines.size || b < 0 || b >= lines.size) {
            throw IndexOutOfBoundsException("Index out of bounds (a: $a, b: $b)")
        }
        val first = lines[a]
        lines[a] = lines[b]
        lines[b] = first
    }

    /**
     * Returns the number of lines in the document.
     * @return The number of lines in the document.
     */
    fun getLineCount(): Int {
        return lines.size
    }


}