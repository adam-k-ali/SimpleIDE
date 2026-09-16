package com.adamkali.simpleide.browser.components

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.preferences.EditorColors
import com.adamkali.simpleide.project.SourceFile
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

class FileButton(private val name: String, val level: Int = 0) : JComponent() {
    var sourceFile: SourceFile? = null
        private set
    private var onFileClicked: ((SourceFile) -> Unit)? = null
    private var hovered = false

    constructor(file: SourceFile, level: Int) : this(file.getFileName(), level) {
        sourceFile = file
    }

    val fileName: String
        get() = name

    init {
        font = Global.getFont()
        isOpaque = true
        background = EditorColors.sidebarBackground()
        foreground = EditorColors.sidebarForeground()
        alignmentX = Component.LEFT_ALIGNMENT
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val file = sourceFile
                if (onFileClicked != null && file != null) {
                    onFileClicked?.invoke(file)
                }
            }

            override fun mouseEntered(e: MouseEvent) {
                hovered = true
                repaint()
            }

            override fun mouseExited(e: MouseEvent) {
                hovered = false
                repaint()
            }
        })
    }

    fun setOnFileClicked(onFileClicked: ((SourceFile) -> Unit)?) {
        this.onFileClicked = onFileClicked
    }

    private fun isActiveFile(): Boolean {
        val file = sourceFile ?: return false
        val openPath = OpenFile.path ?: return false
        return openPath == file.getPath()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val selected = isActiveFile()
        val g2 = ExplorerItemStyle.prepare(g)
        ExplorerItemStyle.paintBackground(g2, width, height, hovered, selected)
        ExplorerItemStyle.paintFileIcon(g2, level, height, name)
        ExplorerItemStyle.paintLabel(g2, name, level, height, hovered || selected)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(
            ExplorerItemStyle.preferredWidth(level, name),
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
