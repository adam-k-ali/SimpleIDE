package com.adamkali.simpleide.project.lang.tokens

import com.adamkali.simpleide.project.lang.tokens.literal.LiteralToken
import org.apache.commons.lang.StringEscapeUtils
import java.awt.Color

abstract class Token(
    val text: String,
    private val backgroundColor: Color,
    private val foregroundColor: Color
) {

    var valid = true

    fun getBackgroundColor(): Color {
        return backgroundColor
    }

    fun getForegroundColor(): Color {
        return foregroundColor
    }
//
//    fun getText(): String {
//        return text
//    }

    fun length(): Int {
        return text.length
    }

    override fun toString(): String {
        return if (this is IdentifierToken || this is LiteralToken) {
            this.javaClass.simpleName + " [text = \"" + StringEscapeUtils.escapeJava(text) + "\"]"
        } else {
            this.javaClass.simpleName
        }
    }
}