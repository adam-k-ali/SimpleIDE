package com.adamkali.simpleide.editor.lang.tokens.literal;

import com.adamkali.simpleide.editor.lang.tokens.Token;

import java.awt.*;

public class LiteralTokenType extends Token {
    public LiteralTokenType(Color backgroundColor, Color foregroundColor, String text) {
        super(backgroundColor, foregroundColor, text);
    }
}
