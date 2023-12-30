package com.adamkali.simpleide;

import com.adamkali.simpleide.editor.CodeEditor;
import com.adamkali.simpleide.editor.StatusBar;

import javax.swing.*;
import java.awt.*;

public class App {
    private JFrame mainWindow;

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


        run();
    }

    public void run() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints layout = new GridBagConstraints();

        // Make the panel take up the entire window
        layout.fill = GridBagConstraints.BOTH;
        mainPanel.setPreferredSize(new Dimension(mainWindow.getWidth(), mainWindow.getHeight()));

        CodeEditor codeEditor = new CodeEditor();

        JViewport viewport = new JViewport();
        viewport.setView(codeEditor);
//        viewport.setViewSize(new Dimension(800, 800));
//        viewport.setBounds(0, 0, 800, 800);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewport(viewport);
        scrollPane.setBounds(0, 0, 800, 500);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        layout.weightx = 1;
        layout.weighty = 1;
        layout.gridx = 0;
        layout.gridy = 0;
        mainPanel.add(scrollPane, layout);

        StatusBar statusBar = new StatusBar();
        statusBar.setBounds(0, 0, mainWindow.getWidth(), StatusBar.STATUS_BAR_HEIGHT);
        statusBar.setPreferredSize(new Dimension(mainWindow.getWidth(), StatusBar.STATUS_BAR_HEIGHT));

        layout.weightx = 1;
        layout.weighty = 0.05;
        layout.gridx = 0;
        layout.gridy = 1;
        mainPanel.add(statusBar, layout);

        mainWindow.add(mainPanel);

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
     * @param args
     */
    public static void main(String[] args) {
        App app = new App();
        app.init();
    }
}
