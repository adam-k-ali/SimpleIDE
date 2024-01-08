package com.adamkali.simpleide.editor.lang.tokens;

import java.awt.*;

public class OperatorToken extends Token {
    public OperatorToken(String text) {
        super(Color.WHITE, Color.BLACK);
        this.text = text;
    }
}
