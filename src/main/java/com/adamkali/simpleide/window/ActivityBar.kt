package com.adamkali.simpleide.window

import com.adamkali.simpleide.preferences.EditorColors
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

/**
 * Narrow VS Code-style activity bar. Explorer is the only action; clicking it
 * toggles the sidebar.
 */
class ActivityBar : JComponent() {
    var explorerSelected = true
        set(value) {
            if (field != value) {
                field = value
                repaint()
            }
        }

    var onExplorerClicked: (() -> Unit)? = null

    init {
        isOpaque = true
        background = EditorColors.activityBarBackground()
        preferredSize = Dimension(WIDTH, 1)
        minimumSize = Dimension(WIDTH, 1)
        maximumSize = Dimension(WIDTH, Integer.MAX_VALUE)
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.y < ICON_SIZE) {
                    onExplorerClicked?.invoke()
                }
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2.color = EditorColors.activityBarBackground()
        g2.fillRect(0, 0, width, height)

        if (explorerSelected) {
            g2.color = EditorColors.accentColor()
            g2.fillRect(0, 0, ACCENT_STRIPE, ICON_SIZE)
        }

        val iconPad = 14
        val x = iconPad
        val y = iconPad
        val size = ICON_SIZE - iconPad * 2
        g2.color = if (explorerSelected) EditorColors.editorForeground() else EditorColors.activityBarForeground()
        // Folder glyph: tab + body.
        g2.drawRect(x, y + 4, size, size - 4)
        g2.drawLine(x, y + 4, x + 4, y)
        g2.drawLine(x + 4, y, x + size / 2, y)
        g2.drawLine(x + size / 2, y, x + size / 2 + 3, y + 4)
    }

    companion object {
        const val WIDTH = 48
        const val ICON_SIZE = 48
        const val ACCENT_STRIPE = 3
    }
}
