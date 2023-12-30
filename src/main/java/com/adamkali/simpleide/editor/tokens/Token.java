package com.adamkali.simpleide.editor.tokens;

import java.awt.*;

public abstract class Token {
    protected Color backgroundColor;
    protected Color foregroundColor;

    protected String text;

    public Token(Color backgroundColor, Color foregroundColor) {
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public int length() {
        return text.length();
    }
}
