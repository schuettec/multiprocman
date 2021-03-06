package com.github.schuettec.multiprocman.manager;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import com.github.schuettec.multiprocman.ProcessDescriptor;

public class ProcessCellRenderer extends DefaultListCellRenderer {
    private static final int WIDTH = 100;
    private static final int HEIGHT = 70;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean desktopStyle = true;

    public ProcessCellRenderer() {
        super();
    }

    public ProcessCellRenderer(boolean desktopStyle) {
        super();
        this.desktopStyle = desktopStyle;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        ProcessDescriptor processDescriptor = (ProcessDescriptor) value;
        this.setText(processDescriptor.getTitle());
        this.setToolTipText(processDescriptor.getCommand());
        this.setIcon(processDescriptor.getIcon());
        if (desktopStyle) {
            this.setHorizontalTextPosition(JLabel.CENTER);
            this.setVerticalTextPosition(JLabel.BOTTOM);
            this.setHorizontalAlignment(JLabel.CENTER);
            this.setSize(new Dimension(WIDTH, HEIGHT));
            this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
            this.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        }
        return this;
    }
}
