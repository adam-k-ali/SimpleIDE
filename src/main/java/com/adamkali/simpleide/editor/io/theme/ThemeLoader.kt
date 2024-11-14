package com.adamkali.simpleide.editor.io.theme

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object ThemeLoader {
    fun loads(s: String): Theme {
        val propertyMapType = object : TypeToken<Map<String, ThemeProperty>>() {}.type
        val colorProperty = ColorProperty(intArrayOf(0, 0, 0, 255), intArrayOf(255, 255, 255, 255))

        val colorMap = mapOf("current_line_height" to colorProperty)
        val properties: Map<String, ThemeProperty> = Gson().fromJson(s, propertyMapType)
        return Theme(properties)
    }
}