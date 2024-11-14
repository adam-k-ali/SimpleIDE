package com.adamkali.simpleide.browser.components

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.project.SourcePackage
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent
import javax.swing.SwingConstants

class FolderButton(private val folder: SourcePackage) : JComponent(), SwingConstants {
    /** Whether the folder is selected.  */
    var selected = false

    /** Whether the folder has been 'dropped' to show its contents.  */
    var dropped = false

    /** The level from the root folder. 0 if root.  */
    var level = 0

    fun getText(): String {
        return folder.getName()
    }

    init {
        // Set layout to Box_Layout.X_AXIS
//        layout = BoxLayout(this, BoxLayout.X_AXIS)
//        repaint()
    }

    private fun drawArrow(g: Graphics?, direction: Direction, x: Int, y: Int, width: Int, height: Int) {
        if (g == null) return

        // Draw a dropdown arrow
        if (direction == Direction.DOWN) {
            g.drawLine(x, y, x + width / 2, y + height)
            g.drawLine(x + width, y, x + width / 2, y + height)
        } else {
            g.drawLine(x, y, x + width, y + height / 2)
            g.drawLine(x, y + height, x + width, y + height / 2)
        }
    }

    override fun paint(g: Graphics?) {
        super.paint(g)
        if (g == null) return

        val lineHeight = g.fontMetrics.height

        // Draw a dropdown arrow
        drawArrow(g, Direction.UP, 5, height / 2 - 2, 4, 4)

        // Draw the folder name
        g.drawString(folder.getName(), 16, lineHeight / 2 + 3)

        // Draw red border to debug
        g.color = Color.RED;
        g.drawRect(0, 0, width - 1, height - 1)
        println(height)
    }

    /**
     * @return getPreferredSize(c)
     */
    override fun getMaximumSize(): Dimension {
        val maxWidth = this.parent.width
        val maxHeight = Global.getLineHeight() + 2
        return Dimension(maxWidth, maxHeight)
    }

    private enum class Direction {
        UP, DOWN
    }


}