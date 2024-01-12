package com.adamkali.simpleide.editor.lang.tokens.character;

import com.adamkali.simpleide.editor.lang.tokens.Token;
import com.adamkali.simpleide.preferences.EditorColors;

public class CommaToken extends Token {
    public CommaToken() {
        super(EditorColors.PLAINTEXT_BG_COLOR, EditorColors.PLAINTEXT_FG_COLOR, ",");
    }
}
