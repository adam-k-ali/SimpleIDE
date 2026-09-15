package com.adamkali.simpleide.browser.components;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.EditorCoordinates;
import com.adamkali.simpleide.editor.io.OpenFile;
import com.adamkali.simpleide.preferences.EditorColors;
import com.adamkali.simpleide.project.SourceFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class FileButton extends JComponent {
    private final String name;
    private final int level;
    private SourceFile sourceFile;
    private Consumer<SourceFile> onFileClicked;
    private boolean hovered;

    public FileButton(String name) {
        this(name, 0);
    }

    public FileButton(SourceFile file, int level) {
        this(file.getFileName(), level);
        this.sourceFile = file;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onFileClicked != null && sourceFile != null) {
                    onFileClicked.accept(sourceFile);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public FileButton(String name, int level) {
        super();
        this.name = name;
        this.level = level;
        setFont(Global.getFont());
        setOpaque(true);
        setBackground(EditorColors.sidebarBackground());
        setForeground(EditorColors.sidebarForeground());
        setAlignmentX(LEFT_ALIGNMENT);
    }

    public void setOnFileClicked(Consumer<SourceFile> onFileClicked) {
        this.onFileClicked = onFileClicked;
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public String getFileName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    private boolean isActiveFile() {
        return sourceFile != null
                && OpenFile.INSTANCE.getPath() != null
                && OpenFile.INSTANCE.getPath().equals(sourceFile.getPath());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isActiveFile()) {
            g.setColor(EditorColors.sidebarSelection());
        } else if (hovered) {
            g.setColor(EditorColors.sidebarHover());
        } else {
            g.setColor(EditorColors.sidebarBackground());
        }
        g.fillRect(0, 0, getWidth(), getHeight());

        FontMetrics fm = g.getFontMetrics();
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        int indent = EditorCoordinates.treeIndent(level);
        g.setColor(EditorColors.sidebarForeground());
        g.setFont(getFont());
        g.drawString(name, 16 + indent, textY);
    }

    @Override
    public Dimension getPreferredSize() {
        int indent = EditorCoordinates.treeIndent(level);
        int textWidth = Global.getStringWidth(name);
        int width = 16 + indent + textWidth + 8;
        return new Dimension(width, Global.getLineHeight() + 6);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(80, Global.getLineHeight() + 6);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    public static enum FileButtonState {
        IDLE,
        SELECTED
    }
}
