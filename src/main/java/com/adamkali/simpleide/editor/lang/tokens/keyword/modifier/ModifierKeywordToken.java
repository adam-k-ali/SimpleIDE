package com.adamkali.simpleide.editor.lang.tokens.keyword.modifier;

import com.adamkali.simpleide.editor.lang.tokens.keyword.KeywordToken;

/**
 * Base class for all Class, Method, and Variable modifiers
 */
public class ModifierKeywordToken extends KeywordToken {
    public ModifierKeywordToken(String value) {
        super(value);
    }
}
