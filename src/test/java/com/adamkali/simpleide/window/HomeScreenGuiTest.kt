package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
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

class HomeScreenGuiTest {
    private val sampleProject = Paths.get("src/main/resources/testproject/TestProject.proj")

    @BeforeEach
    fun setUp() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        ProjectManager.reset()
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
    fun openProject_loadsChosenFileAndInvokesReady() {
        var ready = false
        val home = HomeScreen()
        home.chooseProjectFile = { sampleProject }
        home.onProjectReady = { ready = true }

        home.openButton.doClick()

        assertTrue(ready)
        assertEquals("TestProject", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun openProject_resolvesPathWhenExtensionIsMissing() {
        val parent = Files.createTempDirectory("simpleide-home-ext-")
        parent.toFile().deleteOnExit()
        val projectDir = parent.resolve("HiddenExt")
        Files.createDirectories(projectDir.resolve("src"))
        Files.writeString(
            projectDir.resolve("HiddenExt.proj"),
            """{"projectName":"HiddenExt","sourcePaths":["src"]}"""
        )

        var ready = false
        val home = HomeScreen()
        home.chooseProjectFile = { projectDir.resolve("HiddenExt") }
        home.onProjectReady = { ready = true }

        home.openButton.doClick()

        assertTrue(ready)
        assertEquals("HiddenExt", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun openProject_loadsProjInsideSelectedFolder() {
        val parent = Files.createTempDirectory("simpleide-home-dir-")
        parent.toFile().deleteOnExit()
        val projectDir = parent.resolve("FolderOpen")
        Files.createDirectories(projectDir.resolve("src"))
        Files.writeString(
            projectDir.resolve("FolderOpen.proj"),
            """{"projectName":"FolderOpen","sourcePaths":["src"]}"""
        )

        var ready = false
        val home = HomeScreen()
        home.chooseProjectFile = { projectDir }
        home.onProjectReady = { ready = true }

        home.openButton.doClick()

        assertTrue(ready)
        assertEquals("FolderOpen", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun openProject_showsErrorForNonProjectFile() {
        val parent = Files.createTempDirectory("simpleide-home-badfile-")
        parent.toFile().deleteOnExit()
        val javaFile = parent.resolve("Main.java")
        Files.writeString(javaFile, "class Main {}")

        var ready = false
        val errors = mutableListOf<String>()
        val home = HomeScreen()
        home.chooseProjectFile = { javaFile }
        home.showError = { _, message -> errors.add(message) }
        home.onProjectReady = { ready = true }

        home.openButton.doClick()

        assertFalse(ready)
        assertNull(ProjectManager.activeProject)
        assertTrue(errors.any { it.contains(".proj") }, errors.toString())
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
        assertTrue(Files.isRegularFile(projectDir.resolve("Fresh.proj")))
        assertTrue(Files.isDirectory(projectDir.resolve("src")))
        assertEquals("Fresh", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun cancelOpen_doesNotLoadAProject() {
        var ready = false
        val home = HomeScreen()
        home.chooseProjectFile = { null }
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
}
