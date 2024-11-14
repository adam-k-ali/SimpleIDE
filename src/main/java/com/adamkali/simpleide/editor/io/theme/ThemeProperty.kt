package com.adamkali.simpleide.editor.io.theme

data class ThemeProperty(
    val color: ColorProperty?
) {
    override fun toString(): String {
        return ("{" +
                "color=" + color +
                "}");
    }

    override fun equals(other: Any?): Boolean {
        return other is ThemeProperty && color == other.color
    }
}
