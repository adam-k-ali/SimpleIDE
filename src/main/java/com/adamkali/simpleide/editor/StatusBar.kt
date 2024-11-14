package com.adamkali.simpleide.editor

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.activity.CursorActivityListener
import com.adamkali.simpleide.editor.io.TextPosition
import java.awt.Color
import java.awt.Graphics
import javax.swing.JComponent

/**
 * StatusBar is a component that displays
 * the current line and column number of the cursor
 */
class StatusBar : JComponent(), CursorActivityListener {
    /** The height of the StatusBar  */
    val STATUS_BAR_HEIGHT = 32;

    /** The left margin of the StatusBar  */
    val MARGIN_LEFT = 8;

    /** The top margin of the StatusBar  */
    val MARGIN_TOP = 4

    init {
        font = Global.getFont()
        Global.getCursor().setActionListener(this)
    }

    /**
     * Paints the StatusBar
     * @param g the Graphics object to paint on
     */
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        if (g == null) return

        val lineHeight = Global.getLineHeight()

        g.color = Color.LIGHT_GRAY

        g.fillRect(0, 0, width, height)
        g.color = Color.BLACK
        g.drawString("SimpleIDE", MARGIN_LEFT, MARGIN_TOP + lineHeight / 2)
        g.drawString(String.format("Line: %d, Column: %d", Global.getCursor().getLine(), Global.getCursor().getColumn()), width - 200, MARGIN_TOP + lineHeight / 2)
    }

    companion object {
        @JvmField
        var STATUS_BAR_HEIGHT: Int = 32
    }

    override fun onCursorMove(from: TextPosition, to: TextPosition) {
        repaint()
    }
}