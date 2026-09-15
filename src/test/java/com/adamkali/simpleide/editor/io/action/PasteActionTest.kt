package com.adamkali.simpleide.editor.io.action

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorClipboard
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.TextPosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PasteActionTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        EditorClipboard.reset()
    }

    @Test
    fun paste_insertsAtCursor() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 1)
        EditorClipboard.setText("XY")

        ActionsList.PASTE.execute()

        assertEquals("aXYbc", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(3, Global.getCursor().getColumn())
    }

    @Test
    fun paste_replacesSelection() {
        "abcd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().setSelection(TextPosition(0, 1), TextPosition(0, 3))
        EditorClipboard.setText("XY")

        ActionsList.PASTE.execute()

        assertEquals("aXYd", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(3, Global.getCursor().getColumn())
        assertNull(Global.getCursor().getSelectedText())
    }

    @Test
    fun paste_insertsMultipleLines() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 1)
        EditorClipboard.setText("X\nY")

        ActionsList.PASTE.execute()

        assertEquals(2, Global.getCursor().getDocument().getLineCount())
        assertEquals("aX", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals("Ybc", Global.getCursor().getDocument().getLine(1).toString())
        assertEquals(1, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
    }

    @Test
    fun paste_emptyClipboard_isNoOp() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        EditorClipboard.setText("")

        ActionsList.PASTE.execute()

        assertEquals("abc", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(3, Global.getCursor().getColumn())
    }
}
