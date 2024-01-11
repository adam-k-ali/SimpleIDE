package com.adamkali.simpleide.editor.lang.tokens.operator;

import com.adamkali.simpleide.editor.lang.tokens.Token;
import com.adamkali.simpleide.preferences.EditorColors;

public class OperatorToken extends Token {
    public OperatorToken(String value) {
        super(EditorColors.OPERATOR_BG_COLOR, EditorColors.OPERATOR_FG_COLOR, value);
    }
}
