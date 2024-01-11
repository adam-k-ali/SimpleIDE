package com.adamkali.simpleide.editor.lang.tokens;

import com.adamkali.simpleide.preferences.EditorColors;

public class UndefinedToken extends Token {
    public UndefinedToken(String text) {
        super(EditorColors.UNDEFINED_BG_COLOR, EditorColors.UNDEFINED_FG_COLOR, text);
        this.isValid = true;
    }
}
