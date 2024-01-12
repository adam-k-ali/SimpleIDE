package com.adamkali.simpleide.editor.lang.tokens.keyword;

import com.adamkali.simpleide.editor.lang.tokens.Token;
import com.adamkali.simpleide.preferences.EditorColors;

public class KeywordToken extends Token {
    public KeywordToken(String text) {
        super(EditorColors.KEYWORD_BG_COLOR, EditorColors.KEYWORD_FG_COLOR, text);
    }
}
