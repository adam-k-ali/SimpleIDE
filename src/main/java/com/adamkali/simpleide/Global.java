package com.adamkali.simpleide;

import com.adamkali.simpleide.editor.io.Document;
import com.adamkali.simpleide.preferences.ThemeData;
import com.adamkali.simpleide.editor.io.EditorCursor;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Global {
    private static int margin_top = 10;
    private static int margin_left = 10;
    private static int line_num_width = 32;
    private static Font font = new Font("Monospaced", Font.PLAIN, 12);
    private static ThemeData theme;
    private static FontMetrics fontMetrics;

    private static EditorCursor editorCursor = new EditorCursor(new Document(), 0, 0);

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
        return metrics().getHeight();
    }

    public static Font getFont() {
        return font;
    }

    public static int getStringWidth(String string) {
        if (string == null) {
            return 0;
        }
        return metrics().stringWidth(string);
    }

    public static void setTheme(ThemeData theme) {
        Global.theme = theme;
    }

    public static ThemeData getTheme() {
        return theme;
    }

    /**
     * Font metrics via an off-screen image so height/width stay available in headless tests.
     */
    private static FontMetrics metrics() {
        if (fontMetrics == null) {
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            fontMetrics = graphics.getFontMetrics(font);
            graphics.dispose();
        }
        return fontMetrics;
    }
}
