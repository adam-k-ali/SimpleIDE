package com.adamkali.simpleide.editor.io

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.action.ActionsList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class OpenFileTest {
    @BeforeEach
    fun setUp() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        OpenFile.showError = { _, _ -> }
        OpenFile.prompt = { UnsavedChoice.CANCEL }
    }

    @Test
    fun open_populatesTheEditorDocument() {
        val file = tempFile("hello\nworld\n")

        assertTrue(OpenFile.open(file))
        assertEquals(file, OpenFile.path)
        assertEquals("hello\nworld\n", Global.getCursor().getDocument().toText())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(0, Global.getCursor().getColumn())
        assertFalse(OpenFile.isDirty())
    }

    @Test
    fun save_writesEditsToDisk() {
        val file = tempFile("hello")
        assertTrue(OpenFile.open(file))
        ActionsList.TYPE_CHARACTER.execute('!')

        assertTrue(OpenFile.isDirty())
        assertTrue(OpenFile.save())
        assertFalse(OpenFile.isDirty())
        assertEquals("!hello", Files.readString(file, StandardCharsets.UTF_8))
    }

    @Test
    fun reload_restoresDiskContents() {
        val file = tempFile("original")
        assertTrue(OpenFile.open(file))
        ActionsList.TYPE_CHARACTER.execute('x')
        OpenFile.prompt = { UnsavedChoice.DISCARD }

        assertTrue(OpenFile.reload())
        assertEquals("original", Global.getCursor().getDocument().toText())
        assertFalse(OpenFile.isDirty())
    }

    @Test
    fun dirtyCancel_leavesBufferAndDoesNotOpenTheOtherFile() {
        val first = tempFile("aaa")
        val second = tempFile("bbb")
        assertTrue(OpenFile.open(first))
        ActionsList.TYPE_CHARACTER.execute('z')
        OpenFile.prompt = { UnsavedChoice.CANCEL }

        assertFalse(OpenFile.open(second))
        assertEquals(first, OpenFile.path)
        assertEquals("zaaa", Global.getCursor().getDocument().toText())
        assertEquals("aaa", Files.readString(first, StandardCharsets.UTF_8))
    }

    @Test
    fun dirtyDiscard_opensTheOtherFileWithoutWriting() {
        val first = tempFile("aaa")
        val second = tempFile("bbb")
        assertTrue(OpenFile.open(first))
        ActionsList.TYPE_CHARACTER.execute('z')
        OpenFile.prompt = { UnsavedChoice.DISCARD }

        assertTrue(OpenFile.open(second))
        assertEquals(second, OpenFile.path)
        assertEquals("bbb", Global.getCursor().getDocument().toText())
        assertEquals("aaa", Files.readString(first, StandardCharsets.UTF_8))
    }

    @Test
    fun dirtySave_writesThenOpensTheOtherFile() {
        val first = tempFile("aaa")
        val second = tempFile("bbb")
        assertTrue(OpenFile.open(first))
        ActionsList.TYPE_CHARACTER.execute('z')
        OpenFile.prompt = { UnsavedChoice.SAVE }

        assertTrue(OpenFile.open(second))
        assertEquals(second, OpenFile.path)
        assertEquals("bbb", Global.getCursor().getDocument().toText())
        assertEquals("zaaa", Files.readString(first, StandardCharsets.UTF_8))
    }

    @Test
    fun saveOrReload_withNoOpenFile_isANoOp() {
        assertFalse(OpenFile.save())
        assertFalse(OpenFile.reload())
    }

    @Test
    fun missingFile_doesNotSwapTheBuffer() {
        val errors = mutableListOf<String>()
        OpenFile.showError = { _, message -> errors.add(message) }
        Global.getCursor().getDocument().replaceText("keep me")

        assertFalse(OpenFile.open(Files.createTempDirectory("simpleide-missing").resolve("nope.txt")))
        assertEquals("keep me", Global.getCursor().getDocument().toText())
        assertTrue(errors.isNotEmpty())
        assertEquals(null, OpenFile.path)
    }

    private fun tempFile(contents: String) = Files.createTempFile("simpleide-", ".txt").also {
        it.toFile().deleteOnExit()
        Files.writeString(it, contents, StandardCharsets.UTF_8)
    }
}
