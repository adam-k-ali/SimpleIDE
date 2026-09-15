package com.adamkali.simpleide.browser.components

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.EditorCoordinates
import com.adamkali.simpleide.project.SourcePackage
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingConstants

class FolderButton(private val folder: SourcePackage) : JComponent(), SwingConstants {
    /** Whether the folder is selected.  */
    var selected = false

    /** Whether the folder has been 'dropped' to show its contents.  */
    var dropped = false
        set(value) {
            if (field != value) {
                field = value
                repaint()
            }
        }

    /** The level from the root folder. 0 if root.  */
    var level = 0

    var onDroppedChanged: (() -> Unit)? = null

    fun getText(): String {
        return folder.getName()
    }

    init {
        font = Global.getFont()
        isOpaque = true
        background = Color.WHITE
        foreground = Color.BLACK
        alignmentX = Component.LEFT_ALIGNMENT
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                dropped = !dropped
                onDroppedChanged?.invoke()
            }
        })
    }

    private fun drawArrow(g: Graphics, direction: Direction, x: Int, y: Int, width: Int, height: Int) {
        if (direction == Direction.DOWN) {
            g.drawLine(x, y, x + width / 2, y + height)
            g.drawLine(x + width, y, x + width / 2, y + height)
        } else {
            g.drawLine(x, y, x + width, y + height / 2)
            g.drawLine(x, y + height, x + width, y + height / 2)
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        g.color = if (selected) Color(230, 230, 255) else background
        g.fillRect(0, 0, width, height)

        val indent = EditorCoordinates.treeIndent(level)
        val arrowSize = 8
        val arrowX = 6 + indent
        val arrowY = (height - arrowSize) / 2
        val direction = if (dropped) Direction.DOWN else Direction.RIGHT

        g.color = foreground
        drawArrow(g, direction, arrowX, arrowY, arrowSize, arrowSize)

        val fm = g.fontMetrics
        val textY = (height + fm.ascent - fm.descent) / 2
        g.drawString(folder.getName(), arrowX + arrowSize + 6, textY)
    }

    override fun getPreferredSize(): Dimension {
        val indent = EditorCoordinates.treeIndent(level)
        val arrowSize = 8
        val textWidth = Global.getStringWidth(folder.getName())
        val width = 6 + indent + arrowSize + 6 + textWidth + 8
        return Dimension(width, Global.getLineHeight() + 6)
    }

    override fun getMinimumSize(): Dimension {
        return Dimension(80, Global.getLineHeight() + 6)
    }

    override fun getMaximumSize(): Dimension {
        return Dimension(Integer.MAX_VALUE, preferredSize.height)
    }

    private enum class Direction {
        RIGHT, DOWN
    }
}
