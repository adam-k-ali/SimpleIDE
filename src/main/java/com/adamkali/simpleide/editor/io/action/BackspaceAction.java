package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;

public class BackspaceAction extends Action {
    @Override
    public void execute(Object... args) {
        // Don't allow backspace if we're on the first, empty line.
        if (Global.getCursor().getColumn() == 0 && Global.getCursor().getLine() == 0) {
            return;
        }

        String textBeforeCursor = Global.getCursor().getTextBeforeCursor();
        String textAfterCursor = Global.getCursor().getTextAfterCursor();

        if (textBeforeCursor.isEmpty()) {
            // Move textAfterCursor up one line, and delete the current line
            Global.getCursor().getDocument().removeLine(Global.getCursor().getLine());
            Global.getCursor().moveUp();
            int prevLineLength = Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).length();
            Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).append(textAfterCursor);
            Global.getCursor().moveBy(prevLineLength, 0);
        } else {
            // Remove the last character from textBeforeCursor
            textBeforeCursor = textBeforeCursor.substring(0, textBeforeCursor.length() - 1);
            Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).rewrite(textBeforeCursor + textAfterCursor);
            Global.getCursor().moveLeft();
        }
    }
}
