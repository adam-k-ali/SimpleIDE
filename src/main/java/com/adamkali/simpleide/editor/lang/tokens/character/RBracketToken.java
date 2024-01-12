package com.adamkali.simpleide.editor.lang.tokens.character;

import com.adamkali.simpleide.editor.lang.tokens.Token;
import com.adamkali.simpleide.preferences.EditorColors;

public class RBracketToken extends Token {
    public RBracketToken() {
        super(EditorColors.PLAINTEXT_BG_COLOR, EditorColors.PLAINTEXT_FG_COLOR, "]");
    }
}
