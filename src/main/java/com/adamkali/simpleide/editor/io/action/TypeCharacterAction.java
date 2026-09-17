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

        Global.getCursor().deleteSelection();
        Global.getCursor().insertText(String.valueOf((Character) args[0]));
    }
}
