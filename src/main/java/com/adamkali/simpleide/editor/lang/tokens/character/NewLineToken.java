package com.adamkali.simpleide.editor.lang.tokens.character;

import com.adamkali.simpleide.editor.lang.tokens.Token;

import java.awt.*;

public class NewLineToken extends Token {
    public NewLineToken() {
        super(Color.WHITE, Color.BLACK, "\n");
    }
}
