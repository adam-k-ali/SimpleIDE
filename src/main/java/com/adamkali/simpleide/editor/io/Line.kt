package com.adamkali.simpleide.editor.io

import com.adamkali.simpleide.project.lang.Lexer
import com.adamkali.simpleide.project.lang.tokens.TabToken
import com.adamkali.simpleide.project.lang.tokens.Token
import com.adamkali.simpleide.project.lang.tokens.NewLineToken
import com.adamkali.simpleide.project.lang.tokens.WhitespaceToken

class Line {
    var tokens: MutableList<Token> = mutableListOf()
        private set

    /**
     * Replaces the current line with a given line.
     * Confirms there are no line breaks in the middle of the line.
     * @param line The line to replace the current line with.
     * @throws IllegalArgumentException If there are line breaks in the middle of the line.
     */
    fun rewrite(line: String) {
        val newTokens = Lexer.lex(line)
        for (i in 0 until newTokens.size) {
            if (newTokens[i] is NewLineToken && i != tokens.size - 1) {
                throw IllegalArgumentException("NewLineToken must be the last token in the line")
            }
        }
        tokens.clear()
        tokens.addAll(Lexer.lex(line))
    }

    /**
     * Appends a string to the end of the line.
     * @param text The string to append.
     */
    fun append(text: String) {
        rewrite(toString() + text)
    }

    fun indentations(): Int {
        var indentations = 0
        for (token in tokens) {
            if (token !is TabToken && token !is WhitespaceToken) {
                break
            }

            if (token is TabToken) {
                indentations += 4
            } else {
                indentations++
            }
        }
        return indentations
    }


    /**
     * Removes a token from the line.
     * @param index The index of the token to remove.
     */
    fun removeToken(index: Int) {
        tokens.removeAt(index)
    }


    /**
     * Returns the character length of the line.
     * @return The character length of the line.
     */
    fun length(): Int {
        var length = 0
        for (token in tokens) {
            length += token.length()
        }
        return length
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (token in tokens) {
            sb.append(token.text)
        }
        return sb.toString()
    }

    /**
     * Returns a substring of the line.
     * @param start The index of the first character to include in the substring.
     * @param end The index of the last character to include in the substring.
     * @return The substring.
     */
    fun substring(start: Int, end: Int): String {
        if (start < 0 || end < 0 || start > end || end > length()) {
            throw IndexOutOfBoundsException("Index out of bounds (start: $start, end: $end)");
        }
        return toString().substring(start, end)
    }
}