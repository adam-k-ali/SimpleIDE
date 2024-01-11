package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;

public class BackspaceAction extends Action {
    @Override
    public void execute(Object... args) {
        System.out.println("BackspaceAction.execute()");
        // Don't allow backspace if we're on the first, empty line.
        if (Global.getCursor().getCurrentColumn() == 0 && Global.getCursor().getCurrentLine() == 0) {
            return;
        }

        String textBeforeCursor = Global.getCursor().getTextBeforeCursor();
        String textAfterCursor = Global.getCursor().getTextAfterCursor();

        if (textBeforeCursor.isEmpty()) {
            // Move textAfterCursor up one line, and delete the current line
            Global.getCursor().document.removeLine(Global.getCursor().getCurrentLine());
            Global.getCursor().moveUp();
            int prevLineLength = Global.getCursor().document.getLine(Global.getCursor().getCurrentLine()).length();
            Global.getCursor().document.getLine(Global.getCursor().getCurrentLine()).append(textAfterCursor);
            Global.getCursor().moveBy(prevLineLength, 0);
        } else {
            // Remove the last character from textBeforeCursor
            textBeforeCursor = textBeforeCursor.substring(0, textBeforeCursor.length() - 1);
            Global.getCursor().document.getLine(Global.getCursor().getCurrentLine()).rewrite(textBeforeCursor + textAfterCursor);
            Global.getCursor().moveLeft();
        }
    }
}
