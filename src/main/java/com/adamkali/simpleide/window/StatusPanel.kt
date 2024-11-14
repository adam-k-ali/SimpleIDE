package com.adamkali.simpleide.window

import com.adamkali.simpleide.editor.StatusBar
import java.awt.Dimension
import javax.swing.JPanel


class StatusPanel : JPanel() {
    val statusBar: StatusBar = StatusBar()

    init {
        statusBar.preferredSize = Dimension(800, StatusBar.STATUS_BAR_HEIGHT)
        add(statusBar)
    }
}