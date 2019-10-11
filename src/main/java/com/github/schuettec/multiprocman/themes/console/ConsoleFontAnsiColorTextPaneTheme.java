package com.github.schuettec.multiprocman.themes.console;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;

import com.github.schuettec.multiprocman.console.AnsiColorTextPane;

public class ConsoleFontAnsiColorTextPaneTheme implements AnsiColorTextPaneTheme {

    @Override
    public void apply(AnsiColorTextPane ansiColorTextPane) {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        ansiColorTextPane.setFont(font);
    }

    @Override
    public void reset(AnsiColorTextPane ansiColorTextPane) {
        Color color = UIManager.getColor("EditorPane.background");
        ansiColorTextPane.setBackground(color);
    }
}
