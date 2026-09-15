package com.adamkali.simpleide.browser

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.activity.ProjectActivityListener
import com.adamkali.simpleide.browser.components.FileButton
import com.adamkali.simpleide.browser.components.FolderButton
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.project.Project
import com.adamkali.simpleide.project.ProjectManager
import com.adamkali.simpleide.project.SourcePackage
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.FileNotFoundException
import java.nio.file.Paths
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * The ProjectBrowser class is a JPanel that displays the project structure.
 */
class ProjectBrowser : JPanel(), ProjectActivityListener, MouseListener {
    private val expandedPaths = mutableSetOf<String>()
    var onFileOpened: (() -> Unit)? = null

    init {
        addMouseListener(this)

        font = Global.getFont()
        background = Color.WHITE
        isOpaque = true

        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val title = JLabel("Project Browser")
        title.alignmentX = Component.LEFT_ALIGNMENT
        add(title)

        ProjectManager.registerCallback(this)

        // Load a default project (for now)
        try {
            ProjectManager.load(Paths.get("src/main/resources/testproject/TestProject.proj"))
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Paint the project browser.
     * @param g The graphics object to paint with.
     */
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        if (g == null) return

        g.color = background
        g.fillRect(0, 0, width, height)

        g.color = Color.GRAY
        g.drawRect(0, 0, width - 1, height - 1)
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
                }
                add(fileButton)
            }
        }
    }

    override fun onProjectLoad(project: Project) {
        expandedPaths.clear()
        rebuildTree()
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
