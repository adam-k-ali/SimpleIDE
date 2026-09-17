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

class MoveLineUpActionTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
    }

    @Test
    fun moveLineUp_swapsWithPreviousLineAndMovesCaret() {
        Global.getCursor().getDocument().replaceText("first\nsecond")
        Global.getCursor().moveTo(1, 2)

        ActionsList.MOVE_LINE_UP.execute()

        assertEquals("second\nfirst", Global.getCursor().getDocument().toText())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(2, Global.getCursor().getColumn())
    }

    @Test
    fun moveLineUp_onFirstLine_isNoOp() {
        Global.getCursor().getDocument().replaceText("first\nsecond")
        Global.getCursor().moveTo(0, 1)

        ActionsList.MOVE_LINE_UP.execute()

        assertEquals("first\nsecond", Global.getCursor().getDocument().toText())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
    }

    @Test
    fun moveLineUp_clearsSelection() {
        Global.getCursor().getDocument().replaceText("first\nsecond")
        Global.getCursor().moveTo(1, 0)
        Global.getCursor().setSelection(TextPosition(1, 0), TextPosition(1, 3))

        ActionsList.MOVE_LINE_UP.execute()

        assertNull(Global.getCursor().getSelectedText())
        assertEquals("second\nfirst", Global.getCursor().getDocument().toText())
    }
}
