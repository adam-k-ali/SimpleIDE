package com.adamkali.simpleide;

import com.adamkali.simpleide.editor.io.Cursor;

import java.awt.*;

public class Global {
    private static int margin_top = 10;
    private static int margin_left = 10;
    private static int line_num_width = 32;
    private static Font font = new Font("Monospaced", Font.PLAIN, 12);

    private static Cursor cursor = new Cursor();

    public static int getMarginTop() {
        return margin_top;
    }

    public static int getMarginLeft() {
        return margin_left;
    }

    public static int getLineNumWidth() {
        return line_num_width;
    }

    public static void setCursor(Cursor cursor) {
        Global.cursor = cursor;
    }

    public static Cursor getCursor() {
        return cursor;
    }

    public static int getLineHeight() {
        return Toolkit.getDefaultToolkit().getFontMetrics(font).getHeight();
    }

    public static int getStringWidth(String string) {
        return Toolkit.getDefaultToolkit().getFontMetrics(font).stringWidth(string);
    }

}
