package com.adamkali.simpleide.editor.io.theme

object ThemeLoader {
    fun loads(s: String): Theme {
        val colorProperty = ColorProperty(intArrayOf(0, 0, 0, 255), intArrayOf(255, 255, 255, 255))

        val colorMap = mapOf("current_line_height" to colorProperty)
        return Theme(colorMap)
    }
}