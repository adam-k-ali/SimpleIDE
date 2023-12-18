package com.adamkali.simpleide.codearea;

import java.awt.*;

public class Text {
    // The text to be displayed
    private String text;
    // The width of the text in pixels (used for cursor positioning)
    private int stringWidth;

    public Text() {
        this.text = "";
        this.stringWidth = 0;
    }

    public String getText() {
        return text;
    }

    /**
     * Saves the updated text and updates stringWidth
     * @param text The new text
     * @param fontMetrics The fontMetrics of the font used to draw the text
     */
    public void setText(String text, FontMetrics fontMetrics) {
        this.text = text;
        stringWidth = fontMetrics.stringWidth(text);
    }

    public int getStringWidth() {
        return stringWidth;
    }
}
