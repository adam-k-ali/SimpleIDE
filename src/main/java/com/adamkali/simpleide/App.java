package com.adamkali.simpleide;

import com.adamkali.simpleide.window.AppWindow;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> AppWindow.INSTANCE.run());
    }
}
