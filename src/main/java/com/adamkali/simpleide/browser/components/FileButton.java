package com.adamkali.simpleide.browser.components;

import javax.swing.*;
import java.awt.*;

public class FileButton extends JComponent {
    public FileButton(String name) {
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString("File View", 10, 10);
    }

    public static enum FileButtonState {
        IDLE,
        SELECTED
    }
}
