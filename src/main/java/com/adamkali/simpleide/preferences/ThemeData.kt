package com.adamkali.simpleide.preferences

import com.adamkali.simpleide.editor.io.theme.Theme
import com.adamkali.simpleide.editor.io.theme.ThemeProperty

class ThemeData(private val theme: Theme) {

    fun getProperty(key: String): ThemeProperty {
        if (!theme.properties.containsKey(key)) {
            throw IllegalArgumentException("Property $key not found")
        }
        return theme.properties.getValue(key)
    }
}