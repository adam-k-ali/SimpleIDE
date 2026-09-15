package com.adamkali.simpleide.editor.io

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DocumentTest {
    @Test
    fun emptyFile_isOneEmptyLine_withoutTrailingNewline() {
        val document = Document()
        document.replaceText("")

        assertEquals(1, document.getLineCount())
        assertEquals("", document.getLine(0).toString())
        assertFalse(document.trailingNewline)
        assertEquals("", document.toText())
    }

    @Test
    fun trailingNewline_isNotAnExtraPaintedLine() {
        val document = Document()
        document.replaceText("hello\n")

        assertEquals(1, document.getLineCount())
        assertEquals("hello", document.getLine(0).toString())
        assertTrue(document.trailingNewline)
        assertEquals("hello\n", document.toText())
    }

    @Test
    fun fileThatIsOnlyANewline_isOneEmptyLine() {
        val document = Document()
        document.replaceText("\n")

        assertEquals(1, document.getLineCount())
        assertEquals("", document.getLine(0).toString())
        assertTrue(document.trailingNewline)
        assertEquals("\n", document.toText())
    }

    @Test
    fun crlf_normalizesToLf_onRoundTrip() {
        val document = Document()
        document.replaceText("a\r\nb\r\n")

        assertEquals(2, document.getLineCount())
        assertEquals("a", document.getLine(0).toString())
        assertEquals("b", document.getLine(1).toString())
        assertTrue(document.trailingNewline)
        assertEquals("a\nb\n", document.toText())
    }

    @Test
    fun lfRoundTrip_preservesMultipleLinesWithoutTrailingNewline() {
        val original = "package test;\npublic class Main {"
        val document = Document()
        document.replaceText(original)

        assertEquals(2, document.getLineCount())
        assertFalse(document.trailingNewline)
        assertEquals(original, document.toText())
    }

    @Test
    fun blankLineInTheMiddle_isPreserved() {
        val original = "a\n\nb\n"
        val document = Document()
        document.replaceText(original)

        assertEquals(3, document.getLineCount())
        assertEquals("a", document.getLine(0).toString())
        assertEquals("", document.getLine(1).toString())
        assertEquals("b", document.getLine(2).toString())
        assertEquals(original, document.toText())
    }
}
