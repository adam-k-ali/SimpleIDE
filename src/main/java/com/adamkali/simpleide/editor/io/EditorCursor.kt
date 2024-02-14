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
        val prevPosition = TextPosition(line, column)
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
        if (selectionStart == null || selectionEnd == null) {
            return null;
        }

        if (selectionStart!!.line == selectionEnd!!.line) {
            return document.getLine(selectionStart!!.line).substring(selectionStart!!.column, selectionEnd!!.column)
        } else {
            val from: TextPosition = if (selectionStart!! < selectionEnd!!) selectionStart!! else selectionEnd!!
            val to: TextPosition = if (selectionStart!! < selectionEnd!!) selectionEnd!! else selectionStart!!

            var text = document.getLine(from.line).substring(from.column, document.getLine(from.line).length())
            for (i in from.line + 1 until to.line) {
                text += document.getLine(i)
                text += "\n"
            }
            text += document.getLine(to.line).substring(0, to.column)
            return text
        }
    }

    fun getSelectionStart(): TextPosition? {
        return selectionStart
    }

    fun getSelectionEnd(): TextPosition? {
        return selectionEnd
    }


}