package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.preferences.RecentProjects
import com.adamkali.simpleide.project.ProjectManager
import com.adamkali.simpleide.testsupport.GuiRender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JButton
import javax.swing.JLabel

class HomeScreenGuiTest {
    private val sampleProject = Paths.get("src/main/resources/testproject")

    @BeforeEach
    fun setUp() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        ProjectManager.reset()
        RecentProjects.useTempStore()
    }

    @Test
    fun titleAndButtonsArePresent() {
        val home = HomeScreen()
        home.setSize(800, 600)
        home.doLayout()

        assertEquals("SimpleIDE", home.titleLabel.text)
        assertEquals("Open Project", home.openButton.text)
        assertEquals("New Project", home.newButton.text)
        assertTrue(home.titleLabel.preferredSize.width > 0)
        assertTrue(home.openButton.preferredSize.width > 0)
        assertTrue(home.newButton.preferredSize.width > 0)
        GuiRender.render(home, 800, 600)
    }

    @Test
    fun openProject_loadsChosenDirAndInvokesReady() {
        var ready = false
        val home = HomeScreen()
        home.chooseProjectDir = { sampleProject }
        home.onProjectReady = { ready = true }

        home.openButton.doClick()

        assertTrue(ready)
        assertEquals("TestProject", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun newProject_writesFilesLoadsAndInvokesReady() {
        var ready = false
        val parent = Files.createTempDirectory("simpleide-home-")
        parent.toFile().deleteOnExit()

        val home = HomeScreen()
        home.chooseNewProject = { NewProjectRequest(parent, "Fresh") }
        home.onProjectReady = { ready = true }

        home.newButton.doClick()

        assertTrue(ready)
        val projectDir = parent.resolve("Fresh")
        assertTrue(Files.isRegularFile(projectDir.resolve(".simple").resolve("Fresh.proj")))
        assertTrue(Files.isDirectory(projectDir.resolve("src")))
        assertEquals("Fresh", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun cancelOpen_doesNotLoadAProject() {
        var ready = false
        val home = HomeScreen()
        home.chooseProjectDir = { null }
        home.onProjectReady = { ready = true }

        home.openButton.doClick()

        assertFalse(ready)
        assertNull(ProjectManager.activeProject)
    }

    @Test
    fun invalidName_showsErrorAndStaysOnHome() {
        var ready = false
        val errors = mutableListOf<String>()
        val parent = Files.createTempDirectory("simpleide-home-invalid-")
        parent.toFile().deleteOnExit()

        val home = HomeScreen()
        home.chooseNewProject = { NewProjectRequest(parent, "bad/name") }
        home.showError = { _, message -> errors.add(message) }
        home.onProjectReady = { ready = true }

        home.newButton.doClick()

        assertFalse(ready)
        assertNull(ProjectManager.activeProject)
        assertTrue(errors.isNotEmpty(), "invalid name should show an error")
        assertEquals(0, Files.list(parent).use { it.count() })
    }

    @Test
    fun emptyRecents_omitsRecentSection() {
        val home = HomeScreen()
        home.setSize(800, 600)
        home.doLayout()

        assertTrue(home.recentButtons.isEmpty())
        assertNull(home.recentsLabel.parent)
        GuiRender.render(home, 800, 600)
    }

    @Test
    fun recentButtons_showSeededProjects_andClickLoads() {
        val other = Files.createTempDirectory("simpleide-home-other-")
        other.toFile().deleteOnExit()
        Files.createDirectories(other.resolve(".simple"))
        Files.writeString(
            other.resolve(".simple").resolve("Other.proj"),
            """{"projectName":"Other","sourcePaths":["src"]}"""
        )

        RecentProjects.record(other, "Other")
        RecentProjects.record(sampleProject, "TestProject")

        var ready = false
        val home = HomeScreen()
        home.onProjectReady = { ready = true }
        home.setSize(800, 600)
        home.doLayout()

        assertEquals(listOf("TestProject", "Other"), home.recentButtons.map { recentName(it) })
        assertEquals("Recent", home.recentsLabel.text)
        assertTrue(home.recentsLabel.parent != null)

        home.recentButtons[0].doClick()

        assertTrue(ready)
        assertEquals("TestProject", ProjectManager.activeProject?.getProjectName())
        GuiRender.render(home, 800, 600)
    }

    @Test
    fun missingRecent_showsErrorAndDropsEntry() {
        val missing = Paths.get("/definitely/not/a/simpleide/project")
        RecentProjects.record(missing, "Gone")

        var ready = false
        val errors = mutableListOf<String>()
        val home = HomeScreen()
        home.showError = { _, message -> errors.add(message) }
        home.onProjectReady = { ready = true }

        assertEquals(1, home.recentButtons.size)
        home.recentButtons[0].doClick()

        assertFalse(ready)
        assertNull(ProjectManager.activeProject)
        assertTrue(errors.isNotEmpty(), "missing recent should show an error")
        assertTrue(RecentProjects.list().isEmpty())
        assertTrue(home.recentButtons.isEmpty())
        assertNull(home.recentsLabel.parent)
    }

    private fun recentName(button: JButton): String {
        val labels = button.components.filterIsInstance<JLabel>()
        return labels.first().text
    }
}
