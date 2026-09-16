package com.adamkali.simpleide.browser.components

import com.adamkali.simpleide.project.SourceFile
import com.adamkali.simpleide.project.SourcePackage
import com.adamkali.simpleide.testsupport.GuiRender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Color
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
}

class FileButtonGuiTest {
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
}
