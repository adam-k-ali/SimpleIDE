package com.adamkali.simpleide.editor.lang.tokens.literal;

import com.adamkali.simpleide.preferences.EditorColors;

public class DoubleToken extends LiteralTokenType {
    public DoubleToken(String value) {
        super(EditorColors.PLAINTEXT_BG_COLOR, EditorColors.PLAINTEXT_FG_COLOR, value);
    }
}
