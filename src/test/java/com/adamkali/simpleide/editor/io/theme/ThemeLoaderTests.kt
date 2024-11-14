package com.adamkali.simpleide.editor.io.theme

import org.junit.jupiter.api.Assertions

class ThemeLoaderTests {

    @org.junit.jupiter.api.Test
    fun loads() {
        val colorPropertyString: String = "{\n" +
                "  \"current_line_highlight\": {\n" +
                "    \"color\": {\n" +
                "      \"foreground\": [\n" +
                "        30,\n" +
                "        30,\n" +
                "        30,\n" +
                "        255\n" +
                "      ],\n" +
                "      \"background\": [\n" +
                "        200,\n" +
                "        200,\n" +
                "        200,\n" +
                "        255\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}"

        val colorMap =
            mapOf(
                "current_line_highlight" to ThemeProperty(
                    ColorProperty(
                        foreground = intArrayOf(30, 30, 30, 255),
                        background = intArrayOf(200, 200, 200, 255)
                    )
                )
            )
        val expectedTheme = Theme(colorMap)

        val loadedTheme = ThemeLoader.loads(colorPropertyString)
        Assertions.assertEquals(expectedTheme, loadedTheme)
    }
}