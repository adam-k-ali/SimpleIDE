package com.adamkali.simpleide.editor.tokens;

import java.awt.*;

public class NewLineToken extends Token {
    public NewLineToken() {
        super(Color.WHITE, Color.BLACK);
        this.text = "\n";
    }
}
