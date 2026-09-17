package com.adamkali.simpleide.browser.components

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.EditorCoordinates
import com.adamkali.simpleide.preferences.EditorColors
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.RenderingHints

/**
 * Shared metrics and painting for explorer folder/file rows so names, icons,
 * and hover/selection chrome stay aligned.
 */
object ExplorerItemStyle {
    const val ROW_EXTRA_HEIGHT = 10
    const val ROW_PAD_X = 6
    const val CHEVRON_SIZE = 8
    const val CHEVRON_ICON_GAP = 4
    const val ICON_SIZE = 12
    const val ICON_LABEL_GAP = 6
    const val TRAILING_PAD = 8
    const val HIGHLIGHT_INSET = 2
    const val CORNER = 4
    const val ACCENT_WIDTH = 2

    val FOLDER_FILL: Color = Color(220, 180, 80)
    val FOLDER_TAB: Color = Color(232, 198, 110)

    fun rowHeight(): Int = Global.getLineHeight() + ROW_EXTRA_HEIGHT

    fun indentX(level: Int): Int = ROW_PAD_X + EditorCoordinates.treeIndent(level)

    fun chevronX(level: Int): Int = indentX(level)

    fun iconX(level: Int): Int = chevronX(level) + CHEVRON_SIZE + CHEVRON_ICON_GAP

    fun labelX(level: Int): Int = iconX(level) + ICON_SIZE + ICON_LABEL_GAP

    fun preferredWidth(level: Int, text: String): Int {
        return labelX(level) + Global.getStringWidth(text) + TRAILING_PAD
    }

    fun centeredY(height: Int, size: Int): Int = (height - size) / 2

    fun prepare(g: Graphics): Graphics2D {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.font = Global.getFont()
        return g2
    }

    fun paintBackground(g2: Graphics2D, width: Int, height: Int, hovered: Boolean, selected: Boolean) {
        g2.color = EditorColors.sidebarBackground()
        g2.fillRect(0, 0, width, height)

        if (selected || hovered) {
            g2.color = if (selected) EditorColors.sidebarSelection() else EditorColors.sidebarHover()
            g2.fillRoundRect(
                HIGHLIGHT_INSET,
                HIGHLIGHT_INSET,
                (width - HIGHLIGHT_INSET * 2).coerceAtLeast(0),
                (height - HIGHLIGHT_INSET * 2).coerceAtLeast(0),
                CORNER * 2,
                CORNER * 2
            )
        }

        if (selected) {
            g2.color = EditorColors.accentColor()
            g2.fillRect(0, 0, ACCENT_WIDTH, height)
        }
    }

    fun paintChevron(g2: Graphics2D, level: Int, height: Int, expanded: Boolean) {
        val x = chevronX(level)
        val y = centeredY(height, CHEVRON_SIZE)
        val w = CHEVRON_SIZE
        val h = CHEVRON_SIZE
        val triangle = if (expanded) {
            Polygon(
                intArrayOf(x, x + w, x + w / 2),
                intArrayOf(y + 1, y + 1, y + h - 1),
                3
            )
        } else {
            Polygon(
                intArrayOf(x + 1, x + 1, x + w - 1),
                intArrayOf(y, y + h, y + h / 2),
                3
            )
        }
        g2.color = EditorColors.gutterForeground()
        g2.fillPolygon(triangle)
    }

    fun paintFolderIcon(g2: Graphics2D, level: Int, height: Int, expanded: Boolean) {
        val x = iconX(level)
        val y = centeredY(height, ICON_SIZE)
        val size = ICON_SIZE
        val tabHeight = 5
        val tabWidth = size / 2 + if (expanded) 2 else 1

        g2.color = FOLDER_TAB
        g2.fillRoundRect(x, y, tabWidth, tabHeight, 2, 2)
        g2.color = FOLDER_FILL
        g2.fillRoundRect(x, y + 3, size, size - 3, 2, 2)
    }

    fun paintFileIcon(g2: Graphics2D, level: Int, height: Int, fileName: String) {
        val x = iconX(level)
        val y = centeredY(height, ICON_SIZE)
        val size = ICON_SIZE
        val fold = 4
        val color = fileIconColor(fileName)
        val page = Polygon(
            intArrayOf(x, x + size - fold, x + size, x + size, x),
            intArrayOf(y, y, y + fold, y + size, y + size),
            5
        )
        g2.color = color
        g2.fillPolygon(page)
        g2.color = color.brighter()
        g2.fillPolygon(
            Polygon(
                intArrayOf(x + size - fold, x + size, x + size - fold),
                intArrayOf(y, y + fold, y + fold),
                3
            )
        )
    }

    fun paintLabel(g2: Graphics2D, text: String, level: Int, height: Int, emphasized: Boolean) {
        val fm = g2.fontMetrics
        val textY = (height + fm.ascent - fm.descent) / 2
        g2.color = if (emphasized) EditorColors.editorForeground() else EditorColors.sidebarForeground()
        g2.drawString(text, labelX(level), textY)
    }

    fun fileIconColor(fileName: String): Color {
        return if (fileName.endsWith(".java", ignoreCase = true)) {
            EditorColors.KEYWORD_FG_COLOR
        } else {
            EditorColors.sidebarForeground()
        }
    }
}
