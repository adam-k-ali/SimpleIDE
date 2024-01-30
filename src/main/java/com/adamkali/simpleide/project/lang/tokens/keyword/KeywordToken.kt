package com.adamkali.simpleide.project.lang.tokens.keyword

import com.adamkali.simpleide.preferences.EditorColors
import com.adamkali.simpleide.project.lang.tokens.Token

abstract class KeywordToken(text: String) : Token(text, EditorColors.KEYWORD_BG_COLOR, EditorColors.KEYWORD_FG_COLOR) {
}