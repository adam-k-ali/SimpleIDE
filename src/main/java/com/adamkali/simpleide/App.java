package com.adamkali.simpleide;

import com.adamkali.simpleide.browser.ProjectBrowser;
import com.adamkali.simpleide.editor.CodeEditor;
import com.adamkali.simpleide.editor.StatusBar;

import javax.swing.*;
import java.awt.*;

public class App {
    private JFrame mainWindow;
    public static StatusBar statusBar;
    private static CodeEditor codeEditor;

    /**
     * Creates the JFrame and sets the size and title
     */
    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainWindow = new JFrame("SimpleIDE");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setSize(800, 600);
        mainWindow.setLocationRelativeTo(null);

        statusBar = new StatusBar();

        run();
    }

    private JPanel editorPanel() {
        JPanel editorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints layout = new GridBagConstraints();
        layout.fill = GridBagConstraints.BOTH;
        editorPanel.setPreferredSize(new Dimension(mainWindow.getWidth(), mainWindow.getHeight()));

        // Create ProjectBrowser
        ProjectBrowser projectBrowser = new ProjectBrowser();
        projectBrowser.setPreferredSize(new Dimension(100, 800));

        layout.weightx = 0.2;
        layout.weighty = 1;
        layout.gridx = 0;
        layout.gridy = 0;
        editorPanel.add(projectBrowser, layout);

        // Create CodeEditor
        codeEditor = new CodeEditor();
        codeEditor.setPreferredSize(new Dimension(800, 800));

        JViewport viewport = new JViewport();
        viewport.setView(codeEditor);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewport(viewport);
        scrollPane.setBounds(0, 0, 800, 500);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        layout.weightx = 1;
        layout.weighty = 1;
        layout.gridx = 1;
        layout.gridy = 0;
        editorPanel.add(scrollPane, layout);
        return editorPanel;
    }

    private JPanel statusPanel() {
        JPanel statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(mainWindow.getWidth(), StatusBar.STATUS_BAR_HEIGHT));
        statusBar.setPreferredSize(new Dimension(mainWindow.getWidth(), StatusBar.STATUS_BAR_HEIGHT));

        statusPanel.add(statusBar);
        return statusPanel;
    }

    /**
     * Creates the main panel and adds it to the JFrame
     * Has the following layout:
     * +----------------+----------------+
     * | Options                         |
     * |Project Browser|    Code Editor  |
     * | Status Bar                       |
     * +----------------+----------------+
     */
    public void run() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(editorPanel());
        container.add(statusPanel());

        mainWindow.add(container);

        // Set mainWindow to be visible
        mainWindow.setVisible(true);

        // Create thread to repaint the codeEditor 60 times per second
        Thread thread = new Thread(() -> {
            while (true) {
                codeEditor.update();
                try {
                    Thread.sleep(1000 / 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {
        App app = new App();
        app.init();
    }
}
