package com.adamkali.simpleide.testsupport

import java.awt.Color
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.SwingUtilities

object GuiRender {
    fun render(component: JComponent, width: Int, height: Int): BufferedImage {
        component.setSize(width, height)
        component.doLayout()
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        component.paint(graphics)
        graphics.dispose()
        return image
    }

    fun click(
        component: JComponent,
        x: Int = 2,
        y: Int = 2,
        modifiers: Int = 0,
        clickCount: Int = 1
    ) {
        val event = MouseEvent(
            component,
            MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(),
            modifiers,
            x,
            y,
            clickCount,
            false
        )
        component.dispatchEvent(event)
    }

    fun hover(component: JComponent, entered: Boolean = true, x: Int = 2, y: Int = 2) {
        val id = if (entered) MouseEvent.MOUSE_ENTERED else MouseEvent.MOUSE_EXITED
        component.dispatchEvent(
            MouseEvent(component, id, System.currentTimeMillis(), 0, x, y, 0, false)
        )
    }

    fun drag(component: JComponent, x1: Int, y1: Int, x2: Int, y2: Int, modifiers: Int = 0) {
        component.dispatchEvent(
            MouseEvent(component, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), modifiers or InputEvent.BUTTON1_DOWN_MASK, x1, y1, 1, false)
        )
        component.dispatchEvent(
            MouseEvent(component, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), modifiers or InputEvent.BUTTON1_DOWN_MASK, x2, y2, 1, false)
        )
        component.dispatchEvent(
            MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), modifiers, x2, y2, 1, false)
        )
    }

    fun rgb(image: BufferedImage, x: Int, y: Int): Color {
        return Color(image.getRGB(x, y), true)
    }

    fun isNear(color: Color, other: Color, tolerance: Int = 8): Boolean {
        return Math.abs(color.red - other.red) <= tolerance &&
                Math.abs(color.green - other.green) <= tolerance &&
                Math.abs(color.blue - other.blue) <= tolerance
    }

    fun hasDarkGlyph(image: BufferedImage, x0: Int, y0: Int, x1: Int, y1: Int): Boolean {
        val minX = x0.coerceIn(0, image.width - 1)
        val minY = y0.coerceIn(0, image.height - 1)
        val maxX = x1.coerceIn(0, image.width - 1)
        val maxY = y1.coerceIn(0, image.height - 1)
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                val color = rgb(image, x, y)
                if (color.alpha > 200 && color.red < 40 && color.green < 40 && color.blue < 40) {
                    return true
                }
            }
        }
        return false
    }

    fun hasLightGlyph(image: BufferedImage, x0: Int, y0: Int, x1: Int, y1: Int): Boolean {
        val minX = x0.coerceIn(0, image.width - 1)
        val minY = y0.coerceIn(0, image.height - 1)
        val maxX = x1.coerceIn(0, image.width - 1)
        val maxY = y1.coerceIn(0, image.height - 1)
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                val color = rgb(image, x, y)
                if (color.alpha > 200 && color.red > 180 && color.green > 180 && color.blue > 180) {
                    return true
                }
            }
        }
        return false
    }

    fun hasColor(image: BufferedImage, target: Color, tolerance: Int = 5): Boolean {
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                if (isNear(rgb(image, x, y), target, tolerance) && rgb(image, x, y).alpha > 200) {
                    return true
                }
            }
        }
        return false
    }

    fun flushEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait {}
        }
    }
}
