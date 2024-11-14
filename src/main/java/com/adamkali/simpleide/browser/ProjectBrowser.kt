package com.adamkali.simpleide.browser

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.activity.ProjectActivityListener
import com.adamkali.simpleide.browser.components.FolderButton
import com.adamkali.simpleide.project.Project
import com.adamkali.simpleide.project.ProjectManager
import java.awt.Color
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
    init {
        addMouseListener(this)

        font = Global.getFont()

        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(JLabel("Project Browser"))

//        add(JLabel("Source Folders"))

        ProjectManager.registerCallback(this)

        // Load a default project (for now)
        try {
            ProjectManager.load(Paths.get("src/main/resources/testproject/TestProject.proj"))
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }

//        var button = FolderButton(ProjectManager.activeProject!!.sourceFolders[0])
////        button.setSize(100, 20)
//        button.setBounds(10, 10, 100, 20)
//        add(button)
    }

    /**
     * Paint the project browser.
     * @param g The graphics object to paint with.
     */
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        if (g == null) return

        // Draw background
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)

        // Draw border
        g.color = Color.GRAY
        g.drawRect(0, 0, width - 1, height - 1)

    }


    private fun clearFolderButtons() {
        for (i in 0 until componentCount) {
            val component = getComponent(i)
            if (component is FolderButton) {
                remove(component)
            }
        }
    }

    override fun onProjectLoad(project: Project) {
        clearFolderButtons()
        for (sourceFolder in project.sourceFolders) {
            val folderButton = FolderButton(sourceFolder)
            add(folderButton)
        }

        repaint()
    }

    override fun mouseClicked(e: MouseEvent?) {
        if (e == null) return

        for (i in 0 until componentCount) {
            val component = getComponent(i)
            if (component is FolderButton) {
                if (component.contains(e.point)) {
                    println("Clicked on folder button for ${component.getText()}")
                    println("Button bounds: ${component.bounds}")
                } else {
                    // Print the button's bounds
                    println("Button bounds: ${component.bounds}")
                }
            }
        }
    }

    override fun mousePressed(e: MouseEvent?) {
    }

    override fun mouseReleased(e: MouseEvent?) {
    }

    override fun mouseEntered(e: MouseEvent?) {
        if (e == null) return
    }

    override fun mouseExited(e: MouseEvent?) {
    }
}