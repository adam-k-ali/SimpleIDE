package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.theme.ThemeLoader
import com.adamkali.simpleide.preferences.ThemeData
import com.formdev.flatlaf.FlatDarkLaf
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import javax.swing.UIManager

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
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(1100, 720)
        frame.setLocationRelativeTo(null)

        val homeScreen = HomeScreen()
        homeScreen.onProjectReady = { showEditor() }
        frame.add(homeScreen)
        frame.isVisible = true
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
