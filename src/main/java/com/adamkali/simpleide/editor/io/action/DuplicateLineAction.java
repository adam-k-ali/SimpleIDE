package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.io.Document;
import com.adamkali.simpleide.editor.io.EditorCursor;

public class DuplicateLineAction extends Action {
    @Override
    public void execute(Object... args) {
        if (Global.getCursor().getSelectedText() != null) {
            return;
        }

        String lineText = Global.getCursor().document.getLine(Global.getCursor().getCurrentLine()).toString();
        Document.Line line = new Document.Line();
        line.rewrite(lineText);
        Global.getCursor().document.insertLine(Global.getCursor().getCurrentLine() + 1, line);
    }
}
