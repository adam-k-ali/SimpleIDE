package com.adamkali.simpleide.preferences;

import com.adamkali.simpleide.Global;

import java.awt.Color;

/**
 * Theme-backed colors with Dark+ fallbacks so chrome and tokens paint
 * consistently even when tests skip {@link Global#setTheme}.
 */
public class EditorColors {
    public static Color SELECTION_COLOR = ThemeData.FALLBACK_SELECTION;

    public static Color PLAINTEXT_FG_COLOR = ThemeData.FALLBACK_EDITOR_FG;
    public static Color PLAINTEXT_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;

    public static Color OPERATOR_FG_COLOR = ThemeData.FALLBACK_EDITOR_FG;
    public static Color OPERATOR_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;

    public static Color COMMENT_FG_COLOR = ThemeData.FALLBACK_COMMENT;
    public static Color COMMENT_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;

    public static Color KEYWORD_FG_COLOR = ThemeData.FALLBACK_KEYWORD;
    public static Color KEYWORD_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;

    public static Color STRING_FG_COLOR = ThemeData.FALLBACK_STRING;
    public static Color STRING_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;

    public static void apply(ThemeData theme) {
        if (theme == null) {
            resetDefaults();
            return;
        }
        PLAINTEXT_FG_COLOR = theme.plaintextForeground();
        PLAINTEXT_BG_COLOR = theme.plaintextBackground();
        OPERATOR_FG_COLOR = theme.plaintextForeground();
        OPERATOR_BG_COLOR = theme.plaintextBackground();
        COMMENT_FG_COLOR = theme.commentForeground();
        COMMENT_BG_COLOR = theme.commentBackground();
        KEYWORD_FG_COLOR = theme.keywordForeground();
        KEYWORD_BG_COLOR = theme.keywordBackground();
        STRING_FG_COLOR = theme.stringForeground();
        STRING_BG_COLOR = theme.stringBackground();
        SELECTION_COLOR = theme.selectionColor();
    }

    public static void resetDefaults() {
        PLAINTEXT_FG_COLOR = ThemeData.FALLBACK_EDITOR_FG;
        PLAINTEXT_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;
        OPERATOR_FG_COLOR = ThemeData.FALLBACK_EDITOR_FG;
        OPERATOR_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;
        COMMENT_FG_COLOR = ThemeData.FALLBACK_COMMENT;
        COMMENT_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;
        KEYWORD_FG_COLOR = ThemeData.FALLBACK_KEYWORD;
        KEYWORD_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;
        STRING_FG_COLOR = ThemeData.FALLBACK_STRING;
        STRING_BG_COLOR = ThemeData.FALLBACK_EDITOR_BG;
        SELECTION_COLOR = ThemeData.FALLBACK_SELECTION;
    }

    public static Color editorBackground() {
        return theme() != null ? theme().editorBackground() : ThemeData.FALLBACK_EDITOR_BG;
    }

    public static Color editorForeground() {
        return theme() != null ? theme().editorForeground() : ThemeData.FALLBACK_EDITOR_FG;
    }

    public static Color gutterBackground() {
        return theme() != null ? theme().gutterBackground() : ThemeData.FALLBACK_EDITOR_BG;
    }

    public static Color gutterForeground() {
        return theme() != null ? theme().gutterForeground() : ThemeData.FALLBACK_GUTTER_FG;
    }

    public static Color sidebarBackground() {
        return theme() != null ? theme().sidebarBackground() : ThemeData.FALLBACK_SIDEBAR_BG;
    }

    public static Color sidebarForeground() {
        return theme() != null ? theme().sidebarForeground() : ThemeData.FALLBACK_CHROME_FG;
    }

    public static Color activityBarBackground() {
        return theme() != null ? theme().activityBarBackground() : ThemeData.FALLBACK_ACTIVITY_BG;
    }

    public static Color activityBarForeground() {
        return theme() != null ? theme().activityBarForeground() : ThemeData.FALLBACK_CHROME_FG;
    }

    public static Color statusBarBackground() {
        return theme() != null ? theme().statusBarBackground() : ThemeData.FALLBACK_ACCENT;
    }

    public static Color statusBarForeground() {
        return theme() != null ? theme().statusBarForeground() : Color.WHITE;
    }

    public static Color tabBackground() {
        return theme() != null ? theme().tabBackground() : ThemeData.FALLBACK_TAB_BG;
    }

    public static Color tabForeground() {
        return theme() != null ? theme().tabForeground() : ThemeData.FALLBACK_CHROME_FG;
    }

    public static Color accentColor() {
        return theme() != null ? theme().accentColor() : ThemeData.FALLBACK_ACCENT;
    }

    public static Color currentLineHighlight() {
        return theme() != null ? theme().currentLineHighlight() : ThemeData.FALLBACK_LINE_HIGHLIGHT;
    }

    public static Color sidebarHover() {
        return ThemeData.FALLBACK_SIDEBAR_HOVER;
    }

    public static Color sidebarSelection() {
        return ThemeData.FALLBACK_SIDEBAR_SELECTION;
    }

    private static ThemeData theme() {
        return Global.getTheme();
    }
}
