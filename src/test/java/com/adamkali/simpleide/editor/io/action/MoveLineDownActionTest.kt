package com.adamkali.simpleide.editor.io.action

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MoveLineDownActionTest {
    @BeforeEach
    fun resetDocument() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
    }

    @Test
    fun moveLineDown_swapsWithNextLineAndMovesCaret() {
        Global.getCursor().getDocument().replaceText("first\nsecond")
        Global.getCursor().moveTo(0, 2)

        ActionsList.MOVE_LINE_DOWN.execute()

        assertEquals("second\nfirst", Global.getCursor().getDocument().toText())
        assertEquals(1, Global.getCursor().getLine())
        assertEquals(2, Global.getCursor().getColumn())
    }

    @Test
    fun moveLineDown_onLastLine_isNoOp() {
        Global.getCursor().getDocument().replaceText("first\nsecond")
        Global.getCursor().moveTo(1, 1)

        ActionsList.MOVE_LINE_DOWN.execute()

        assertEquals("first\nsecond", Global.getCursor().getDocument().toText())
        assertEquals(1, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
    }
}
