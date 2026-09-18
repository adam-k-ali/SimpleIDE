package com.adamkali.simpleide.browser

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.activity.ProjectActivityListener
import com.adamkali.simpleide.browser.components.FileButton
import com.adamkali.simpleide.browser.components.FolderButton
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.preferences.EditorColors
import com.adamkali.simpleide.project.Project
import com.adamkali.simpleide.project.ProjectManager
import com.adamkali.simpleide.project.SourcePackage
import java.awt.Component
import java.awt.Font
import java.awt.Graphics
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPopupMenu

/**
 * The ProjectBrowser class is a JPanel that displays the project structure.
 */
class ProjectBrowser : JPanel(), ProjectActivityListener, MouseListener {
    private val expandedPaths = mutableSetOf<String>()
    var onFileOpened: (() -> Unit)? = null
    var promptName: (title: String) -> String? = { defaultPromptName(it) }
    var showError: (title: String, message: String) -> Unit = ::swingError

    init {
        addMouseListener(this)

        font = Global.getFont()
        background = EditorColors.sidebarBackground()
        isOpaque = true
        border = BorderFactory.createEmptyBorder(8, 8, 8, 4)

        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val title = JLabel("EXPLORER")
        title.font = Global.getFont().deriveFont(Font.BOLD, 11f)
        title.foreground = EditorColors.gutterForeground()
        title.background = EditorColors.sidebarBackground()
        title.isOpaque = true
        title.border = BorderFactory.createEmptyBorder(0, 4, 8, 0)
        title.alignmentX = Component.LEFT_ALIGNMENT
        add(title)

        ProjectManager.registerCallback(this)

        if (ProjectManager.activeProject != null) {
            rebuildTree()
        }
    }

    /**
     * Paint the project browser.
     * @param g The graphics object to paint with.
     */
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        if (g == null) return

        g.color = EditorColors.sidebarBackground()
        g.fillRect(0, 0, width, height)
    }

    private fun rebuildTree() {
        val stale = components.filter { it is FolderButton || it is FileButton }
        stale.forEach { remove(it) }

        val project = ProjectManager.activeProject
        if (project != null) {
            for (sourceFolder in project.sourceFolders) {
                addPackage(sourceFolder, 0)
            }
        }

        revalidate()
        repaint()
    }

    private fun addPackage(sourcePackage: SourcePackage, level: Int) {
        val path = sourcePackage.getPath().toString()
        val folderButton = FolderButton(sourcePackage)
        folderButton.level = level
        folderButton.dropped = expandedPaths.contains(path)
        folderButton.onDroppedChanged = {
            if (folderButton.dropped) {
                expandedPaths.add(path)
            } else {
                expandedPaths.remove(path)
            }
            rebuildTree()
        }
        folderButton.onPopup = { event ->
            showFolderMenu(folderButton, sourcePackage, event)
        }
        add(folderButton)

        if (folderButton.dropped) {
            for (child in sourcePackage.sourcePackages) {
                addPackage(child, level + 1)
            }
            for (file in sourcePackage.sourceFiles) {
                val fileButton = FileButton(file, level + 1)
                fileButton.setOnFileClicked { sourceFile ->
                    if (OpenFile.open(sourceFile.getPath())) {
                        onFileOpened?.invoke()
                    }
                    rebuildTree()
                }
                add(fileButton)
            }
        }
    }

    override fun onProjectLoad(project: Project) {
        expandedPaths.clear()
        rebuildTree()
    }

    fun createFileIn(sourcePackage: SourcePackage) {
        val name = promptName("New File") ?: return
        try {
            val created = ProjectManager.createFile(sourcePackage, name)
            expandedPaths.add(sourcePackage.getPath().toString())
            rebuildTree()
            if (OpenFile.open(created)) {
                onFileOpened?.invoke()
            }
        } catch (e: Exception) {
            showError("Error", "Could not create file: ${e.message}")
        }
    }

    fun createFolderIn(sourcePackage: SourcePackage) {
        val name = promptName("New Folder") ?: return
        try {
            ProjectManager.createFolder(sourcePackage, name)
            expandedPaths.add(sourcePackage.getPath().toString())
            rebuildTree()
        } catch (e: Exception) {
            showError("Error", "Could not create folder: ${e.message}")
        }
    }

    private fun showFolderMenu(component: Component, sourcePackage: SourcePackage, event: MouseEvent) {
        val menu = JPopupMenu()
        val newFile = JMenuItem("New File")
        newFile.addActionListener { createFileIn(sourcePackage) }
        val newFolder = JMenuItem("New Folder")
        newFolder.addActionListener { createFolderIn(sourcePackage) }
        menu.add(newFile)
        menu.add(newFolder)
        menu.show(component, event.x, event.y)
    }

    private fun defaultPromptName(title: String): String? {
        return JOptionPane.showInputDialog(
            this,
            "Name:",
            title,
            JOptionPane.PLAIN_MESSAGE
        )
    }

    private fun swingError(title: String, message: String) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE)
    }

    override fun mouseClicked(e: MouseEvent?) {
        if (e == null) return

        // FolderButton handles its own clicks. This listener only exists so clicks on
        // empty browser space do not fall through unhandled.
    }

    override fun mousePressed(e: MouseEvent?) {
    }

    override fun mouseReleased(e: MouseEvent?) {
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }
}
