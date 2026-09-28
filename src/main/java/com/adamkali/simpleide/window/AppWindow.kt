package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.theme.ThemeLoader
import com.adamkali.simpleide.preferences.ThemeData
import com.formdev.flatlaf.FlatDarkLaf
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import javax.swing.UIManager
import kotlin.system.exitProcess

object AppWindow {
    private lateinit var frame: JFrame
    private lateinit var editorPanel: EditorPanel
    private lateinit var statusPanel: StatusPanel
    private var editorTimer: Timer? = null

    fun setTitle(title: String) {
        if (this::frame.isInitialized) {
            frame.title = "SimpleIDE - $title"
        }
    }

    fun loadTheme() {
        val theme = ThemeLoader.load("src/main/resources/preferences/editor-theme.json")
        Global.setTheme(ThemeData(theme))
    }

    fun run() {
        FlatDarkLaf.setup()
        UIManager.put("TitlePane.unifiedBackground", false)
        loadTheme()

        frame = JFrame("SimpleIDE")
        frame.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                if (handleWindowClosing { frame.dispose() }) {
                    exitProcess(0)
                }
            }
        })
        frame.setSize(1100, 720)
        frame.setLocationRelativeTo(null)

        val homeScreen = HomeScreen()
        homeScreen.onProjectReady = { showEditor() }
        frame.add(homeScreen)
        frame.isVisible = true
    }

    /**
     * Prompts for unsaved changes, then runs [closeWindow] if the caller may quit.
     * @return true if the window was closed
     */
    fun handleWindowClosing(closeWindow: () -> Unit): Boolean {
        if (!OpenFile.confirmIfDirty()) {
            return false
        }
        editorTimer?.stop()
        closeWindow()
        return true
    }

    private fun showEditor() {
        editorPanel = EditorPanel()
        statusPanel = StatusPanel()

        val container = JPanel(BorderLayout())
        container.add(editorPanel, BorderLayout.CENTER)
        container.add(statusPanel, BorderLayout.SOUTH)
        container.preferredSize = Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)

        frame.contentPane.removeAll()
        frame.contentPane.add(container)
        frame.revalidate()
        frame.repaint()
        editorPanel.codeEditor.requestFocusInWindow()

        editorTimer?.stop()
        editorTimer = Timer(1000 / 60) {
            editorPanel.codeEditor.update()
            editorPanel.editorTabBar.refresh()
        }
        editorTimer?.start()
    }
}
