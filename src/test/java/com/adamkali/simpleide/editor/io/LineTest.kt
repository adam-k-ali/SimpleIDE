package com.adamkali.simpleide.editor.io

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class LineTest {
    @Test
    fun tokenSpanAt_findsContainingTokenAndTokenEnd() {
        val line = Line()
        line.rewrite("int x = 10;")

        assertEquals(TokenSpan(0, 3), line.tokenSpanAt(0))
        assertEquals(TokenSpan(0, 3), line.tokenSpanAt(2))
        assertEquals(TokenSpan(0, 3), line.tokenSpanAt(3))
        assertEquals(TokenSpan(4, 5), line.tokenSpanAt(4))
        assertEquals(TokenSpan(10, 11), line.tokenSpanAt(10))
    }

    @Test
    fun tokenSpanAt_returnsNullInInteriorWhitespace() {
        val line = Line()
        line.rewrite("int  x")

        assertEquals(TokenSpan(0, 3), line.tokenSpanAt(3))
        assertNull(line.tokenSpanAt(4))
        assertEquals(TokenSpan(5, 6), line.tokenSpanAt(5))
    }
}
