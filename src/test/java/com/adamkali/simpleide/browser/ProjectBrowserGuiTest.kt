package com.adamkali.simpleide.browser

import com.adamkali.simpleide.browser.components.FileButton
import com.adamkali.simpleide.browser.components.FolderButton
import com.adamkali.simpleide.testsupport.GuiRender
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.swing.JLabel

class ProjectBrowserGuiTest {
    @Test
    fun expandingFolder_revealsNestedPackagesAndFiles() {
        val browser = ProjectBrowser()
        browser.setSize(240, 400)
        browser.doLayout()

        val root = browser.components.filterIsInstance<FolderButton>().single { it.getText() == "src" }
        assertTrue(root.preferredSize.height > 8)
        GuiRender.click(root)
        browser.doLayout()

        val nested = browser.components.filterIsInstance<FolderButton>().first { it.getText() == "test" }
        GuiRender.click(nested)
        browser.doLayout()

        val files = browser.components.filterIsInstance<FileButton>().map { it.fileName }
        assertTrue(files.contains("Main.java"), "expanded tree should show Main.java, was $files")
        assertTrue(files.contains("Person.java"), "expanded tree should show Person.java, was $files")
        assertTrue(browser.components.any { it is JLabel })
    }
}
