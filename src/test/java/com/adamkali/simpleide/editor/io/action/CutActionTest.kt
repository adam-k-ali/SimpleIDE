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

class CutActionTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        EditorClipboard.reset()
    }

    @Test
    fun cut_selection_copiesAndDeletesRange() {
        "abcd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().setSelection(TextPosition(0, 1), TextPosition(0, 3))

        ActionsList.CUT.execute()

        assertEquals("bc", EditorClipboard.getText())
        assertEquals("ad", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
        assertNull(Global.getCursor().getSelectedText())
    }

    @Test
    fun cut_withoutSelection_removesCurrentLine() {
        "hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "world".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 2)

        ActionsList.CUT.execute()

        assertEquals("hello\n", EditorClipboard.getText())
        assertEquals(1, Global.getCursor().getDocument().getLineCount())
        assertEquals("world", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(2, Global.getCursor().getColumn())
    }

    @Test
    fun cut_onlyLine_leavesOneEmptyLine() {
        "hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }

        ActionsList.CUT.execute()

        assertEquals("hello\n", EditorClipboard.getText())
        assertEquals(1, Global.getCursor().getDocument().getLineCount())
        assertEquals("", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(0, Global.getCursor().getColumn())
    }
}
