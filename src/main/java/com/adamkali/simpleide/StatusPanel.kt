package com.adamkali.simpleide

import com.adamkali.simpleide.editor.StatusBar
import javax.swing.JPanel

class StatusPanel : JPanel() {
    val statusBar: StatusBar = StatusBar()

    init {
        add(statusBar)
    }
}