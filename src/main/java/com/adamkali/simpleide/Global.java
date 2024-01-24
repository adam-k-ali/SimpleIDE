package com.adamkali.simpleide;

import com.adamkali.simpleide.editor.io.EditorCursor;
import com.adamkali.simpleide.editor.io.Document;

import java.awt.*;

public class Global {
    private static int margin_top = 10;
    private static int margin_left = 10;
    private static int line_num_width = 32;
    private static Font font = new Font("Monospaced", Font.PLAIN, 12);

    private static EditorCursor editorCursor = new EditorCursor(new Document());

    public static int getMarginTop() {
        return margin_top;
    }

    public static int getMarginLeft() {
        return margin_left;
    }

    public static int getLineNumWidth() {
        return line_num_width;
    }

    public static void setCursor(EditorCursor editorCursor) {
        Global.editorCursor = editorCursor;
    }

    public static EditorCursor getCursor() {
        return editorCursor;
    }

    public static int getLineHeight() {
        return Toolkit.getDefaultToolkit().getFontMetrics(font).getHeight();
    }

    public static Font getFont() {
        return font;
    }

    public static int getStringWidth(String string) {
        return Toolkit.getDefaultToolkit().getFontMetrics(font).stringWidth(string);
    }

}
