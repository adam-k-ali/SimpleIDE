package com.adamkali.simpleide.editor.io

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.action.ActionsList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EditorCursorTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
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

    @Test
    fun getSelectedText_includesNewlinesOnMultiLineSelection() {
        "ab".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "cd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "ef".forEach { ActionsList.TYPE_CHARACTER.execute(it) }

        Global.getCursor().setSelection(TextPosition(0, 1), TextPosition(2, 1))
        assertEquals("b\ncd\ne", Global.getCursor().getSelectedText())
    }

    @Test
    fun deleteSelection_removesSameLineRangeAndMovesCursorToStart() {
        "abcd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().setSelection(TextPosition(0, 1), TextPosition(0, 3))

        assertTrue(Global.getCursor().deleteSelection())

        assertEquals("ad", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
        assertNull(Global.getCursor().getSelectedText())
    }

    @Test
    fun deleteSelection_joinsMultiLineRange() {
        "ab".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "cd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "ef".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().setSelection(TextPosition(0, 1), TextPosition(2, 1))

        assertTrue(Global.getCursor().deleteSelection())

        assertEquals(1, Global.getCursor().getDocument().getLineCount())
        assertEquals("af", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
    }

    @Test
    fun deleteSelection_returnsFalseWhenNothingSelected() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        assertFalse(Global.getCursor().deleteSelection())
        assertEquals("abc", Global.getCursor().getDocument().getLine(0).toString())
    }

    @Test
    fun insertText_insertsAtCursorAndMovesToEndOfInsert() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 1)

        Global.getCursor().insertText("XY")

        assertEquals("aXYbc", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(3, Global.getCursor().getColumn())
    }

    @Test
    fun insertText_splitsOnNewlinesAndNormalizesCrlf() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 1)

        Global.getCursor().insertText("X\r\nY")

        assertEquals(2, Global.getCursor().getDocument().getLineCount())
        assertEquals("aX", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals("Ybc", Global.getCursor().getDocument().getLine(1).toString())
        assertEquals(1, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
    }
}
