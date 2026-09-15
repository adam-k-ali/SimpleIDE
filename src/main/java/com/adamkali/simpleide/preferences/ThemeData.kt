package com.adamkali.simpleide.preferences

import com.adamkali.simpleide.editor.io.theme.Theme
import com.adamkali.simpleide.editor.io.theme.ThemeProperty
import java.awt.Color

class ThemeData(private val theme: Theme) {
    val currentLineColor = getProperty("current_line_highlight")

    fun getProperty(key: String): ThemeProperty {
        if (!theme.properties.containsKey(key)) {
            throw IllegalArgumentException("Property $key not found")
        }
        return theme.properties.getValue(key)
    }

    fun editorBackground(): Color = background("editor", FALLBACK_EDITOR_BG)
    fun editorForeground(): Color = foreground("editor", FALLBACK_EDITOR_FG)
    fun gutterBackground(): Color = background("gutter", FALLBACK_EDITOR_BG)
    fun gutterForeground(): Color = foreground("gutter", FALLBACK_GUTTER_FG)
    fun sidebarBackground(): Color = background("sidebar", FALLBACK_SIDEBAR_BG)
    fun sidebarForeground(): Color = foreground("sidebar", FALLBACK_CHROME_FG)
    fun activityBarBackground(): Color = background("activity_bar", FALLBACK_ACTIVITY_BG)
    fun activityBarForeground(): Color = foreground("activity_bar", FALLBACK_CHROME_FG)
    fun statusBarBackground(): Color = background("status_bar", FALLBACK_ACCENT)
    fun statusBarForeground(): Color = foreground("status_bar", Color.WHITE)
    fun tabBackground(): Color = background("tab", FALLBACK_TAB_BG)
    fun tabForeground(): Color = foreground("tab", FALLBACK_CHROME_FG)
    fun accentColor(): Color = foreground("accent", FALLBACK_ACCENT)
    fun selectionColor(): Color = background("selection", FALLBACK_SELECTION)
    fun plaintextBackground(): Color = background("plaintext", FALLBACK_EDITOR_BG)
    fun plaintextForeground(): Color = foreground("plaintext", FALLBACK_EDITOR_FG)
    fun keywordBackground(): Color = background("keyword", FALLBACK_EDITOR_BG)
    fun keywordForeground(): Color = foreground("keyword", FALLBACK_KEYWORD)
    fun stringBackground(): Color = background("string", FALLBACK_EDITOR_BG)
    fun stringForeground(): Color = foreground("string", FALLBACK_STRING)
    fun commentBackground(): Color = background("comment", FALLBACK_EDITOR_BG)
    fun commentForeground(): Color = foreground("comment", FALLBACK_COMMENT)
    fun currentLineHighlight(): Color = foreground("current_line_highlight", FALLBACK_LINE_HIGHLIGHT)

    private fun foreground(key: String, fallback: Color): Color {
        return theme.properties[key]?.color?.foregroundColor() ?: fallback
    }

    private fun background(key: String, fallback: Color): Color {
        return theme.properties[key]?.color?.backgroundColor() ?: fallback
    }

    companion object {
        @JvmField val FALLBACK_EDITOR_BG = Color(30, 30, 30)
        @JvmField val FALLBACK_EDITOR_FG = Color(212, 212, 212)
        @JvmField val FALLBACK_GUTTER_FG = Color(133, 133, 133)
        @JvmField val FALLBACK_SIDEBAR_BG = Color(37, 37, 38)
        @JvmField val FALLBACK_ACTIVITY_BG = Color(24, 24, 24)
        @JvmField val FALLBACK_TAB_BG = Color(45, 45, 45)
        @JvmField val FALLBACK_CHROME_FG = Color(204, 204, 204)
        @JvmField val FALLBACK_ACCENT = Color(0, 122, 204)
        @JvmField val FALLBACK_SELECTION = Color(38, 79, 120)
        @JvmField val FALLBACK_KEYWORD = Color(86, 156, 214)
        @JvmField val FALLBACK_STRING = Color(206, 145, 120)
        @JvmField val FALLBACK_COMMENT = Color(106, 153, 85)
        @JvmField val FALLBACK_LINE_HIGHLIGHT = Color(42, 45, 46)
        @JvmField val FALLBACK_SIDEBAR_HOVER = Color(42, 45, 46)
        @JvmField val FALLBACK_SIDEBAR_SELECTION = Color(55, 55, 61)
    }
}
