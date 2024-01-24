package com.adamkali.simpleide.browser;

import javax.swing.*;
import java.awt.*;

public class ProjectBrowser extends JPanel {
    public ProjectBrowser() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel("Project Browser"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background (white)
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw border (gray)
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        
    }
}
