package com.adamkali.simpleide.editor;

import java.util.function.ToIntFunction;

/**
 * Pixel and character coordinate mapping for the code editor.
 * Keeps mouse hit-testing, cursor placement, and painted text aligned.
 */
public final class EditorCoordinates {
    public static final int LINE_NUM_WIDTH = 64;
    public static final int MARGIN_LEFT = 8;
    public static final int MARGIN_TOP = 4;
    public static final int CURSOR_OFFSET_X = 0;
    public static final int CURSOR_OFFSET_Y = 4;
    public static final int CURSOR_HEIGHT = 16;
    public static final int INDENT_PX = 12;

    private EditorCoordinates() {
    }

    /**
     * Expands tabs to four spaces so measurements match what {@code CodeEditor} paints.
     */
    public static String toVisual(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\t", "    ");
    }

    public static int visualWidth(String text, ToIntFunction<String> stringWidth) {
        return stringWidth.applyAsInt(toVisual(text));
    }

    public static int lineAt(int y, int lineHeight, int lineCount) {
        if (lineHeight <= 0 || lineCount <= 0) {
            return 0;
        }
        int line = (y - MARGIN_TOP) / lineHeight;
        if (line < 0) {
            return 0;
        }
        return Math.min(line, lineCount - 1);
    }

    /**
     * Maps a mouse x coordinate to the closest character column in {@code lineText}.
     */
    public static int columnAt(int x, String lineText, ToIntFunction<String> stringWidth) {
        if (lineText == null) {
            lineText = "";
        }
        int xp = x - LINE_NUM_WIDTH - MARGIN_LEFT;
        if (xp <= 0) {
            return 0;
        }

        int closest = 0;
        int closestDist = Integer.MAX_VALUE;
        for (int i = 0; i <= lineText.length(); i++) {
            int width = visualWidth(lineText.substring(0, i), stringWidth);
            int dist = Math.abs(width - xp);
            if (dist < closestDist) {
                closestDist = dist;
                closest = i;
            }
            if (width >= xp) {
                break;
            }
        }
        return closest;
    }

    public static int cursorX(String textBeforeCursor, ToIntFunction<String> stringWidth) {
        return LINE_NUM_WIDTH + MARGIN_LEFT + visualWidth(textBeforeCursor, stringWidth) + CURSOR_OFFSET_X;
    }

    public static int cursorY(int line, int lineHeight) {
        return MARGIN_TOP + line * lineHeight + CURSOR_OFFSET_Y;
    }

    public static int lineTop(int line, int lineHeight) {
        return MARGIN_TOP + line * lineHeight + 2;
    }

    public static int textAreaX() {
        return LINE_NUM_WIDTH;
    }

    public static int treeIndent(int level) {
        return Math.max(0, level) * INDENT_PX;
    }
}
