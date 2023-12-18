package com.adamkali.simpleide;

import com.adamkali.simpleide.codearea.CodeArea;

import javax.swing.*;

public class App {
    private JFrame mainWindow;

    /**
     * Creates the JFrame and sets the size and title
     */
    public void init() {
        mainWindow = new JFrame("SimpleIDE");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setSize(800, 600);
        mainWindow.setLocationRelativeTo(null);


        run();
    }

    public void run() {
        CodeArea codeArea = new CodeArea();
        mainWindow.add(codeArea);

        // Set mainWindow to be visible
        mainWindow.setVisible(true);

        // Initial paint of the codeArea
//        codeArea.paint(codeArea.getGraphics());

        // Create thread to repaint the codeArea 60 times per second
        Thread thread = new Thread(() -> {
            while (true) {
                codeArea.update();
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
