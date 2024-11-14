package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.io.Line;

public class DuplicateLineAction extends Action {
    @Override
    public void execute(Object... args) {
        if (Global.getCursor().getSelectedText() != null) {
            return;
        }

        String lineText = Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).toString();
        Line line = new Line();
        line.rewrite(lineText);
        Global.getCursor().getDocument().insertLine(Global.getCursor().getLine() + 1, line);
    }
}
