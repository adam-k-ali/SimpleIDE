package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;

/**
 * Inserts a new line at the current cursor position.
 */
public class NewLineAction extends Action {
    @Override
    public void execute(Object... args) {
        String textBeforeCursor = Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).substring(0, Global.getCursor().getColumn());
        String textAfterCursor = Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).substring(Global.getCursor().getColumn(), Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).length());

        Global.getCursor().getDocument().newLine(Global.getCursor().getLine());
        if (!textAfterCursor.isEmpty()) {
            Global.getCursor().getDocument().getLine(Global.getCursor().getLine() + 1).append(textAfterCursor);
            Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).rewrite(textBeforeCursor);
        }
        Global.getCursor().moveDown();
        Global.getCursor().moveToStartOfLine();

        // Make the new line have the same indentation as the previous line
        int indent = Global.getCursor().getDocument().getLine(Global.getCursor().getLine() - 1).indentations();
        for (int i = 0; i < indent; i++) {
            Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).append(" ");
            Global.getCursor().moveRight();
        }
    }
}
