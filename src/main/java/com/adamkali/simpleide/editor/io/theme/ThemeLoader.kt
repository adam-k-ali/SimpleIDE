package com.adamkali.simpleide.editor.io.theme

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object ThemeLoader {
    fun loads(s: String): Theme {
        val propertyMapType = object : TypeToken<Map<String, ThemeProperty>>() {}.type
        val properties: Map<String, ThemeProperty> = Gson().fromJson(s, propertyMapType)
        return Theme(properties)
    }
}