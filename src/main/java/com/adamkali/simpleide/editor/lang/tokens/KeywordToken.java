package com.adamkali.simpleide.editor.lang.tokens;

import com.adamkali.simpleide.preferences.EditorColors;

import java.awt.*;

public class KeywordToken extends Token {
    public KeywordToken(String text) {
        super(EditorColors.KEYWORD_BG_COLOR, EditorColors.KEYWORD_FG_COLOR, text);
    }
}
