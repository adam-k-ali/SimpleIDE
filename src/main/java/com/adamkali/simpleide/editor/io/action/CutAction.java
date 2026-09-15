package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.io.EditorCursor;

public class CutAction extends Action {
    @Override
    public void execute(Object... args) {
        EditorCursor cursor = Global.getCursor();
        boolean hadSelection = cursor.getSelectedText() != null;
        ActionsList.COPY.execute();

        if (hadSelection) {
            cursor.deleteSelection();
            return;
        }

        int line = cursor.getLine();
        int column = cursor.getColumn();
        if (cursor.getDocument().getLineCount() == 1) {
            cursor.getDocument().getLine(0).rewrite("");
            cursor.moveTo(0, 0);
            return;
        }

        cursor.getDocument().removeLine(line);
        int newLine = Math.min(line, cursor.getDocument().getLineCount() - 1);
        int newColumn = Math.min(column, cursor.getDocument().getLine(newLine).length());
        cursor.moveTo(newLine, newColumn);
    }
}
