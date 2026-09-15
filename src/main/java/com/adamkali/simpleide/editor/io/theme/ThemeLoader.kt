package com.adamkali.simpleide.editor.io.theme

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


object ThemeLoader {
    fun load(path: String): Theme {
        val file = File(path)
        if (file.exists()) {
            return loads(file.readText())
        }

        val classpathPath = path
            .removePrefix("src/main/resources/")
            .removePrefix("src/test/resources/")
        val stream = ThemeLoader::class.java.classLoader.getResourceAsStream(classpathPath)
            ?: throw java.io.FileNotFoundException("Theme file not found: $path")
        return loads(stream.bufferedReader().use { it.readText() })
    }

    fun loads(s: String): Theme {
        val propertyMapType = object : TypeToken<Map<String, ThemeProperty>>() {}.type
        val properties: Map<String, ThemeProperty> = Gson().fromJson(s, propertyMapType)
        return Theme(properties)
    }
}