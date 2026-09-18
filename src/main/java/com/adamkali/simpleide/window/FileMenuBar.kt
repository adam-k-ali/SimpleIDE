package com.adamkali.simpleide.window

import java.awt.event.KeyEvent
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

/**
 * Frame menu with project session commands. Close Project is enabled only while a
 * project is open.
 */
class FileMenuBar : JMenuBar() {
    val newProjectItem = JMenuItem("New Project")
    val openProjectItem = JMenuItem("Open Project")
    val closeProjectItem = JMenuItem("Close Project")

    init {
        val fileMenu = JMenu("File")
        fileMenu.mnemonic = KeyEvent.VK_F
        newProjectItem.mnemonic = KeyEvent.VK_N
        openProjectItem.mnemonic = KeyEvent.VK_O
        closeProjectItem.mnemonic = KeyEvent.VK_C
        closeProjectItem.isEnabled = false

        fileMenu.add(newProjectItem)
        fileMenu.add(openProjectItem)
        fileMenu.addSeparator()
        fileMenu.add(closeProjectItem)
        add(fileMenu)
    }

    fun setProjectOpen(open: Boolean) {
        closeProjectItem.isEnabled = open
    }
}
