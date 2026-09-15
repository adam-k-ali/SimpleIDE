package com.adamkali.simpleide.editor.io.action

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorClipboard
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.TextPosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CopyActionTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        EditorClipboard.reset()
    }

    @Test
    fun copy_writesSelectedTextToClipboard() {
        "abcd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().setSelection(TextPosition(0, 1), TextPosition(0, 3))

        ActionsList.COPY.execute()

        assertEquals("bc", EditorClipboard.getText())
        assertEquals("abcd", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals("bc", Global.getCursor().getSelectedText())
    }

    @Test
    fun copy_withoutSelection_copiesCurrentLineWithNewline() {
        "hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "world".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 2)

        ActionsList.COPY.execute()

        assertEquals("hello\n", EditorClipboard.getText())
        assertEquals("hello", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals("world", Global.getCursor().getDocument().getLine(1).toString())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(2, Global.getCursor().getColumn())
    }
}
