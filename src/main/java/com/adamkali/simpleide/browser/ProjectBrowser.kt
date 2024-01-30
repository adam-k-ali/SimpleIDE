package com.adamkali.simpleide.browser

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.project.ProjectManager
import java.awt.Color
import java.awt.Graphics
import java.io.FileNotFoundException
import java.nio.file.Paths
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * The ProjectBrowser class is a JPanel that displays the project structure.
 */
class ProjectBrowser : JPanel() {
    init {
        font = Global.getFont()

        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(JLabel("Project Browser"))

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

        // Draw background
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)

        // Draw border
        g.color = Color.GRAY
        g.drawRect(0, 0, width - 1, height - 1)

        if (ProjectManager.activeProject != null) {
            // Draw project name
            g.color = Color.BLACK
            g.drawString(ProjectManager.activeProject!!.getProjectName(), 10, 20)
            
            // Draw Source Folders
            var y = 40;
            for (sourceFolder in ProjectManager.activeProject!!.sourceFolders) {

                g.drawString(sourceFolder.getName(), 20, y)
                y += 20

                for (sourcePackage in sourceFolder.sourcePackages) {
                    g.drawString(sourcePackage.getName(), 30, y)
                    y += 20

                    for (sourceFile in sourcePackage.sourceFiles) {
                        g.drawString(sourceFile.getFileName(), 40, y)
                        y += 20
                    }

                }

                for (sourceFile in sourceFolder.sourceFiles) {
                    g.drawString(sourceFile.getFileName(), 30, y)
                    y += 20
                }
            }
        }

    }
}