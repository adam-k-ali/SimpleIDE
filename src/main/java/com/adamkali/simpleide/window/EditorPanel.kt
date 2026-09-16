package com.adamkali.simpleide.window

import com.adamkali.simpleide.browser.ProjectBrowser
import com.adamkali.simpleide.editor.CodeEditor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.preferences.EditorColors
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JViewport

class EditorPanel : JPanel(BorderLayout()) {
    val projectBrowser: ProjectBrowser = ProjectBrowser()
    val codeEditor: CodeEditor = CodeEditor()
    val activityBar: ActivityBar = ActivityBar()
    val editorTabBar: EditorTabBar = EditorTabBar()
    val splitPane: JSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
    val browserScroll: JScrollPane
    val editorScroll: JScrollPane

    var sidebarVisible = true
        private set

    private var lastDividerLocation = DEFAULT_SIDEBAR_WIDTH

    init {
        background = EditorColors.editorBackground()
        maximumSize = Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)

        browserScroll = JScrollPane(projectBrowser)
        browserScroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        browserScroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        browserScroll.preferredSize = Dimension(DEFAULT_SIDEBAR_WIDTH, 1)
        browserScroll.minimumSize = Dimension(120, 1)
        browserScroll.border = BorderFactory.createEmptyBorder()
        browserScroll.viewport.background = EditorColors.sidebarBackground()
        browserScroll.background = EditorColors.sidebarBackground()

        val viewport = JViewport()
        viewport.view = codeEditor
        viewport.background = EditorColors.editorBackground()

        editorScroll = JScrollPane()
        editorScroll.viewport = viewport
        editorScroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        editorScroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        editorScroll.border = BorderFactory.createEmptyBorder()
        editorScroll.background = EditorColors.editorBackground()

        val editorGroup = JPanel(BorderLayout())
        editorGroup.background = EditorColors.editorBackground()
        editorGroup.add(editorTabBar, BorderLayout.NORTH)
        editorGroup.add(editorScroll, BorderLayout.CENTER)

        splitPane.leftComponent = browserScroll
        splitPane.rightComponent = editorGroup
        splitPane.resizeWeight = 0.0
        splitPane.isContinuousLayout = true
        splitPane.dividerSize = 1
        splitPane.border = BorderFactory.createEmptyBorder()
        splitPane.setDividerLocation(DEFAULT_SIDEBAR_WIDTH)

        activityBar.onExplorerClicked = { toggleSidebar() }

        projectBrowser.onFileOpened = {
            editorTabBar.refresh()
            codeEditor.requestFocusInWindow()
        }
        OpenFile.onStateChanged = { editorTabBar.refresh() }

        add(activityBar, BorderLayout.WEST)
        add(splitPane, BorderLayout.CENTER)
    }

    fun toggleSidebar() {
        if (sidebarVisible) {
            lastDividerLocation = splitPane.dividerLocation.coerceAtLeast(120)
            browserScroll.isVisible = false
            sidebarVisible = false
            splitPane.dividerLocation = 0
        } else {
            browserScroll.isVisible = true
            sidebarVisible = true
            splitPane.dividerLocation = lastDividerLocation
        }
        activityBar.explorerSelected = sidebarVisible
        splitPane.revalidate()
        splitPane.repaint()
    }

    companion object {
        const val DEFAULT_SIDEBAR_WIDTH = 260
    }
}
