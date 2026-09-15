package com.adamkali.simpleide.editor

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.testsupport.GuiRender
import com.adamkali.simpleide.window.StatusPanel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.Dimension

class StatusBarTest {
    @BeforeEach
    fun resetCursor() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
    }

    @Test
    fun formatCursorPosition_isOneBasedToMatchGutter() {
        assertEquals("Line: 1, Column: 1", StatusBar.formatCursorPosition(0, 0))
        assertEquals("Line: 4, Column: 12", StatusBar.formatCursorPosition(3, 11))
    }

    @Test
    fun statusBar_fillsPanelWidth() {
        val panel = StatusPanel()
        panel.size = Dimension(640, StatusBar.STATUS_BAR_HEIGHT)
        panel.doLayout()

        assertEquals(640, panel.statusBar.width)
        assertEquals(StatusBar.STATUS_BAR_HEIGHT, panel.statusBar.height)
    }

    @Test
    fun statusBar_paintsWithoutThrowing() {
        Global.getCursor().moveTo(2, 5)
        val bar = StatusBar()
        bar.size = Dimension(400, StatusBar.STATUS_BAR_HEIGHT)
        GuiRender.render(bar, 400, StatusBar.STATUS_BAR_HEIGHT)
        assertTrue(bar.width > 0)
    }
}
