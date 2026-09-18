package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.theme.ThemeLoader
import com.adamkali.simpleide.preferences.ThemeData
import com.adamkali.simpleide.project.ProjectManager
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

    internal lateinit var homeScreen: HomeScreen
        private set
    internal lateinit var fileMenu: FileMenuBar
        private set

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

        val created = createFrame()
        created.isVisible = true
    }

    internal fun startForTest(): JFrame {
        disposeForTest()
        return createFrame()
    }

    internal fun disposeForTest() {
        editorTimer?.stop()
        editorTimer = null
        if (this::frame.isInitialized) {
            frame.dispose()
        }
    }

    private fun createFrame(): JFrame {
        frame = JFrame("SimpleIDE")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(1100, 720)
        frame.setLocationRelativeTo(null)

        homeScreen = HomeScreen()
        homeScreen.onProjectReady = { showEditor() }

        fileMenu = FileMenuBar()
        fileMenu.newProjectItem.addActionListener { homeScreen.newProject() }
        fileMenu.openProjectItem.addActionListener { homeScreen.openProject() }
        fileMenu.closeProjectItem.addActionListener { closeProject() }
        fileMenu.setProjectOpen(ProjectManager.activeProject != null)
        frame.jMenuBar = fileMenu

        frame.add(homeScreen)
        return frame
    }

    private fun showEditor() {
        disposeEditorUi()
        OpenFile.reset()
        Global.setCursor(EditorCursor(Document(), 0, 0))

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
        fileMenu.setProjectOpen(true)

        editorTimer = Timer(1000 / 60) {
            editorPanel.codeEditor.update()
            editorPanel.editorTabBar.refresh()
        }
        editorTimer?.start()
    }

    private fun showHome() {
        editorTimer?.stop()
        editorTimer = null
        frame.contentPane.removeAll()
        frame.contentPane.add(homeScreen)
        frame.title = "SimpleIDE"
        fileMenu.setProjectOpen(false)
        homeScreen.refreshRecents()
        frame.revalidate()
        frame.repaint()
    }

    private fun closeProject() {
        if (!OpenFile.confirmIfDirty()) {
            return
        }
        disposeEditorUi()
        OpenFile.reset()
        Global.setCursor(EditorCursor(Document(), 0, 0))
        ProjectManager.reset()
        showHome()
    }

    private fun disposeEditorUi() {
        editorTimer?.stop()
        editorTimer = null
        if (this::editorPanel.isInitialized) {
            ProjectManager.deregisterCallback(editorPanel.projectBrowser)
        }
    }
}
