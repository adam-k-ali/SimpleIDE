package com.adamkali.simpleide.browser.components

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.preferences.EditorColors
import com.adamkali.simpleide.project.SourcePackage
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingUtilities

class FolderButton(private val folder: SourcePackage) : JComponent() {
    /** Whether the folder is selected.  */
    var selected = false
        set(value) {
            if (field != value) {
                field = value
                repaint()
            }
        }

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

    var onPopup: ((MouseEvent) -> Unit)? = null

    private var hovered = false

    fun getText(): String {
        return folder.getName()
    }

    init {
        font = Global.getFont()
        isOpaque = true
        background = EditorColors.sidebarBackground()
        foreground = EditorColors.sidebarForeground()
        alignmentX = Component.LEFT_ALIGNMENT
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.isPopupTrigger || SwingUtilities.isRightMouseButton(e)) {
                    return
                }
                dropped = !dropped
                onDroppedChanged?.invoke()
            }

            override fun mousePressed(e: MouseEvent) {
                maybePopup(e)
            }

            override fun mouseReleased(e: MouseEvent) {
                maybePopup(e)
            }

            override fun mouseEntered(e: MouseEvent) {
                hovered = true
                repaint()
            }

            override fun mouseExited(e: MouseEvent) {
                hovered = false
                repaint()
            }

            private fun maybePopup(e: MouseEvent) {
                if (e.isPopupTrigger) {
                    onPopup?.invoke(e)
                }
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = ExplorerItemStyle.prepare(g)
        ExplorerItemStyle.paintBackground(g2, width, height, hovered, selected)
        ExplorerItemStyle.paintChevron(g2, level, height, dropped)
        ExplorerItemStyle.paintFolderIcon(g2, level, height, dropped)
        ExplorerItemStyle.paintLabel(g2, folder.getName(), level, height, hovered || selected)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(
            ExplorerItemStyle.preferredWidth(level, folder.getName()),
            ExplorerItemStyle.rowHeight()
        )
    }

    override fun getMinimumSize(): Dimension {
        return Dimension(80, ExplorerItemStyle.rowHeight())
    }

    override fun getMaximumSize(): Dimension {
        return Dimension(Integer.MAX_VALUE, preferredSize.height)
    }
}
