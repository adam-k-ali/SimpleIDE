package com.adamkali.simpleide.browser.components

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.editor.io.Document
import com.adamkali.simpleide.editor.io.EditorCursor
import com.adamkali.simpleide.editor.io.OpenFile
import com.adamkali.simpleide.editor.io.UnsavedChoice
import com.adamkali.simpleide.preferences.EditorColors
import com.adamkali.simpleide.project.SourceFile
import com.adamkali.simpleide.project.SourcePackage
import com.adamkali.simpleide.testsupport.GuiRender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Paths

class FolderButtonGuiTest {
    @Test
    fun preferredHeight_isUsableByBoxLayout() {
        val button = FolderButton(SourcePackage(Paths.get("src")))
        assertTrue(button.preferredSize.height > 8, "FolderButton preferred height was ${button.preferredSize.height}")
        assertTrue(button.preferredSize.width > 0)
    }

    @Test
    fun click_togglesDropped() {
        val button = FolderButton(SourcePackage(Paths.get("src")))
        button.size = button.preferredSize
        assertFalse(button.dropped)
        GuiRender.click(button)
        assertTrue(button.dropped)
        GuiRender.click(button)
        assertFalse(button.dropped)
    }

    @Test
    fun paint_doesNotDrawDebugRedBorder() {
        val button = FolderButton(SourcePackage(Paths.get("src")))
        val image = GuiRender.render(button, 180, button.preferredSize.height)
        assertFalse(GuiRender.hasColor(image, Color.RED, 20), "debug red border should not be painted")
    }

    @Test
    fun paint_keepsGlyphsVerticallyCentered() {
        val button = FolderButton(SourcePackage(Paths.get("browser")))
        val height = 24
        val image = GuiRender.render(button, 200, height)
        var minY = height
        var maxY = 0
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val color = GuiRender.rgb(image, x, y)
                val isContent = color.red > 180 && color.green > 180 && color.blue > 180 && color.alpha > 200
                if (isContent) {
                    minY = minOf(minY, y)
                    maxY = maxOf(maxY, y)
                }
            }
        }
        assertTrue(maxY > minY, "expected painted folder label/arrow pixels")
        val contentCenter = (minY + maxY) / 2.0
        assertEquals(height / 2.0, contentCenter, 5.0)
    }

    @Test
    fun hover_paintsSidebarHoverFill() {
        val button = FolderButton(SourcePackage(Paths.get("src")))
        GuiRender.hover(button)
        val image = GuiRender.render(button, 180, button.preferredSize.height)
        assertTrue(
            GuiRender.hasColor(image, EditorColors.sidebarHover()),
            "hovered folder row should paint sidebar hover fill"
        )
        assertTrue(
            GuiRender.isNear(GuiRender.rgb(image, 0, 0), EditorColors.sidebarBackground()),
            "hover fill should stay inset so the row corner remains the sidebar background"
        )
    }

    @Test
    fun paint_expandedChevronDiffersFromCollapsed() {
        val button = FolderButton(SourcePackage(Paths.get("src")))
        val height = button.preferredSize.height
        val collapsed = GuiRender.render(button, 180, height)
        button.dropped = true
        val expanded = GuiRender.render(button, 180, height)

        val x0 = ExplorerItemStyle.chevronX(0)
        val x1 = x0 + ExplorerItemStyle.CHEVRON_SIZE
        assertTrue(
            regionDiffers(collapsed, expanded, x0, 0, x1, height - 1),
            "expanded folder should paint a different chevron than the collapsed state"
        )
    }

    @Test
    fun selected_paintsSelectionFillAndAccent() {
        val button = FolderButton(SourcePackage(Paths.get("src")))
        button.selected = true
        val image = GuiRender.render(button, 180, button.preferredSize.height)
        assertTrue(GuiRender.hasColor(image, EditorColors.sidebarSelection()))
        assertTrue(GuiRender.hasColor(image, EditorColors.accentColor()))
    }

    companion object {
        fun regionDiffers(
            a: BufferedImage,
            b: BufferedImage,
            x0: Int,
            y0: Int,
            x1: Int,
            y1: Int
        ): Boolean {
            for (x in x0..x1) {
                for (y in y0..y1) {
                    if (a.getRGB(x, y) != b.getRGB(x, y)) {
                        return true
                    }
                }
            }
            return false
        }

        fun hasNonBackgroundIn(
            image: BufferedImage,
            x0: Int,
            y0: Int,
            x1: Int,
            y1: Int,
            background: Color
        ): Boolean {
            for (x in x0..x1) {
                for (y in y0..y1) {
                    if (!GuiRender.isNear(GuiRender.rgb(image, x, y), background, 8)) {
                        return true
                    }
                }
            }
            return false
        }
    }
}

