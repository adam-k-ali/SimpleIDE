package com.adamkali.simpleide.editor.io.action

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.TextPosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteActionTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
    }

    @Test
    fun deleteMidLine_removesCharacterAfterCursor() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 1)

        ActionsList.DELETE.execute()

        assertEquals("ac", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
    }

    @Test
    fun deleteAtEndOfLine_joinsWithNextLine() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "def".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 3)

        ActionsList.DELETE.execute()

        assertEquals("abcdef", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(1, Global.getCursor().getDocument().getLineCount())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(3, Global.getCursor().getColumn())
    }

    @Test
    fun deleteAtEndOfDocument_isNoOp() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }

        ActionsList.DELETE.execute()

        assertEquals("abc", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(3, Global.getCursor().getColumn())
    }

    @Test
    fun delete_replacesSelection() {
        "abcd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().setSelection(TextPosition(0, 1), TextPosition(0, 3))

        ActionsList.DELETE.execute()

        assertEquals("ad", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
        assertNull(Global.getCursor().getSelectedText())
    }
}
