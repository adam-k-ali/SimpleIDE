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
import java.awt.Container
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import javax.swing.UIManager

object AppWindow {
    private var frame: JFrame? = null
    private lateinit var contentHost: Container
    private lateinit var editorPanel: EditorPanel
    private lateinit var statusPanel: StatusPanel
    private var editorTimer: Timer? = null

    internal lateinit var homeScreen: HomeScreen
        private set
    internal lateinit var fileMenu: FileMenuBar
        private set
    internal var windowTitle: String = "SimpleIDE"
        private set

    fun setTitle(title: String) {
        windowTitle = "SimpleIDE - $title"
        frame?.title = windowTitle
    }

    fun loadTheme() {
        val theme = ThemeLoader.load("src/main/resources/preferences/editor-theme.json")
        Global.setTheme(ThemeData(theme))
    }

    fun run() {
        FlatDarkLaf.setup()
        UIManager.put("TitlePane.unifiedBackground", false)
        loadTheme()

        val created = JFrame("SimpleIDE")
        created.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        created.setSize(1100, 720)
        created.setLocationRelativeTo(null)
        installSession(created.contentPane)
        created.jMenuBar = fileMenu
        frame = created
        created.isVisible = true
    }

    internal fun startForTest(): JPanel {
        disposeForTest()
        val host = JPanel(BorderLayout())
        installSession(host)
        return host
    }

    internal fun disposeForTest() {
        editorTimer?.stop()
        editorTimer = null
        frame?.dispose()
        frame = null
        windowTitle = "SimpleIDE"
    }

    private fun installSession(host: Container) {
        contentHost = host
        homeScreen = HomeScreen()
        homeScreen.onProjectReady = { showEditor() }

        fileMenu = FileMenuBar()
        fileMenu.newProjectItem.addActionListener { homeScreen.newProject() }
        fileMenu.openProjectItem.addActionListener { homeScreen.openProject() }
        fileMenu.closeProjectItem.addActionListener { closeProject() }
        fileMenu.setProjectOpen(ProjectManager.activeProject != null)

        host.removeAll()
        host.add(homeScreen)
        windowTitle = "SimpleIDE"
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

        contentHost.removeAll()
        contentHost.add(container)
        contentHost.revalidate()
        contentHost.repaint()
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
        contentHost.removeAll()
        contentHost.add(homeScreen)
        windowTitle = "SimpleIDE"
        frame?.title = windowTitle
        fileMenu.setProjectOpen(false)
        homeScreen.refreshRecents()
        contentHost.revalidate()
        contentHost.repaint()
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
