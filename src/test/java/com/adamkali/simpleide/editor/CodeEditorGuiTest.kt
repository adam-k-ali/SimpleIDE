package com.adamkali.simpleide.editor

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorClipboard
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.UnsavedChoice
import com.adamkali.simpleide.editor.io.action.ActionsList
import com.adamkali.simpleide.editor.io.theme.ThemeLoader
import com.adamkali.simpleide.preferences.ThemeData
import com.adamkali.simpleide.testsupport.GuiRender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class CodeEditorGuiTest {
    @BeforeEach
    fun setUp() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        Global.setTheme(ThemeData(ThemeLoader.load("src/main/resources/preferences/editor-theme.json")))
        OpenFile.reset()
        OpenFile.showError = { _, _ -> }
        OpenFile.prompt = { UnsavedChoice.DISCARD }
        EditorClipboard.reset()
    }

    @Test
    fun lineHighlight_doesNotTintLineNumberGutter() {
        ActionsList.TYPE_CHARACTER.execute('X')
        Global.getCursor().moveTo(0, 0)

        val editor = CodeEditor()
        val image = GuiRender.render(editor, 400, 120)
        val lineHeight = Global.getLineHeight()
        val sampleY = EditorCoordinates.lineTop(0, lineHeight) + lineHeight / 2
        val gutter = GuiRender.rgb(image, 4, sampleY)

        assertTrue(
            GuiRender.isNear(gutter, Color.LIGHT_GRAY, 2),
            "gutter pixel should stay LIGHT_GRAY, but was $gutter"
        )
    }

    @Test
    fun currentLineHighlight_isVisibleBesideText_andTextStaysDark() {
        "Hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "World".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 0)

        val editor = CodeEditor()
        val image = GuiRender.render(editor, 500, 160)
        val lineHeight = Global.getLineHeight()
        val textX = EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT

        assertTrue(
            GuiRender.hasDarkGlyph(image, textX, EditorCoordinates.MARGIN_TOP, textX + 80, EditorCoordinates.MARGIN_TOP + lineHeight),
            "text on the current line should still paint as dark glyphs"
        )

        val highlightSample = GuiRender.rgb(image, textX + 120, EditorCoordinates.lineTop(0, lineHeight) + lineHeight / 2)
        val otherLineSample = GuiRender.rgb(image, textX + 120, EditorCoordinates.lineTop(1, lineHeight) + lineHeight + 8)
        assertNotEquals(
            Color.WHITE.rgb or 0xFF000000.toInt(),
            highlightSample.rgb,
            "empty space on the current line should be tinted by the line highlight"
        )
        assertTrue(
            otherLineSample.red > 240 && otherLineSample.green > 240 && otherLineSample.blue > 240,
            "empty space on a non-current line should remain near white, but was $otherLineSample"
        )
    }

    @Test
    fun mouseClick_placesCursorOnClickedCharacter() {
        "Hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        val editor = CodeEditor()
        editor.setSize(400, 120)

        val x = EditorCoordinates.cursorX("He", Global::getStringWidth)
        val y = EditorCoordinates.lineTop(0, Global.getLineHeight()) + 4
        editor.dispatchEvent(
            java.awt.event.MouseEvent(
                editor,
                java.awt.event.MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                1,
                false
            )
        )

        assertEquals(0, Global.getCursor().getLine())
        assertEquals(2, Global.getCursor().getColumn())
    }

    @Test
    fun mouseDrag_buildsSelectionWithLineThenColumn() {
        "abcd".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "efgh".forEach { ActionsList.TYPE_CHARACTER.execute(it) }

        val editor = CodeEditor()
        editor.setSize(400, 160)
        val startX = EditorCoordinates.cursorX("ab", Global::getStringWidth)
        val endX = EditorCoordinates.cursorX("ef", Global::getStringWidth)
        val startY = EditorCoordinates.lineTop(0, Global.getLineHeight()) + 4
        val endY = EditorCoordinates.lineTop(1, Global.getLineHeight()) + 4

        GuiRender.drag(editor, startX, startY, endX, endY)

        val start = Global.getCursor().getSelectionStart()
        val end = Global.getCursor().getSelectionEnd()
        assertEquals(0, start!!.line)
        assertEquals(2, start.column)
        assertEquals(1, end!!.line)
        assertEquals(2, end.column)
        assertFalse(start.line == 2 && start.column == 0, "TextPosition must not be constructed as (column, line)")
    }

    @Test
    fun highKeyCodes_doNotCrashKeyboardHandler() {
        val editor = CodeEditor()
        editor.dispatchEvent(
            java.awt.event.KeyEvent(
                editor,
                java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                0xF000,
                java.awt.event.KeyEvent.CHAR_UNDEFINED
            )
        )
        editor.dispatchEvent(
            java.awt.event.KeyEvent(
                editor,
                java.awt.event.KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(),
                0,
                0xF000,
                java.awt.event.KeyEvent.CHAR_UNDEFINED
            )
        )
    }

    @Test
    fun ctrlS_savesTheOpenFile() {
        val file = Files.createTempFile("simpleide-save-", ".txt")
        file.toFile().deleteOnExit()
        Files.writeString(file, "hello", StandardCharsets.UTF_8)
        assertTrue(OpenFile.open(file))
        ActionsList.TYPE_CHARACTER.execute('!')

        dispatchShortcut(CodeEditor(), KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)

        assertEquals("!hello", Files.readString(file, StandardCharsets.UTF_8))
        assertEquals(false, OpenFile.isDirty())
    }

    @Test
    fun metaS_savesTheOpenFile() {
        val file = Files.createTempFile("simpleide-save-meta-", ".txt")
        file.toFile().deleteOnExit()
        Files.writeString(file, "hello", StandardCharsets.UTF_8)
        assertTrue(OpenFile.open(file))
        ActionsList.TYPE_CHARACTER.execute('!')

        dispatchShortcut(CodeEditor(), KeyEvent.VK_S, InputEvent.META_DOWN_MASK)

        assertEquals("!hello", Files.readString(file, StandardCharsets.UTF_8))
    }

    @Test
    fun ctrlR_reloadsTheOpenFile() {
        val file = Files.createTempFile("simpleide-reload-", ".txt")
        file.toFile().deleteOnExit()
        Files.writeString(file, "original", StandardCharsets.UTF_8)
        assertTrue(OpenFile.open(file))
        ActionsList.TYPE_CHARACTER.execute('x')
        OpenFile.prompt = { UnsavedChoice.DISCARD }

        dispatchShortcut(CodeEditor(), KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK)

        assertEquals("original", Global.getCursor().getDocument().toText())
    }

    @Test
    fun metaR_reloadsTheOpenFile() {
        val file = Files.createTempFile("simpleide-reload-meta-", ".txt")
        file.toFile().deleteOnExit()
        Files.writeString(file, "original", StandardCharsets.UTF_8)
        assertTrue(OpenFile.open(file))
        ActionsList.TYPE_CHARACTER.execute('x')
        OpenFile.prompt = { UnsavedChoice.DISCARD }

        dispatchShortcut(CodeEditor(), KeyEvent.VK_R, InputEvent.META_DOWN_MASK)

        assertEquals("original", Global.getCursor().getDocument().toText())
    }

    @Test
    fun deleteKey_removesCharacterAfterCursor() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 1)

        val editor = CodeEditor()
        val event = KeyEvent(
            editor,
            KeyEvent.KEY_TYPED,
            System.currentTimeMillis(),
            0,
            KeyEvent.VK_UNDEFINED,
            KeyEvent.VK_DELETE.toChar()
        )
        editor.keyListeners.forEach { it.keyTyped(event) }

        assertEquals("ac", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(1, Global.getCursor().getColumn())
    }

    @Test
    fun ctrlC_copiesCurrentLine() {
        "hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }

        dispatchShortcut(CodeEditor(), KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)

        assertEquals("hello\n", EditorClipboard.getText())
    }

    @Test
    fun metaC_copiesCurrentLine() {
        "hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }

        dispatchShortcut(CodeEditor(), KeyEvent.VK_C, InputEvent.META_DOWN_MASK)

        assertEquals("hello\n", EditorClipboard.getText())
    }

    @Test
    fun ctrlX_cutsCurrentLine() {
        "hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "world".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 0)

        dispatchShortcut(CodeEditor(), KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK)

        assertEquals("hello\n", EditorClipboard.getText())
        assertEquals("world", Global.getCursor().getDocument().toText())
    }

    @Test
    fun metaX_cutsCurrentLine() {
        "hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "world".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 0)

        dispatchShortcut(CodeEditor(), KeyEvent.VK_X, InputEvent.META_DOWN_MASK)

        assertEquals("hello\n", EditorClipboard.getText())
        assertEquals("world", Global.getCursor().getDocument().toText())
    }

    @Test
    fun ctrlV_pastesClipboardText() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 1)
        EditorClipboard.setText("XY")

        dispatchShortcut(CodeEditor(), KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK)

        assertEquals("aXYbc", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(3, Global.getCursor().getColumn())
    }

    @Test
    fun metaV_pastesClipboardText() {
        "abc".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 1)
        EditorClipboard.setText("XY")

        dispatchShortcut(CodeEditor(), KeyEvent.VK_V, InputEvent.META_DOWN_MASK)

        assertEquals("aXYbc", Global.getCursor().getDocument().getLine(0).toString())
        assertEquals(3, Global.getCursor().getColumn())
    }

    private fun dispatchShortcut(editor: CodeEditor, keyCode: Int, modifiers: Int) {
        val event = KeyEvent(
            editor,
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            modifiers,
            keyCode,
            KeyEvent.CHAR_UNDEFINED
        )
        editor.keyListeners.forEach { it.keyPressed(event) }
    }
}
