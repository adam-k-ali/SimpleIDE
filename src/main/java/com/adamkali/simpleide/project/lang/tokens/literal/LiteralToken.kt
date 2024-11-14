package com.adamkali.simpleide.project.lang.tokens.literal

import com.adamkali.simpleide.project.lang.tokens.Token
import java.awt.Color

open class LiteralToken(text: String, bgColor: Color, fgColor: Color) :
    Token(text, bgColor, fgColor) {
}