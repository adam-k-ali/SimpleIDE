package com.adamkali.simpleide.window

import com.adamkali.simpleide.editor.StatusBar
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel


class StatusPanel : JPanel(BorderLayout()) {
    val statusBar: StatusBar = StatusBar()

    init {
        val height = StatusBar.STATUS_BAR_HEIGHT
        preferredSize = Dimension(0, height)
        minimumSize = Dimension(0, height)
        maximumSize = Dimension(Integer.MAX_VALUE, height)
        add(statusBar, BorderLayout.CENTER)
    }
}
