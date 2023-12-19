package com.adamkali.simpleide.codearea;

import java.awt.*;

public class Text {
    public static final Text EMPTY = new Text();

    // The text to be displayed
    private String text;

    public Text() {
        this.text = "";
    }

    public String getText() {
        return text;
    }

    /**
     * Saves the updated text and updates stringWidth
     * @param text The new text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Inserts text at the specified index
     * @param text The text to be inserted
     * @param index The index to insert the text at
     */
    public void insert(String text, int index) {
        if (index < 0 || index > this.text.length()) {
            throw new IndexOutOfBoundsException();
        }

        this.text = this.text.substring(0, index) + text + this.text.substring(index);
    }

    public int getStringWidth(FontMetrics fontMetrics) {
        return fontMetrics.stringWidth(text);
    }
}
