package com.adamkali.simpleide.codearea;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JComponent {
    public static final int STATUS_BAR_HEIGHT = 32;
    private static final int MARGIN_LEFT = 8;
    private static final int MARGIN_TOP = 4;
    private static final int LINE_HEIGHT = 20;


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.LIGHT_GRAY);

        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.drawString("SimpleIDE", MARGIN_LEFT, MARGIN_TOP + LINE_HEIGHT / 2);
        g.drawString("Line: 1, Column: 1", getWidth() - 200, MARGIN_TOP + LINE_HEIGHT / 2);
    }
}
