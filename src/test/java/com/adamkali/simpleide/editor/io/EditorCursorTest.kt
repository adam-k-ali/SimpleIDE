package com.adamkali.simpleide.editor.io

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.action.ActionsList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EditorCursorTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
    }

    @Test
    fun moveTo_textPosition_updatesLineAndColumn() {
        ActionsList.TYPE_CHARACTER.execute('a')
        ActionsList.NEW_LINE.execute()
        ActionsList.TYPE_CHARACTER.execute('b')
        Global.getCursor().moveTo(TextPosition(0, 1))
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
    }

    @Test
    fun getSelectedText_ordersBackwardSameLineSelection() {
        "abcd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().setSelection(TextPosition(0, 3), TextPosition(0, 1))
        assertEquals("bc", Global.getCursor().getSelectedText())
    }

    @Test
    fun getSelectedText_returnsNullForEmptySelection() {
        Global.getCursor().setSelection(TextPosition(0, 0), TextPosition(0, 0))
        assertNull(Global.getCursor().getSelectedText())
    }

    @Test
    fun textPosition_equalsByValue() {
        assertEquals(TextPosition(2, 5), TextPosition(2, 5))
    }
}
