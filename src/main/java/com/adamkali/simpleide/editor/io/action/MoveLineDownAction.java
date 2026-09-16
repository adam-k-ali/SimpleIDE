package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.io.EditorCursor;

public class MoveLineDownAction extends Action {
    @Override
    public void execute(Object... args) {
        EditorCursor cursor = Global.getCursor();
        int line = cursor.getLine();
        if (line >= cursor.getDocument().getLineCount() - 1) {
            return;
        }

        cursor.getDocument().swapLines(line, line + 1);
        cursor.clearSelection();
        cursor.moveTo(line + 1, cursor.getColumn());
    }
}
