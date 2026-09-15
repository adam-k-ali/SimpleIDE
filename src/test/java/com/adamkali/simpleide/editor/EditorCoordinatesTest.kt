package com.adamkali.simpleide.editor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EditorCoordinatesTest {
    private val eightPx: (String) -> Int = { it.length * 8 }

    @Test
    fun columnAt_doesNotSkipColumnOne() {
        val origin = EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT
        assertEquals(0, EditorCoordinates.columnAt(origin, "Hello", eightPx))
        assertEquals(1, EditorCoordinates.columnAt(origin + 8, "Hello", eightPx))
        assertEquals(2, EditorCoordinates.columnAt(origin + 16, "Hello", eightPx))
    }

    @Test
    fun columnAt_snapsToNearestCharacter() {
        val origin = EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT
        assertEquals(0, EditorCoordinates.columnAt(origin + 3, "Hello", eightPx))
        assertEquals(1, EditorCoordinates.columnAt(origin + 5, "Hello", eightPx))
        assertEquals(5, EditorCoordinates.columnAt(origin + 40, "Hello", eightPx))
    }

    @Test
    fun columnAt_clampsToLineLength() {
        val origin = EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT
        assertEquals(2, EditorCoordinates.columnAt(origin + 800, "ab", eightPx))
    }

    @Test
    fun columnAt_treatsTabsAsFourSpaces() {
        val origin = EditorCoordinates.LINE_NUM_WIDTH + EditorCoordinates.MARGIN_LEFT
        val width = { text: String -> EditorCoordinates.toVisual(text).length * 8 }
        assertEquals(0, EditorCoordinates.columnAt(origin + 8, "\tab", width))
        assertEquals(1, EditorCoordinates.columnAt(origin + 32, "\tab", width))
    }

    @Test
    fun lineAt_clampsToDocument() {
        assertEquals(0, EditorCoordinates.lineAt(-10, 16, 3))
        assertEquals(0, EditorCoordinates.lineAt(EditorCoordinates.MARGIN_TOP, 16, 3))
        assertEquals(2, EditorCoordinates.lineAt(500, 16, 3))
    }

    @Test
    fun cursorX_matchesPaintedTabWidth() {
        val withTab = EditorCoordinates.cursorX("\t", eightPx)
        val withSpaces = EditorCoordinates.cursorX("    ", eightPx)
        assertEquals(withSpaces, withTab)
    }
}
