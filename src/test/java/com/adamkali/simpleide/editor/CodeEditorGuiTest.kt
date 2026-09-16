package com.adamkali.simpleide.editor

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorClipboard
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.TextPosition
import com.adamkali.simpleide.editor.io.UnsavedChoice
import com.adamkali.simpleide.editor.io.action.ActionsList
import com.adamkali.simpleide.editor.io.theme.ThemeLoader
import com.adamkali.simpleide.preferences.EditorColors
import com.adamkali.simpleide.preferences.ThemeData
import com.adamkali.simpleide.testsupport.GuiRender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.Dimension
import java.awt.Point
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import javax.swing.JScrollPane

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
            GuiRender.isNear(gutter, EditorColors.gutterBackground(), 8),
            "gutter pixel should stay the gutter color, but was $gutter"
        )
    }

    @Test
    fun currentLineHighlight_isVisibleBesideText_andTextStaysLight() {
        "Hello".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        ActionsList.NEW_LINE.execute()
        "World".forEach { ActionsList.TYPE_CHARACTER.execute(it) }
        Global.getCursor().moveTo(0, 0)

        val editor = CodeEditor()
        val image = GuiRender.render(editor, 500, 160)
        val lineHeight = Global.getLineHeight()
        val textX = EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT

        assertTrue(
            GuiRender.hasLightGlyph(image, textX, EditorCoordinates.MARGIN_TOP, textX + 80, EditorCoordinates.MARGIN_TOP + lineHeight),
            "text on the current line should paint as light glyphs"
        )

        val highlightSample = GuiRender.rgb(image, textX + 120, EditorCoordinates.lineTop(0, lineHeight) + lineHeight / 2)
        val otherLineSample = GuiRender.rgb(image, textX + 120, EditorCoordinates.lineTop(1, lineHeight) + lineHeight + 8)
        assertTrue(
            GuiRender.isNear(highlightSample, EditorColors.currentLineHighlight(), 12),
            "empty space on the current line should be tinted by the line highlight, but was $highlightSample"
        )
        assertTrue(
            GuiRender.isNear(otherLineSample, EditorColors.editorBackground(), 12),
            "empty space on a non-current line should remain the editor background, but was $otherLineSample"
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

    @Test
    fun shiftRight_selectsByCharacter() {
        Global.getCursor().getDocument().replaceText("abcd")
        Global.getCursor().moveTo(0, 1)

        dispatchShortcut(CodeEditor(), KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK)

        assertEquals("b", Global.getCursor().getSelectedText())
        assertEquals(2, Global.getCursor().getColumn())
    }

    @Test
    fun shiftDown_extendsSelectionToNextLine() {
        Global.getCursor().getDocument().replaceText("ab\ncd")
        Global.getCursor().moveTo(0, 1)

        dispatchShortcut(CodeEditor(), KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK)

        assertEquals("b\nc", Global.getCursor().getSelectedText())
        assertEquals(1, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
    }

    @Test
    fun altRight_movesByTokenWithoutSelecting() {
        Global.getCursor().getDocument().replaceText("int x = 10;")
        Global.getCursor().moveTo(0, 0)

        dispatchShortcut(CodeEditor(), KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK)

        assertEquals(3, Global.getCursor().getColumn())
        assertEquals(null, Global.getCursor().getSelectedText())
    }

    @Test
    fun ctrlAltRight_selectsByToken() {
        Global.getCursor().getDocument().replaceText("int x = 10;")
        Global.getCursor().moveTo(0, 0)

        dispatchShortcut(CodeEditor(), KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK or InputEvent.ALT_DOWN_MASK)

        assertEquals("int", Global.getCursor().getSelectedText())
        assertEquals(3, Global.getCursor().getColumn())
    }

    @Test
    fun metaAltRight_selectsByToken() {
        Global.getCursor().getDocument().replaceText("int x = 10;")
        Global.getCursor().moveTo(0, 0)

        dispatchShortcut(CodeEditor(), KeyEvent.VK_RIGHT, InputEvent.META_DOWN_MASK or InputEvent.ALT_DOWN_MASK)

        assertEquals("int", Global.getCursor().getSelectedText())
        assertEquals(3, Global.getCursor().getColumn())
    }

    @Test
    fun altUp_movesCurrentLineUpWithCaret() {
        Global.getCursor().getDocument().replaceText("first\nsecond")
        Global.getCursor().moveTo(1, 2)

        dispatchShortcut(CodeEditor(), KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK)

        assertEquals("second\nfirst", Global.getCursor().getDocument().toText())
        assertEquals(0, Global.getCursor().getLine())
        assertEquals(2, Global.getCursor().getColumn())
    }

    @Test
    fun bareRight_clearsSelectionThenMoves() {
        Global.getCursor().getDocument().replaceText("abcd")
        Global.getCursor().moveTo(0, 2)
        Global.getCursor().setSelection(TextPosition(0, 1), TextPosition(0, 2))

        dispatchShortcut(CodeEditor(), KeyEvent.VK_RIGHT, 0)

        assertEquals(null, Global.getCursor().getSelectedText())
        assertEquals(3, Global.getCursor().getColumn())
    }

    @Test
    fun arrowKeys_insideViewport_doNotScrollThePane() {
        Global.getCursor().getDocument().replaceText((0 until 40).joinToString("\n") { "abcdefghij" })
        Global.getCursor().moveTo(1, 0)

        val (editor, scroll) = editorInScrollPane(400, 150)
        val before = Point(scroll.viewport.viewPosition)

        editor.press(KeyEvent.VK_DOWN)
        editor.press(KeyEvent.VK_RIGHT)

        assertEquals(2, Global.getCursor().getLine())
        assertEquals(1, Global.getCursor().getColumn())
        assertEquals(before, scroll.viewport.viewPosition)
    }

    @Test
    fun arrowDown_atBottomEdge_scrollsViewport() {
        Global.getCursor().getDocument().replaceText((0 until 80).joinToString("\n") { "x" })

        val (editor, scroll) = editorInScrollPane(400, 120)
        val lineHeight = Global.getLineHeight()
        val extentH = scroll.viewport.extentSize.height
        assertTrue(extentH > lineHeight, "viewport should show at least one line")
        assertTrue(
            scroll.viewport.viewSize.height > extentH,
            "document should be taller than the viewport"
        )

        val edgeLine = 12
        val edgeBottom = EditorCoordinates.lineTop(edgeLine, lineHeight) + lineHeight
        val viewY = edgeBottom - extentH
        assertTrue(viewY > 0, "edge line should sit at the bottom of a scrolled viewport")
        Global.getCursor().moveTo(edgeLine, 0)
        scroll.viewport.viewPosition = Point(0, viewY)

        val visible = scroll.viewport.viewRect
        val nextBottom = EditorCoordinates.lineTop(edgeLine + 1, lineHeight) + lineHeight
        assertTrue(
            nextBottom > visible.y + visible.height,
            "the next line must extend past the visible area before pressing down"
        )

        val yBefore = scroll.viewport.viewPosition.y
        editor.press(KeyEvent.VK_DOWN)

        assertEquals(edgeLine + 1, Global.getCursor().getLine())
        assertTrue(
            scroll.viewport.viewPosition.y > yBefore,
            "viewport should pan down when the caret leaves the visible area"
        )
    }

    @Test
    fun arrowRight_atRightEdge_scrollsViewport() {
        Global.getCursor().getDocument().replaceText("a".repeat(200))
        Global.getCursor().moveTo(0, 0)

        val (editor, scroll) = editorInScrollPane(280, 120)
        val lastVisible = lastFullyVisibleColumn(scroll)
        val lineLength = Global.getCursor().getDocument().getLine(0).length()
        assertTrue(lastVisible < lineLength, "need characters past the right edge to pan")
        Global.getCursor().moveTo(0, lastVisible)
        val xBefore = scroll.viewport.viewPosition.x

        editor.press(KeyEvent.VK_RIGHT)

        assertEquals(lastVisible + 1, Global.getCursor().getColumn())
        assertTrue(
            scroll.viewport.viewPosition.x > xBefore,
            "viewport should pan right when the caret leaves the visible area"
        )
    }

    /**
     * Exposes [processKeyEvent] so tests can exercise consume vs JScrollPane
     * ancestor bindings without a focused, showing window (headless CI).
     */
    private class TestEditor : CodeEditor() {
        fun press(keyCode: Int) {
            processKeyEvent(
                KeyEvent(
                    this,
                    KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(),
                    0,
                    keyCode,
                    KeyEvent.CHAR_UNDEFINED
                )
            )
        }
    }

    private fun editorInScrollPane(width: Int, height: Int): Pair<TestEditor, JScrollPane> {
        val editor = TestEditor()
        val scroll = JScrollPane(editor)
        scroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        scroll.setSize(width, height)
        scroll.doLayout()
        editor.update()
        val viewSize = editor.preferredSize
        editor.size = viewSize
        scroll.viewport.viewSize = viewSize
        scroll.doLayout()
        scroll.viewport.viewSize = viewSize
        scroll.viewport.extentSize = Dimension(
            (width - 32).coerceAtLeast(80),
            (height - 32).coerceAtLeast(48)
        )
        scroll.viewport.viewPosition = Point(0, 0)
        return editor to scroll
    }

    private fun lastFullyVisibleColumn(scroll: JScrollPane): Int {
        val visible = scroll.viewport.viewRect
        val line = Global.getCursor().getLine()
        val text = Global.getCursor().getDocument().getLine(line).toString()
        var last = 0
        for (col in 0..text.length) {
            val x = EditorCoordinates.cursorX(text.substring(0, col), Global::getStringWidth)
            if (x >= visible.x && x + 1 <= visible.x + visible.width) {
                last = col
            }
        }
        return last
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
