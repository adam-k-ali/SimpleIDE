package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
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
import java.nio.file.Paths

class EditorPanelGuiTest {
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
    fun expandingFolder_doesNotChangeBrowserOrEditorColumnWidths() {
        val panel = EditorPanel()
        ProjectManager.load(Paths.get("src/main/resources/testproject"))
        panel.setSize(800, 600)
        panel.doLayout()

        val split = panel.splitPane
        val browserWidthBefore = split.leftComponent.width
        val editorWidthBefore = split.rightComponent.width

        val root = panel.projectBrowser.components.filterIsInstance<FolderButton>().single { it.getText() == "src" }
        GuiRender.click(root)
        panel.doLayout()

        assertEquals(
            browserWidthBefore,
            split.leftComponent.width,
            "project browser column should not grow when a folder is expanded"
        )
        assertEquals(
            editorWidthBefore,
            split.rightComponent.width,
            "editor column should not shrink when a folder is expanded"
        )
    }

    @Test
    fun activityBar_togglesSidebarVisibility() {
        val panel = EditorPanel()
        ProjectManager.load(Paths.get("src/main/resources/testproject"))
        panel.setSize(800, 600)
        panel.doLayout()

        assertTrue(panel.sidebarVisible)
        assertTrue(panel.activityBar.explorerSelected)
        assertTrue(panel.splitPane.leftComponent.isVisible)

        GuiRender.click(panel.activityBar, 24, 24)
        panel.doLayout()

        assertFalse(panel.sidebarVisible)
        assertFalse(panel.activityBar.explorerSelected)
        assertFalse(panel.splitPane.leftComponent.isVisible)

        GuiRender.click(panel.activityBar, 24, 24)
        panel.doLayout()

        assertTrue(panel.sidebarVisible)
        assertTrue(panel.activityBar.explorerSelected)
        assertTrue(panel.splitPane.leftComponent.isVisible)
    }

    @Test
    fun tabBar_showsOpenedFileName() {
        val panel = EditorPanel()
        ProjectManager.load(Paths.get("src/main/resources/testproject"))
        assertEquals("Untitled", panel.editorTabBar.displayedTitle())

        assertTrue(OpenFile.open(Paths.get("src/main/resources/testproject/src/test/Main.java")))
        panel.editorTabBar.refresh()
        assertEquals("Main.java", panel.editorTabBar.displayedTitle())
    }
}
