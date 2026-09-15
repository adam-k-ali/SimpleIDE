package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.theme.ThemeLoader
import com.adamkali.simpleide.preferences.ThemeData
import java.awt.Component
import java.awt.Dimension
import javax.swing.BoxLayout
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
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        loadTheme()

        frame = JFrame("SimpleIDE")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(800, 600)
        frame.setLocationRelativeTo(null)

        val homeScreen = HomeScreen()
        homeScreen.onProjectReady = { showEditor() }
        frame.add(homeScreen)
        frame.isVisible = true
    }

    private fun showEditor() {
        editorPanel = EditorPanel()
        statusPanel = StatusPanel()

        val container = JPanel()
        container.layout = BoxLayout(container, BoxLayout.Y_AXIS)
        editorPanel.alignmentX = Component.LEFT_ALIGNMENT
        statusPanel.alignmentX = Component.LEFT_ALIGNMENT
        editorPanel.maximumSize = Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)
        container.add(editorPanel)
        container.add(statusPanel)

        frame.contentPane.removeAll()
        frame.contentPane.add(container)
        frame.revalidate()
        frame.repaint()
        editorPanel.codeEditor.requestFocusInWindow()

        editorTimer?.stop()
        editorTimer = Timer(1000 / 60) {
            editorPanel.codeEditor.update()
        }
        editorTimer?.start()
    }
}
