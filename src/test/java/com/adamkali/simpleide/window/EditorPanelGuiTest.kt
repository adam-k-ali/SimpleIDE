package com.adamkali.simpleide.window

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.browser.components.FolderButton
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.UnsavedChoice
import com.adamkali.simpleide.testsupport.GuiRender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.swing.JScrollPane

class EditorPanelGuiTest {
    @BeforeEach
    fun setUp() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        OpenFile.showError = { _, _ -> }
        OpenFile.prompt = { UnsavedChoice.DISCARD }
    }

    @Test
    fun expandingFolder_doesNotChangeBrowserOrEditorColumnWidths() {
        val panel = EditorPanel()
        panel.setSize(800, 600)
        panel.doLayout()

        val browserColumn = panel.components[0]
        val editorColumn = panel.components[1] as JScrollPane
        val browserWidthBefore = browserColumn.width
        val editorWidthBefore = editorColumn.width

        val root = panel.projectBrowser.components.filterIsInstance<FolderButton>().single { it.getText() == "src" }
        GuiRender.click(root)
        panel.doLayout()

        assertEquals(
            browserWidthBefore,
            browserColumn.width,
            "project browser column should not grow when a folder is expanded"
        )
        assertEquals(
            editorWidthBefore,
            editorColumn.width,
            "editor column should not shrink when a folder is expanded"
        )
    }
}
