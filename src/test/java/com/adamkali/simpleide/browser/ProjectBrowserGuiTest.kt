package com.adamkali.simpleide.browser

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.browser.components.FileButton
import com.adamkali.simpleide.browser.components.FolderButton
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.UnsavedChoice
import com.adamkali.simpleide.preferences.RecentProjects
import com.adamkali.simpleide.project.ProjectManager
import com.adamkali.simpleide.testsupport.GuiRender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JLabel

class ProjectBrowserGuiTest {
    @BeforeEach
    fun setUp() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        OpenFile.showError = { _, _ -> }
        OpenFile.prompt = { UnsavedChoice.DISCARD }
        ProjectManager.reset()
        RecentProjects.useTempStore()
    }

    @Test
    fun expandingFolder_revealsNestedPackagesAndFiles() {
        val browser = ProjectBrowser()
        ProjectManager.load(SAMPLE_PROJECT)
        browser.setSize(240, 400)
        browser.doLayout()

        val root = browser.components.filterIsInstance<FolderButton>().single { it.getText() == "src" }
        assertTrue(root.preferredSize.height > 8)
        GuiRender.click(root)
        browser.doLayout()

        val folders = browser.components.filterIsInstance<FolderButton>().map { it.getText() }
        assertTrue(folders.contains("util"), "expanded src should show util, was $folders")
        assertTrue(folders.contains("empty"), "expanded src should show empty, was $folders")
        val srcFiles = browser.components.filterIsInstance<FileButton>().map { it.fileName }
        assertTrue(srcFiles.contains("LargeFile.java"), "expanded src should show LargeFile.java, was $srcFiles")

        val nested = browser.components.filterIsInstance<FolderButton>().first { it.getText() == "test" }
        GuiRender.click(nested)
        browser.doLayout()

        val files = browser.components.filterIsInstance<FileButton>().map { it.fileName }
        assertTrue(files.contains("Main.java"), "expanded tree should show Main.java, was $files")
        assertTrue(files.contains("Person.java"), "expanded tree should show Person.java, was $files")
        assertTrue(browser.components.any { it is JLabel })
    }

    @Test
    fun expandingNotes_revealsMarkdownAndKotlinFiles() {
        val browser = ProjectBrowser()
        ProjectManager.load(SAMPLE_PROJECT)
        browser.setSize(240, 400)
        browser.doLayout()

        val notes = browser.components.filterIsInstance<FolderButton>().single { it.getText() == "notes" }
        GuiRender.click(notes)
        browser.doLayout()

        val files = browser.components.filterIsInstance<FileButton>().map { it.fileName }
        assertTrue(files.contains("README.md"), "expanded notes should show README.md, was $files")
        assertTrue(files.contains("Sample.kt"), "expanded notes should show Sample.kt, was $files")
    }

    @Test
    fun clickingFile_loadsContentsIntoTheEditor() {
        val browser = ProjectBrowser()
        ProjectManager.load(SAMPLE_PROJECT)
        browser.setSize(240, 400)
        browser.doLayout()

        GuiRender.click(browser.components.filterIsInstance<FolderButton>().single { it.getText() == "src" })
        browser.doLayout()
        GuiRender.click(browser.components.filterIsInstance<FolderButton>().first { it.getText() == "test" })
        browser.doLayout()

        val main = browser.components.filterIsInstance<FileButton>().single { it.fileName == "Main.java" }
        GuiRender.click(main)

        val text = Global.getCursor().getDocument().toText()
        assertTrue(text.contains("package test;"), "opened file should contain package test;, was: $text")
        assertTrue(text.contains("Hello World!"), "opened file should contain Hello World!, was: $text")
        assertTrue(OpenFile.path.toString().endsWith("Main.java"))
    }

    @Test
    fun newFile_createsOpensAndShowsInTree() {
        val dest = ProjectManager.create(tempDir(), "Demo")
        val browser = ProjectBrowser()
        browser.showError = { _, _ -> }
        browser.promptName = { "Hello.java" }
        browser.setSize(240, 400)
        browser.doLayout()

        val src = ProjectManager.activeProject!!.sourceFolders.single()
        browser.createFileIn(src)
        browser.doLayout()

        val files = browser.components.filterIsInstance<FileButton>().map { it.fileName }
        assertTrue(files.contains("Hello.java"), "tree should show Hello.java after New File, was $files")
        assertEquals(dest.resolve("src").resolve("Hello.java"), OpenFile.path)
        assertEquals("", Global.getCursor().getDocument().toText())
        assertTrue(Files.isRegularFile(dest.resolve("src").resolve("Hello.java")))
        assertTrue(
            browser.components.filterIsInstance<FolderButton>().single { it.getText() == "src" }.dropped,
            "parent folder should stay expanded after creating a file"
        )
    }

    @Test
    fun newFolder_createsAndShowsInTreeWithoutCollapsing() {
        ProjectManager.create(tempDir(), "Demo")
        val browser = ProjectBrowser()
        browser.showError = { _, _ -> }
        val srcButton = browser.components.filterIsInstance<FolderButton>().single { it.getText() == "src" }
        GuiRender.click(srcButton)
        browser.doLayout()
        browser.promptName = { "Hello.java" }
        browser.createFileIn(ProjectManager.activeProject!!.sourceFolders.single())
        browser.doLayout()

        browser.promptName = { "util" }
        browser.createFolderIn(ProjectManager.activeProject!!.sourceFolders.single())
        browser.doLayout()

        val folders = browser.components.filterIsInstance<FolderButton>().map { it.getText() }
        assertTrue(folders.contains("util"), "expanded src should show util after New Folder, was $folders")
        val files = browser.components.filterIsInstance<FileButton>().map { it.fileName }
        assertTrue(files.contains("Hello.java"), "existing file should remain visible, was $files")
        assertTrue(Files.isDirectory(ProjectManager.activeProject!!.sourceFolders.single().getPath().resolve("util")))
    }

    @Test
    fun newFile_invalidName_showsErrorAndDoesNotCreate() {
        val dest = ProjectManager.create(tempDir(), "Demo")
        val browser = ProjectBrowser()
        val errors = mutableListOf<String>()
        browser.showError = { _, message -> errors.add(message) }
        browser.promptName = { "foo/bar" }

        browser.createFileIn(ProjectManager.activeProject!!.sourceFolders.single())

        assertTrue(errors.isNotEmpty(), "invalid name should show an error")
        assertTrue(errors[0].contains("path separators") || errors[0].contains("Could not create file"), errors[0])
        assertEquals(null, OpenFile.path)
        assertEquals(0, Files.list(dest.resolve("src")).use { it.count() })
        assertFalse(browser.components.filterIsInstance<FileButton>().any())
    }

    @Test
    fun newFile_cancelPrompt_doesNotCreate() {
        val dest = ProjectManager.create(tempDir(), "Demo")
        val browser = ProjectBrowser()
        browser.showError = { _, _ -> }
        browser.promptName = { null }

        browser.createFileIn(ProjectManager.activeProject!!.sourceFolders.single())

        assertEquals(null, OpenFile.path)
        assertEquals(0, Files.list(dest.resolve("src")).use { it.count() })
    }

    private fun tempDir() = Files.createTempDirectory("simpleide-browser-").also {
        it.toFile().deleteOnExit()
    }

    companion object {
        private val SAMPLE_PROJECT = Paths.get("src/main/resources/testproject")
    }
}
