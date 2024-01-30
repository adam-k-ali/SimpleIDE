package com.adamkali.simpleide.project.lang.tokens.literal

import com.adamkali.simpleide.preferences.EditorColors

class StringToken(text: String) : LiteralToken(text, EditorColors.STRING_BG_COLOR, EditorColors.STRING_FG_COLOR) {
}