package com.adamkali.simpleide.editor.lang.tokens;

import org.apache.commons.lang.StringEscapeUtils;

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
        if (text == null) {
            return 0;
        }
        return text.length();
    }

    public String getText() {
        return text;
    }

    /**
     * Returns the token in a readable string format
     * @return ClassName [text = "text"]
     */
    @Override
    public String toString() {
        if (text == null) {
            return this.getClass().getSimpleName();
        }

        return this.getClass().getSimpleName() + " [text = \"" + StringEscapeUtils.escapeJava(text) + "\"]";
    }
}
