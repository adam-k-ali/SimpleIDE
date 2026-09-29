package com.adamkali.simpleide.window

import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.preferences.EditorColors
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent

/**
 * Single-file editor tab strip. Shows the open buffer name and a dirty marker.
 */
class EditorTabBar : JComponent() {
    init {
        isOpaque = true
        background = EditorColors.tabBackground()
        preferredSize = Dimension(0, TAB_HEIGHT)
        minimumSize = Dimension(0, TAB_HEIGHT)
        maximumSize = Dimension(Integer.MAX_VALUE, TAB_HEIGHT)
    }

    fun displayedTitle(): String {
        val name = OpenFile.path?.fileName?.toString() ?: "Untitled"
        return if (OpenFile.isDirty()) "● $name" else name
    }

    fun refresh() {
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        g2.color = EditorColors.tabBackground()
        g2.fillRect(0, 0, width, height)

        val title = displayedTitle()
        val fm = g2.fontMetrics
        val tabWidth = (fm.stringWidth(title) + TAB_PAD * 2).coerceAtLeast(120).coerceAtMost(width)
        val tabHeight = height

        g2.color = EditorColors.editorBackground()
        g2.fillRect(0, 0, tabWidth, tabHeight)

        g2.color = EditorColors.accentColor()
        g2.fillRect(0, height - ACCENT_UNDERLINE, tabWidth, ACCENT_UNDERLINE)

        g2.color = EditorColors.editorForeground()
        val textY = (height + fm.ascent - fm.descent) / 2
        g2.drawString(title, TAB_PAD, textY)
    }

    companion object {
        const val TAB_HEIGHT = 35
        const val TAB_PAD = 14
        const val ACCENT_UNDERLINE = 2
    }
}
