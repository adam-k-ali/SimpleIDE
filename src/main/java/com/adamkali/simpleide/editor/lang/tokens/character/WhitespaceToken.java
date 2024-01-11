package com.adamkali.simpleide.editor.lang.tokens.character;

import com.adamkali.simpleide.editor.lang.tokens.Token;

import java.awt.*;

public class WhitespaceToken extends Token {
    public WhitespaceToken() {
        super(Color.WHITE, Color.BLACK, " ");
    }
}
