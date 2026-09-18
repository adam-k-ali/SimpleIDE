package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.UnsavedChoice
import com.adamkali.simpleide.editor.io.action.ActionsList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import javax.swing.JFrame

class AppWindowCloseTest {
    @BeforeEach
    fun setUp() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        OpenFile.showError = { _, _ -> }
        OpenFile.prompt = { UnsavedChoice.CANCEL }
    }

    @Test
    fun dirtyCancel_keepsTheFrameOpen() {
        val file = tempFile("aaa")
        assertTrue(OpenFile.open(file))
        ActionsList.TYPE_CHARACTER.execute('z')
        val frame = packedFrame()

        assertFalse(AppWindow.handleWindowClosing(frame))
        assertTrue(frame.isDisplayable)
        assertEquals("aaa", Files.readString(file, StandardCharsets.UTF_8))
    }

    @Test
    fun dirtyDiscard_disposesTheFrameWithoutWriting() {
        val file = tempFile("aaa")
        assertTrue(OpenFile.open(file))
        ActionsList.TYPE_CHARACTER.execute('z')
        OpenFile.prompt = { UnsavedChoice.DISCARD }
        val frame = packedFrame()

        assertTrue(AppWindow.handleWindowClosing(frame))
        assertFalse(frame.isDisplayable)
        assertEquals("aaa", Files.readString(file, StandardCharsets.UTF_8))
    }

    @Test
    fun cleanBuffer_closesWithoutPrompting() {
        val file = tempFile("hello")
        assertTrue(OpenFile.open(file))
        var prompted = false
        OpenFile.prompt = {
            prompted = true
            UnsavedChoice.CANCEL
        }
        val frame = packedFrame()

        assertTrue(AppWindow.handleWindowClosing(frame))
        assertFalse(frame.isDisplayable)
        assertFalse(prompted)
    }

    private fun packedFrame(): JFrame {
        val frame = JFrame()
        frame.pack()
        assertTrue(frame.isDisplayable)
        return frame
    }

    private fun tempFile(contents: String) = Files.createTempFile("simpleide-", ".txt").also {
        it.toFile().deleteOnExit()
        Files.writeString(it, contents, StandardCharsets.UTF_8)
    }
}
