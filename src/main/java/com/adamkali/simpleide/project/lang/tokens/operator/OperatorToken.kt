package com.adamkali.simpleide.project.lang.tokens.operator

import com.adamkali.simpleide.preferences.EditorColors
import com.adamkali.simpleide.project.lang.tokens.Token

abstract class OperatorToken(text: String) :
    Token(text, EditorColors.PLAINTEXT_BG_COLOR, EditorColors.PLAINTEXT_FG_COLOR) {
}