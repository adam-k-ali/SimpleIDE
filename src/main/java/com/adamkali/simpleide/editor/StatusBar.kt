package com.adamkali.simpleide.editor

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.activity.CursorActivityListener
import com.adamkali.simpleide.editor.io.TextPosition
import com.adamkali.simpleide.preferences.EditorColors
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

/**
 * StatusBar is a component that displays
 * the current line and column number of the cursor
 */
class StatusBar : JComponent(), CursorActivityListener {
    /** The left margin of the StatusBar  */
    val MARGIN_LEFT = 8

    init {
        font = Global.getFont()
        isOpaque = true
        background = EditorColors.statusBarBackground()
        preferredSize = Dimension(0, STATUS_BAR_HEIGHT)
        minimumSize = Dimension(0, STATUS_BAR_HEIGHT)
        Global.getCursor().setActionListener(this)
    }

    /**
     * Paints the StatusBar
     * @param g the Graphics object to paint on
     */
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        if (g == null) return

        g.color = EditorColors.statusBarBackground()
        g.fillRect(0, 0, width, height)

        g.font = font
        val fm = g.fontMetrics
        val textY = (height + fm.ascent - fm.descent) / 2
        g.color = EditorColors.statusBarForeground()
        g.drawString("SimpleIDE", MARGIN_LEFT, textY)

        val position = formatCursorPosition(Global.getCursor().getLine(), Global.getCursor().getColumn())
        val positionWidth = fm.stringWidth(position)
        g.drawString(position, width - positionWidth - MARGIN_LEFT, textY)
    }

    companion object {
        @JvmField
        var STATUS_BAR_HEIGHT: Int = 22

        @JvmStatic
        fun formatCursorPosition(line: Int, column: Int): String {
            return String.format("Ln %d, Col %d", line + 1, column + 1)
        }
    }

    override fun onCursorMove(from: TextPosition, to: TextPosition) {
        repaint()
    }
}
