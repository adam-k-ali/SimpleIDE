package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;

public class TypeCharacterAction extends Action {
    @Override
    public void execute(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("TypeCharacterAction requires one argument");
        }
        if (!(args[0] instanceof Character)) {
            throw new IllegalArgumentException("TypeCharacterAction requires a Character argument");
        }

        String textBeforeCursor = Global.getCursor().getTextBeforeCursor();
        String textAfterCursor = Global.getCursor().getTextAfterCursor();

        Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).rewrite(textBeforeCursor + args[0] + textAfterCursor);
        Global.getCursor().moveRight();
    }
}
