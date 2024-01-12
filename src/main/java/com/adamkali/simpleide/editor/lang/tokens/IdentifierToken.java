package com.adamkali.simpleide.editor.lang.tokens;

import com.adamkali.simpleide.preferences.EditorColors;

public class IdentifierToken extends Token {
    public IdentifierToken(String text) {
        super(EditorColors.PLAINTEXT_BG_COLOR, EditorColors.PLAINTEXT_FG_COLOR, text);
        this.isValid = true;
    }
}
