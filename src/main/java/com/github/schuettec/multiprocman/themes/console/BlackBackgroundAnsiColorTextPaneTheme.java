package com.github.schuettec.multiprocman.themes.console;

import java.awt.Color;

import javax.swing.UIManager;

import com.github.schuettec.multiprocman.console.AnsiColorTextPane;

public class BlackBackgroundAnsiColorTextPaneTheme extends ConsoleFontAnsiColorTextPaneTheme {

    @Override
    public void apply(AnsiColorTextPane ansiColorTextPane) {
        super.apply(ansiColorTextPane);
        ansiColorTextPane.setBackground(Color.BLACK);
    }

    @Override
    public void reset(AnsiColorTextPane ansiColorTextPane) {
        super.reset(ansiColorTextPane);
        Color color = UIManager.getColor("EditorPane.background");
        ansiColorTextPane.setBackground(color);
    }
}
