package com.adamkali.simpleide.activity

import com.adamkali.simpleide.editor.io.TextPosition

interface CursorActivityListener {
    fun onCursorMove(from: TextPosition, to: TextPosition)
}