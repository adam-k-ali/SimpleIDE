package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;

public class DeleteAction extends Action {
    @Override
    public void execute(Object... args) {
        if (Global.getCursor().deleteSelection()) {
            return;
        }

        String textBeforeCursor = Global.getCursor().getTextBeforeCursor();
        String textAfterCursor = Global.getCursor().getTextAfterCursor();

        if (textAfterCursor.isEmpty()) {
            int line = Global.getCursor().getLine();
            if (line >= Global.getCursor().getDocument().getLineCount() - 1) {
                return;
            }
            String nextLineText = Global.getCursor().getDocument().getLine(line + 1).toString();
            Global.getCursor().getDocument().removeLine(line + 1);
            Global.getCursor().getDocument().getLine(line).append(nextLineText);
        } else {
            textAfterCursor = textAfterCursor.substring(1);
            Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).rewrite(textBeforeCursor + textAfterCursor);
        }
    }
}
