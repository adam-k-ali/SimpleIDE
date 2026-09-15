package com.adamkali.simpleide.editor.io.action

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BackspaceActionTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
    }

    @Test
    fun backspaceAtStartOfLine_placesCursorAtJoin() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "def".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(1, 0)

        ActionsList.BACKSPACE.execute()

        assertEquals("abcdef", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(1, Global.getCursor().getDocument().getLineCount())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(3, Global.getCursor().getColumn())
    }
}
