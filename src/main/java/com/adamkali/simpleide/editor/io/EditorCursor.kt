package com.adamkali.simpleide.editor.io

import com.adamkali.simpleide.activity.CursorActivityListener

class EditorCursor(
    private var document: Document,
    private var column: Int = 0,
    private var line: Int = 0
) {


    private var selectionStart: TextPosition? = null
    private var selectionEnd: TextPosition? = null
    private var callback: CursorActivityListener? = null

    fun setActionListener(cursorListener: CursorActivityListener) {
        this.callback = cursorListener
    }

    fun moveUp() {
        val prevPosition = TextPosition(line, column)
        if (line > 0) {
            line--
            column = Math.min(column, document.getLine(line).length())
        }
        val newPosition = TextPosition(line, column)

        if (callback != null && prevPosition != newPosition) {
            callback!!.onCursorMove(prevPosition, newPosition)
        }
    }

    fun moveDown() {
        val prevPosition = TextPosition(line, column)
        if (line < document.getLineCount() - 1) {
            line++
            column = Math.min(column, document.getLine(line).length())
        }
        val newPosition = TextPosition(line, column)

        if (callback != null && prevPosition != newPosition) {
            callback!!.onCursorMove(prevPosition, newPosition)
        }
    }

    fun moveLeft() {
        val prevPosition = TextPosition(line, column)
        if (column > 0) {
            column--
        } else if (line > 0) {
            line--
            column = document.getLine(line).length()
        }
        val newPosition = TextPosition(line, column)

        if (callback != null && prevPosition != newPosition) {
            callback!!.onCursorMove(prevPosition, newPosition)
        }
    }

    fun moveRight() {
        val prevPosition = TextPosition(line, column)
        if (column < document.getLine(line).length()) {
            column++
        } else if (line < document.getLineCount() - 1) {
            line++
            column = 0
        }
        val newPosition = TextPosition(line, column)

        if (callback != null && prevPosition != newPosition) {
            callback!!.onCursorMove(prevPosition, newPosition)
        }
    }

    fun moveToStartOfLine() {
        val prevPosition = TextPosition(line, column)
        column = 0
        val newPosition = TextPosition(line, column)

        if (callback != null && prevPosition != newPosition) {
            callback!!.onCursorMove(prevPosition, newPosition)
        }
    }

    fun moveToEndOfLine() {
        val prevPosition = TextPosition(line, column)
        column = document.getLine(line).length()
        val newPosition = TextPosition(line, column)

        if (callback != null && prevPosition != newPosition) {
            callback!!.onCursorMove(prevPosition, newPosition)
        }
    }

    fun moveToStartOfDocument() {
        val prevPosition = TextPosition(line, column)
        line = 0
        column = 0
        val newPosition = TextPosition(line, column)

        if (callback != null && prevPosition != newPosition) {
            callback!!.onCursorMove(prevPosition, newPosition)
        }
    }

    fun moveToEndOfDocument() {
        val prevPosition = TextPosition(line, column)
        line = document.getLineCount() - 1
        column = document.getLine(line).length()

        val newPosition = TextPosition(line, column)

        if (callback != null && prevPosition != newPosition) {
            callback!!.onCursorMove(prevPosition, newPosition)
        }
    }

    fun moveTo(line: Int, column: Int) {
        val prevPosition = TextPosition(this.line, this.column)

        this.line = Math.max(0, Math.min(line, document.getLineCount() - 1))
        this.column = Math.max(0, Math.min(column, document.getLine(this.line).length()))

        val newPosition = TextPosition(this.line, this.column)

        if (callback != null && prevPosition != newPosition) {
            callback!!.onCursorMove(prevPosition, newPosition)
        }
    }

    fun moveTo(position: TextPosition) {
        moveTo(position.line, position.column)
    }

    fun moveBy(line: Int, column: Int) {
        moveTo(this.line + line, this.column + column)
    }

    fun getLine(): Int {
        return line
    }

    fun getColumn(): Int {
        return column
    }

    fun getDocument(): Document {
        return document
    }

    fun getTextBeforeCursor(): String {
        return document.getLine(line).substring(0, column)
    }

    fun getTextAfterCursor(): String {
        return document.getLine(line).substring(column, document.getLine(line).length())
    }

    fun setSelection(start: TextPosition, end: TextPosition) {
        this.selectionStart = start
        this.selectionEnd = end
    }

    fun clearSelection() {
        this.selectionStart = null
        this.selectionEnd = null
    }

    fun getSelectedText(): String? {
        val range = orderedSelection() ?: return null
        val from = range.first
        val to = range.second

        if (from.line == to.line) {
            return document.getLine(from.line).substring(from.column, to.column)
        }

        val text = StringBuilder()
        text.append(document.getLine(from.line).substring(from.column, document.getLine(from.line).length()))
        for (i in from.line + 1 until to.line) {
            text.append('\n')
            text.append(document.getLine(i).toString())
        }
        text.append('\n')
        text.append(document.getLine(to.line).substring(0, to.column))
        return text.toString()
    }

    /**
     * Deletes the current selection and moves the cursor to the start of the range.
     * @return true if a non-empty selection was deleted.
     */
    fun deleteSelection(): Boolean {
        val range = orderedSelection() ?: return false
        val from = range.first
        val to = range.second

        val prefix = document.getLine(from.line).substring(0, from.column)
        val suffix = document.getLine(to.line).substring(to.column, document.getLine(to.line).length())
        document.getLine(from.line).rewrite(prefix + suffix)
        if (from.line != to.line) {
            for (i in to.line downTo from.line + 1) {
                document.removeLine(i)
            }
        }

        clearSelection()
        moveTo(from.line, from.column)
        return true
    }

    /**
     * Inserts [text] at the cursor, splitting on newlines. The cursor is left at the
     * end of the inserted text. CR/CRLF are normalized to LF.
     */
    fun insertText(text: String) {
        val normalized = text.replace("\r\n", "\n").replace("\r", "\n")
        val parts = normalized.split("\n")
        val before = getTextBeforeCursor()
        val after = getTextAfterCursor()
        val startLine = line

        document.getLine(startLine).rewrite(before + parts[0] + if (parts.size == 1) after else "")
        for (i in 1 until parts.size) {
            val newLine = Line()
            val content = if (i == parts.size - 1) parts[i] + after else parts[i]
            newLine.rewrite(content)
            document.insertLine(startLine + i, newLine)
        }

        val endLine = startLine + parts.size - 1
        val endColumn = if (parts.size == 1) before.length + parts[0].length else parts.last().length
        moveTo(endLine, endColumn)
    }

    fun getSelectionStart(): TextPosition? {
        return selectionStart
    }

    fun getSelectionEnd(): TextPosition? {
        return selectionEnd
    }

    private fun orderedSelection(): Pair<TextPosition, TextPosition>? {
        val start = selectionStart ?: return null
        val end = selectionEnd ?: return null
        val from = if (start <= end) start else end
        val to = if (start <= end) end else start
        if (from == to) {
            return null
        }
        return Pair(from, to)
    }

}
