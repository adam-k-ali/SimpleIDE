package com.adamkali.simpleide.project.lang.tokens

import com.adamkali.simpleide.preferences.EditorColors

class IdentifierToken(text: String) : Token(text, EditorColors.PLAINTEXT_BG_COLOR, EditorColors.PLAINTEXT_FG_COLOR) {
}