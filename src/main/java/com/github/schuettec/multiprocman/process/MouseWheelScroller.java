package com.github.schuettec.multiprocman.process;

import static java.util.Objects.requireNonNull;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;

public class MouseWheelScroller implements MouseWheelListener {

	private JScrollBar scrollBar;

	public MouseWheelScroller(JScrollBar scrollBar) {
		super();
		requireNonNull(scrollBar);
		this.scrollBar = scrollBar;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		BoundedRangeModel model = scrollBar.getModel();
		int extent = (int) Math.round(model.getExtent() / 2.0d);
		int valueSummand = e.getWheelRotation() * extent;
		int newValue = model.getValue() + valueSummand;
		model.setValue(newValue);
	}

}
