package com.adamkali.simpleide.editor.lang.tokens;

import java.awt.*;

public class KeywordToken extends Token {
    public KeywordToken(String text) {
        super(Color.WHITE, Color.BLACK);
        this.text = text;
    }
}
