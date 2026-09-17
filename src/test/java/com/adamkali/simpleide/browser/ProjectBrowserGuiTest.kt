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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    companion object {
        private val SAMPLE_PROJECT = Paths.get("src/main/resources/testproject")
    }
}
