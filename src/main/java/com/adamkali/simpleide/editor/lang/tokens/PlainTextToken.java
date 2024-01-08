package com.adamkali.simpleide.editor.lang.tokens;

import java.awt.*;

public class PlainTextToken extends Token {
    public PlainTextToken() {
        this("");
    }

    public PlainTextToken(String text) {
        super(Color.WHITE, Color.BLACK);
        this.text = text;
    }
}
