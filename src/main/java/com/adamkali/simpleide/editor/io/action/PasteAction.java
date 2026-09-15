package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.io.EditorClipboard;

public class PasteAction extends Action {
    @Override
    public void execute(Object... args) {
        String text = EditorClipboard.getText();
        if (text == null || text.isEmpty()) {
            return;
        }

        Global.getCursor().deleteSelection();
        Global.getCursor().insertText(text);
    }
}
