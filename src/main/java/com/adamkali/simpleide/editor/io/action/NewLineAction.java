package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.lang.tokens.NewLineToken;

/**
 * Inserts a new line at the current cursor position.
 */
public class NewLineAction extends Action {
    @Override
    public void execute(Object... args) {
        String textBeforeCursor = Global.getCursor().document.getLine(Global.getCursor().getCurrentLine()).substring(0, Global.getCursor().getCurrentColumn());
        String textAfterCursor = Global.getCursor().document.getLine(Global.getCursor().getCurrentLine()).substring(Global.getCursor().getCurrentColumn(), Global.getCursor().document.getLine(Global.getCursor().getCurrentLine()).length());

        Global.getCursor().document.newLine(Global.getCursor().getCurrentLine());
        if (!textAfterCursor.isEmpty()) {
            Global.getCursor().document.getLine(Global.getCursor().getCurrentLine() + 1).append(textAfterCursor);
            Global.getCursor().document.getLine(Global.getCursor().getCurrentLine()).rewrite(textBeforeCursor);
        }
        Global.getCursor().moveDown();
        Global.getCursor().moveToStartOfLine();
    }
}
