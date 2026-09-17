package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.preferences.EditorColors
import com.adamkali.simpleide.preferences.RecentProject
import com.adamkali.simpleide.preferences.RecentProjects
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

data class NewProjectRequest(
    val parentDir: Path,
    val projectName: String
)

/**
 * Startup screen: open a project folder (creating `.simple` if needed) or create a new project.
 */
class HomeScreen : JPanel() {
    val titleLabel = JLabel("SimpleIDE")
    val openButton = JButton("Open Project")
    val newButton = JButton("New Project")
    val recentsLabel = JLabel("Recent")

    var recentButtons: List<JButton> = emptyList()
        private set

    var chooseProjectDir: () -> Path? = { defaultChooseProjectDir() }
    var chooseNewProject: () -> NewProjectRequest? = { defaultChooseNewProject() }
    var showError: (title: String, message: String) -> Unit = ::swingError
    var onProjectReady: () -> Unit = {}

    private val recentsHost = JPanel()

    init {
        layout = GridBagLayout()
        background = EditorColors.editorBackground()
        isOpaque = true

        titleLabel.font = Global.getFont().deriveFont(Font.BOLD, 28f)
        titleLabel.foreground = EditorColors.editorForeground()
        titleLabel.alignmentX = Component.CENTER_ALIGNMENT

        recentsLabel.font = Global.getFont().deriveFont(Font.BOLD, 14f)
        recentsLabel.foreground = EditorColors.editorForeground()
        recentsLabel.alignmentX = Component.CENTER_ALIGNMENT

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

        recentsHost.layout = BoxLayout(recentsHost, BoxLayout.Y_AXIS)
        recentsHost.isOpaque = false
        recentsHost.alignmentX = Component.CENTER_ALIGNMENT

        val content = JPanel()
        content.layout = BoxLayout(content, BoxLayout.Y_AXIS)
        content.isOpaque = false
        content.add(titleLabel)
        content.add(Box.createVerticalStrut(24))
        content.add(buttons)
        content.add(recentsHost)

        add(content)
        rebuildRecents()
    }

    private fun openProject() {
        val path = chooseProjectDir() ?: return
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

    private fun openRecent(path: Path) {
        try {
            ProjectManager.load(path)
            onProjectReady()
        } catch (e: Exception) {
            RecentProjects.remove(path)
            rebuildRecents()
            showError("Error", "Could not open project: ${e.message}")
        }
    }

    private fun rebuildRecents() {
        recentsHost.removeAll()
        val recents = RecentProjects.list()
        if (recents.isEmpty()) {
            recentButtons = emptyList()
            recentsHost.revalidate()
            recentsHost.repaint()
            return
        }

        recentsHost.add(Box.createVerticalStrut(32))
        recentsHost.add(recentsLabel)
        recentsHost.add(Box.createVerticalStrut(12))

        val buttons = mutableListOf<JButton>()
        for (entry in recents) {
            val button = recentButton(entry)
            buttons.add(button)
            recentsHost.add(button)
            recentsHost.add(Box.createVerticalStrut(4))
        }
        recentButtons = buttons
        recentsHost.revalidate()
        recentsHost.repaint()
    }

    private fun recentButton(entry: RecentProject): JButton {
        val nameLabel = JLabel(entry.name)
        nameLabel.font = Global.getFont().deriveFont(Font.PLAIN, 13f)
        nameLabel.foreground = EditorColors.editorForeground()
        nameLabel.alignmentX = Component.LEFT_ALIGNMENT

        val pathLabel = JLabel(entry.path.toString())
        pathLabel.font = Global.getFont().deriveFont(11f)
        pathLabel.foreground = EditorColors.gutterForeground()
        pathLabel.alignmentX = Component.LEFT_ALIGNMENT

        val button = JButton()
        button.layout = BoxLayout(button, BoxLayout.Y_AXIS)
        button.alignmentX = Component.CENTER_ALIGNMENT
        button.toolTipText = entry.path.toString()
        button.add(nameLabel)
        button.add(pathLabel)
        button.addActionListener { openRecent(entry.path) }

        val width = 420
        val height = button.preferredSize.height + 8
        button.preferredSize = Dimension(width, height)
        button.maximumSize = Dimension(width, height)
        button.minimumSize = Dimension(width, height)
        return button
    }

    private fun defaultChooseProjectDir(): Path? {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Open Project"
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
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
