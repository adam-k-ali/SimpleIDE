package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.io.EditorClipboard;

public class CopyAction extends Action {
    @Override
    public void execute(Object... args) {
        String selected = Global.getCursor().getSelectedText();
        if (selected != null) {
            EditorClipboard.setText(selected);
            return;
        }

        String lineText = Global.getCursor().getDocument().getLine(Global.getCursor().getLine()).toString();
        EditorClipboard.setText(lineText + "\n");
    }
}
