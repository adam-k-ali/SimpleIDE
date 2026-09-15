package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.preferences.EditorColors
import com.adamkali.simpleide.project.ProjectManager
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.nio.file.Path
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.filechooser.FileNameExtensionFilter

data class NewProjectRequest(
    val parentDir: Path,
    val projectName: String
)

/**
 * Startup screen: open an existing `.proj` file or create a new project.
 */
class HomeScreen : JPanel() {
    val titleLabel = JLabel("SimpleIDE")
    val openButton = JButton("Open Project")
    val newButton = JButton("New Project")

    var chooseProjectFile: () -> Path? = { defaultChooseProjectFile() }
    var chooseNewProject: () -> NewProjectRequest? = { defaultChooseNewProject() }
    var showError: (title: String, message: String) -> Unit = ::swingError
    var onProjectReady: () -> Unit = {}

    init {
        layout = GridBagLayout()
        background = EditorColors.editorBackground()
        isOpaque = true

        titleLabel.font = Global.getFont().deriveFont(Font.BOLD, 28f)
        titleLabel.foreground = EditorColors.editorForeground()
        titleLabel.alignmentX = Component.CENTER_ALIGNMENT

        openButton.alignmentX = Component.CENTER_ALIGNMENT
        newButton.alignmentX = Component.CENTER_ALIGNMENT
        openButton.addActionListener { openProject() }
        newButton.addActionListener { newProject() }

        val buttons = JPanel(GridLayout(2, 1, 0, 8))
        buttons.isOpaque = false
        buttons.add(openButton)
        buttons.add(newButton)
        val buttonWidth = 180
        buttons.maximumSize = Dimension(buttonWidth, buttons.preferredSize.height)
        buttons.alignmentX = Component.CENTER_ALIGNMENT

        val content = JPanel()
        content.layout = BoxLayout(content, BoxLayout.Y_AXIS)
        content.isOpaque = false
        content.add(titleLabel)
        content.add(Box.createVerticalStrut(24))
        content.add(buttons)

        add(content)
    }

    private fun openProject() {
        val path = chooseProjectFile() ?: return
        try {
            ProjectManager.load(path)
            onProjectReady()
        } catch (e: Exception) {
            showError("Error", "Could not open project: ${e.message}")
        }
    }

    private fun newProject() {
        val request = chooseNewProject() ?: return
        try {
            ProjectManager.create(request.parentDir, request.projectName)
            onProjectReady()
        } catch (e: Exception) {
            showError("Error", "Could not create project: ${e.message}")
        }
    }

    private fun defaultChooseProjectFile(): Path? {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Open Project"
        chooser.fileFilter = FileNameExtensionFilter("SimpleIDE Project (*.proj)", "proj")
        val result = chooser.showOpenDialog(this)
        if (result != JFileChooser.APPROVE_OPTION) {
            return null
        }
        return chooser.selectedFile.toPath()
    }

    private fun defaultChooseNewProject(): NewProjectRequest? {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Choose Project Location"
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val result = chooser.showDialog(this, "Select")
        if (result != JFileChooser.APPROVE_OPTION) {
            return null
        }
        val name = JOptionPane.showInputDialog(
            this,
            "Project name:",
            "New Project",
            JOptionPane.PLAIN_MESSAGE
        ) ?: return null
        return NewProjectRequest(chooser.selectedFile.toPath(), name)
    }

    private fun swingError(title: String, message: String) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE)
    }
}
