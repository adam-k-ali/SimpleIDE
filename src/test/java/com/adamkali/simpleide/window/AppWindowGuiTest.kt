package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.UnsavedChoice
import com.adamkali.simpleide.editor.io.action.ActionsList
import com.adamkali.simpleide.preferences.RecentProjects
import com.adamkali.simpleide.project.ProjectManager
import com.adamkali.simpleide.testsupport.GuiRender
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel

class AppWindowGuiTest {
    private val sampleProject = Paths.get("src/main/resources/testproject")

    @BeforeEach
    fun setUp() {
        AppWindow.disposeForTest()
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        OpenFile.showError = { _, _ -> }
        OpenFile.prompt = { UnsavedChoice.DISCARD }
        ProjectManager.reset()
        RecentProjects.useTempStore()
    }

    @AfterEach
    fun tearDown() {
        AppWindow.disposeForTest()
        OpenFile.reset()
        ProjectManager.reset()
    }

    @Test
    fun fileMenu_onHome_closeProjectIsDisabled() {
        val frame = startWindow()

        assertEquals("File", frame.jMenuBar.getMenu(0).text)
        assertEquals("New Project", AppWindow.fileMenu.newProjectItem.text)
        assertEquals("Open Project", AppWindow.fileMenu.openProjectItem.text)
        assertEquals("Close Project", AppWindow.fileMenu.closeProjectItem.text)
        assertFalse(AppWindow.fileMenu.closeProjectItem.isEnabled)
        assertTrue(showingHome(frame))
        GuiRender.render(AppWindow.homeScreen, 800, 600)
    }

    @Test
    fun openProjectFromMenu_showsEditorAndEnablesClose() {
        val frame = startWindow()
        openProject(sampleProject)

        assertFalse(showingHome(frame))
        assertTrue(AppWindow.fileMenu.closeProjectItem.isEnabled)
        assertEquals("TestProject", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun openProjectButton_showsEditor() {
        val frame = startWindow()
        AppWindow.homeScreen.chooseProjectDir = { sampleProject }
        AppWindow.homeScreen.openButton.doClick()
        restubOpenFile()

        assertFalse(showingHome(frame))
        assertTrue(AppWindow.fileMenu.closeProjectItem.isEnabled)
        assertEquals("TestProject", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun newProjectFromMenu_showsEditor() {
        val parent = Files.createTempDirectory("simpleide-menu-new-")
        parent.toFile().deleteOnExit()
        val frame = startWindow()
        AppWindow.homeScreen.chooseNewProject = { NewProjectRequest(parent, "FromMenu") }

        AppWindow.fileMenu.newProjectItem.doClick()
        restubOpenFile()

        assertFalse(showingHome(frame))
        assertEquals("FromMenu", ProjectManager.activeProject?.getProjectName())
        assertTrue(AppWindow.fileMenu.closeProjectItem.isEnabled)
    }

    @Test
    fun closeProject_returnsToHomeAndKeepsRecents() {
        val frame = startWindow()
        openProject(sampleProject)

        AppWindow.fileMenu.closeProjectItem.doClick()

        assertTrue(showingHome(frame))
        assertNull(ProjectManager.activeProject)
        assertFalse(AppWindow.fileMenu.closeProjectItem.isEnabled)
        assertEquals("", Global.getCursor().getDocument().toText())
        assertEquals("SimpleIDE", frame.title)
        assertEquals(listOf("TestProject"), AppWindow.homeScreen.recentButtons.map { recentName(it) })
        GuiRender.render(AppWindow.homeScreen, 800, 600)
    }

    @Test
    fun closeProject_cancelDirty_staysInEditor() {
        val frame = startWindow()
        openProject(sampleProject)

        val file = tempFile("aaa")
        assertTrue(OpenFile.open(file))
        ActionsList.TYPE_CHARACTER.execute('z')
        OpenFile.prompt = { UnsavedChoice.CANCEL }

        AppWindow.fileMenu.closeProjectItem.doClick()

        assertFalse(showingHome(frame))
        assertEquals("TestProject", ProjectManager.activeProject?.getProjectName())
        assertTrue(OpenFile.isDirty())
        assertEquals("zaaa", Global.getCursor().getDocument().toText())
        assertEquals("aaa", Files.readString(file, StandardCharsets.UTF_8))
        assertTrue(AppWindow.fileMenu.closeProjectItem.isEnabled)
    }

    @Test
    fun openWhileProjectOpen_switchesProject_andSecondCloseReturnsHome() {
        val other = Files.createTempDirectory("simpleide-switch-")
        other.toFile().deleteOnExit()

        val frame = startWindow()
        openProject(sampleProject)
        assertEquals("TestProject", ProjectManager.activeProject?.getProjectName())

        openProject(other)
        assertEquals(other.fileName.toString(), ProjectManager.activeProject?.getProjectName())
        assertFalse(showingHome(frame))

        AppWindow.fileMenu.closeProjectItem.doClick()
        assertTrue(showingHome(frame))
        assertNull(ProjectManager.activeProject)

        openProject(sampleProject)
        assertEquals("TestProject", ProjectManager.activeProject?.getProjectName())
        assertFalse(showingHome(frame))

        AppWindow.fileMenu.closeProjectItem.doClick()
        assertTrue(showingHome(frame))
        assertNull(ProjectManager.activeProject)
        assertFalse(AppWindow.fileMenu.closeProjectItem.isEnabled)
    }

    private fun startWindow(): JFrame {
        val frame = AppWindow.startForTest()
        AppWindow.homeScreen.showError = { _, _ -> }
        return frame
    }

    private fun openProject(path: Path) {
        AppWindow.homeScreen.chooseProjectDir = { path }
        AppWindow.fileMenu.openProjectItem.doClick()
        restubOpenFile()
    }

    private fun restubOpenFile() {
        OpenFile.showError = { _, _ -> }
        OpenFile.prompt = { UnsavedChoice.DISCARD }
    }

    private fun showingHome(frame: JFrame): Boolean {
        return frame.contentPane.components.any { it is HomeScreen }
    }

    private fun recentName(button: JButton): String {
        val labels = button.components.filterIsInstance<JLabel>()
        return labels.first().text
    }

    private fun tempFile(contents: String) = Files.createTempFile("simpleide-", ".txt").also {
        it.toFile().deleteOnExit()
        Files.writeString(it, contents, StandardCharsets.UTF_8)
    }
}
