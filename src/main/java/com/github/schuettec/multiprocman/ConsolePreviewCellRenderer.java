package com.github.schuettec.multiprocman;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ConsolePreviewCellRenderer implements ListCellRenderer<ProcessController> {

	@Override
	public Component getListCellRendererComponent(JList<? extends ProcessController> list, ProcessController value,
	    int index, boolean isSelected, boolean cellHasFocus) {
		value.getConsolePreview()
		    .setIsSelected(isSelected);
		return value.getConsolePreview();
	}
}
