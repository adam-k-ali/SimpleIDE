package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.theme.ThemeLoader
import com.adamkali.simpleide.preferences.ThemeData
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.UIManager

object AppWindow {
    private val frame: JFrame = JFrame("SimpleIDE")
    private val editorPanel: EditorPanel = EditorPanel()
    private val statusPanel: StatusPanel = StatusPanel()

    fun setTitle(title: String) {
        frame.title = "SimpleIDE - $title"

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    }

    fun loadTheme() {
        val theme = ThemeLoader.load("src/main/resources/preferences/editor-theme.json")
        Global.setTheme(ThemeData(theme))
    }

    fun run() {
        loadTheme()
        
        frame.setSize(800, 600)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setLocationRelativeTo(null)
        frame.isVisible = true

        val container = JPanel()
        container.layout = BoxLayout(container, BoxLayout.Y_AXIS)
        container.add(editorPanel)
        container.add(statusPanel)

        frame.add(container)

        frame.isVisible = true

        editorPanel.codeEditor.requestFocus()

        val thread: Thread = Thread {
            while (true) {
                editorPanel.codeEditor.update()
                Thread.sleep(1000 / 60)
            }
        }
        thread.start()
    }

}