class FileButtonGuiTest {
    @BeforeEach
    fun setUp() {
        Global.setCursor(EditorCursor(Document(), 0, 0))
        OpenFile.reset()
        OpenFile.showError = { _, _ -> }
        OpenFile.prompt = { UnsavedChoice.DISCARD }
    }

    @Test
    fun click_invokesOnFileClickedWithSourceFile() {
        val source = SourceFile(Paths.get("src/Main.java"))
        val button = FileButton(source, 1)
        var clicked: SourceFile? = null
        button.setOnFileClicked { clicked = it }

        button.size = button.preferredSize
        GuiRender.click(button)

        assertEquals(source, clicked)
        assertEquals(source.getPath(), clicked!!.getPath())
    }

    @Test
    fun paintsFileNameInsteadOfPlaceholder() {
        val button = FileButton(SourceFile(Paths.get("src/Main.java")), 1)
        assertEquals("Main.java", button.fileName)
        assertTrue(button.preferredSize.height > 8)

        val image = GuiRender.render(button, 220, button.preferredSize.height)
        assertTrue(
            GuiRender.hasLightGlyph(image, 0, 0, image.width - 1, image.height - 1),
            "file name glyphs should be visible"
        )
        assertFalse(GuiRender.hasColor(image, Color.RED, 20))
    }

    @Test
    fun paint_drawsFileIconInIconColumn() {
        val button = FileButton(SourceFile(Paths.get("src/Main.java")), 1)
        val height = button.preferredSize.height
        val image = GuiRender.render(button, 220, height)
        val x0 = ExplorerItemStyle.iconX(1)
        val y0 = ExplorerItemStyle.centeredY(height, ExplorerItemStyle.ICON_SIZE)
        val x1 = x0 + ExplorerItemStyle.ICON_SIZE
        val y1 = y0 + ExplorerItemStyle.ICON_SIZE
        assertTrue(
            FolderButtonGuiTest.hasNonBackgroundIn(image, x0, y0, x1, y1, EditorColors.sidebarBackground()),
            "file row should paint a document glyph in the icon column"
        )
    }

    @Test
    fun hover_paintsSidebarHoverFillOnNameOnlyButton() {
        val button = FileButton("Notes.txt")
        GuiRender.hover(button)
        val image = GuiRender.render(button, 180, button.preferredSize.height)
        assertTrue(
            GuiRender.hasColor(image, EditorColors.sidebarHover()),
            "hovered file row should paint sidebar hover fill"
        )
    }

    @Test
    fun openFile_paintsSelectionFillAndAccent() {
        val path = Paths.get("src/main/resources/testproject/src/test/Main.java")
        assertTrue(OpenFile.open(path))
        val button = FileButton(SourceFile(path), 1)
        val image = GuiRender.render(button, 220, button.preferredSize.height)
        assertTrue(
            GuiRender.hasColor(image, EditorColors.sidebarSelection()),
            "active file should paint the sidebar selection fill"
        )
        assertTrue(
            GuiRender.hasColor(image, EditorColors.accentColor()),
            "active file should paint the left accent stripe"
        )
    }
}
