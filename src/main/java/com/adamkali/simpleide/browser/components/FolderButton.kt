package com.adamkali.simpleide.browser.components

import com.adamkali.simpleide.project.SourcePackage
import java.awt.Graphics
import javax.swing.JComponent

class FolderButton(val folder: SourcePackage) : JComponent() {
    /** Whether the folder is selected.  */
    var selected = false

    /** Whether the folder has been 'dropped' to show its contents.  */
    var dropped = false

    /** The level from the root folder. 0 if root.  */
    var level = 0

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        if (g == null) return

        val lineHeight = g.fontMetrics.height

        // Draw the folder name
        g.drawString(folder.getName(), 4 + level * 2, lineHeight / 2 + lineHeight / 4)
    }

}