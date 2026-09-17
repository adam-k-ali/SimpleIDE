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

class TypeCharacterActionTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
    }

    @Test
    fun type_insertsAtCursor() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 1)

        ActionsList.TYPE_CHARACTER.execute('X')

        assertEquals("aXbc", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(2, Global.getCursor().getColumn())
        assertNull(Global.getCursor().getSelectedText())
    }

    @Test
    fun type_replacesSelection() {
        "abcd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().setSelection(TextPosition(0, 1), TextPosition(0, 3))

        ActionsList.TYPE_CHARACTER.execute('X')

        assertEquals("aXd", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(2, Global.getCursor().getColumn())
        assertNull(Global.getCursor().getSelectedText())
    }
}
