package com.adamkali.simpleide.editor.io.action;

import com.adamkali.simpleide.editor.io.OpenFile;

public class SaveAsAction extends Action {
    @Override
    public void execute(Object... args) {
        OpenFile.INSTANCE.saveAs();
    }
}
