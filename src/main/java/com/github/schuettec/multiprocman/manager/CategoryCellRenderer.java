package com.github.schuettec.multiprocman.manager;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class CategoryCellRenderer extends DefaultListCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
	    boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		Category category = (Category) value;
		this.setText(category.getName());
		this.setToolTipText(category.getDescription());
		this.setIcon(category.getIcon());
		return this;
	}
}
