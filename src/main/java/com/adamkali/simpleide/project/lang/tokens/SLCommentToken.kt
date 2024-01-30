package com.adamkali.simpleide.project.lang.tokens

import com.adamkali.simpleide.preferences.EditorColors

class SLCommentToken(text: String) : Token(text, EditorColors.COMMENT_BG_COLOR, EditorColors.COMMENT_FG_COLOR) {
}