package com.adamkali.simpleide.editor.lang.tokens;

import java.awt.*;

/**
 * Token for single line comments
 */
public class SLCommentToken extends Token {
    public SLCommentToken(String text) {
        super(Color.WHITE, Color.GREEN);
        this.text = text;
    }
}
