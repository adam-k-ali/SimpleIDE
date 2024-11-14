package com.adamkali.simpleide.editor.io.theme

data class Theme(
    val properties: Map<String, ThemeProperty>
) {
    override fun equals(other: Any?): Boolean {
        return other is Theme && properties == other.properties
    }
}
