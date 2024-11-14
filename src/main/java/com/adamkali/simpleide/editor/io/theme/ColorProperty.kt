package com.adamkali.simpleide.editor.io.theme

data class ColorProperty(
    val foreground: IntArray,
    val background: IntArray
) {
    override fun toString(): String {
        return ("{" +
                "foreground=[" + foreground[0] + ", " + foreground[1] + ", " + foreground[2] + ", " + foreground[3] + "], " +
                "background=[" + background[0] + ", " + background[1] + ", " + background[2] + ", " + background[3] + "]" +
                "}");
    }

    override fun equals(other: Any?): Boolean {
        return other is ColorProperty && foreground.contentEquals(other.foreground) && background.contentEquals(other.background)
    }
}