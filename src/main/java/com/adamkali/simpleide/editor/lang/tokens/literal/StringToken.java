package com.adamkali.simpleide.editor.lang.tokens.literal;

import com.adamkali.simpleide.preferences.EditorColors;

public class StringToken extends LiteralTokenType {
    public StringToken(String text) {
        super(EditorColors.STRING_BG_COLOR, EditorColors.STRING_FG_COLOR, text);
    }
}
