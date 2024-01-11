package com.adamkali.simpleide.editor.lang.tokens;

import java.awt.*;

public class TabToken extends Token {
    /**
     * <p>
     * Creates a TabToken with default colors and text.
     * The default colors are white text on a black background.
     * </p>
     * <p>
     * The tab is stored as four spaces.
     * </p>
     */
    public TabToken() {
        super(Color.WHITE, Color.BLACK, "    ");
    }
}
