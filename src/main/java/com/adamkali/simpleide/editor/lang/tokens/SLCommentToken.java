package com.adamkali.simpleide.editor.lang.tokens;

import com.adamkali.simpleide.preferences.EditorColors;

import java.awt.*;

/**
 * Token for single line comments
 */
public class SLCommentToken extends Token {
    public SLCommentToken(String text) {
        super(EditorColors.COMMENT_BG_COLOR, EditorColors.COMMENT_FG_COLOR, text);
    }
}
