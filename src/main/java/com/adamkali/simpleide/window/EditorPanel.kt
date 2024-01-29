package com.adamkali.simpleide.window

import com.adamkali.simpleide.browser.ProjectBrowser
import com.adamkali.simpleide.editor.CodeEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JViewport

class EditorPanel : JPanel() {
    val projectBrowser: ProjectBrowser = ProjectBrowser()
    val codeEditor: CodeEditor = CodeEditor()

    init {
        this.layout = GridBagLayout()

        val constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.BOTH

        // Project Browser
        constraints.weightx = 0.2
        constraints.weighty = 1.0
        constraints.gridx = 0
        constraints.gridy = 0

        add(projectBrowser, constraints)

        // Code Editor
        val viewport = JViewport()
        viewport.view = codeEditor

        val scrollPane = JScrollPane()
        scrollPane.viewport = viewport
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS

        constraints.weightx = 0.8
        constraints.weighty = 1.0
        constraints.gridx = 1
        constraints.gridy = 0
        add(scrollPane, constraints)
    }
}