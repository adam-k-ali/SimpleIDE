package com.adamkali.simpleide.browser.components;

import com.adamkali.simpleide.Global;
import com.adamkali.simpleide.editor.EditorCoordinates;
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
        });
    }

    public FileButton(String name, int level) {
        super();
        this.name = name;
        this.level = level;
        setFont(Global.getFont());
        setOpaque(true);
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        FontMetrics fm = g.getFontMetrics();
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        int indent = EditorCoordinates.treeIndent(level);
        g.setColor(getForeground());
        g.setFont(getFont());
        g.drawString(name, 16 + indent, textY);
    }

    @Override
    public Dimension getPreferredSize() {
        int width = getParent() != null && getParent().getWidth() > 0 ? getParent().getWidth() : 200;
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
