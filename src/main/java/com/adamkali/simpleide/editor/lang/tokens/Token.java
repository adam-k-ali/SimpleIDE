package com.adamkali.simpleide.editor.lang.tokens;

import com.adamkali.simpleide.editor.lang.tokens.literal.LiteralTokenType;
import org.apache.commons.lang.StringEscapeUtils;

import java.awt.*;
import java.util.Objects;

public abstract class Token {
    protected Color backgroundColor;
    protected Color foregroundColor;
    protected boolean isValid = true;

    protected String text;

    public Token(Color backgroundColor, Color foregroundColor, String text) {
        Objects.requireNonNull(backgroundColor);
        Objects.requireNonNull(foregroundColor);
        Objects.requireNonNull(text);

        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.text = text;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    /**
     * Returns the length of the token
     *
     * @return the length of the token
     */
    public int length() {
        if (text == null) {
            return 0;
        }
        return text.length();
    }

    /**
     * Returns the text contained in the token
     *
     * @return the text contained in the token
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the token in a readable string format
     *
     * @return ClassName [text = "text"]
     */
    @Override
    public String toString() {
        if (this instanceof IdentifierToken || this instanceof LiteralTokenType) {
            return this.getClass().getSimpleName() + " [text = \"" + StringEscapeUtils.escapeJava(text) + "\"]";
        } else {
            return this.getClass().getSimpleName();
        }
    }
}
