package com.github.schuettec.multiprocman;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;

public class CounterExpressions implements AppendListener {

	private ProcessController controller;
	private List<CounterState> counters;
	private List<JButton> buttons;

	public CounterExpressions(ProcessController controller) {
		super();
		this.controller = controller;
		this.counters = new LinkedList<>();
		this.buttons = new LinkedList<>();
		ProcessDescriptor descriptor = controller.getProcessDescriptor();
		if (descriptor.hasCounters()) {
			if (counters.isEmpty()) {
				createCounters();
			}
		}
	}

	public List<JButton> getButtons() {
		return Collections.unmodifiableList(buttons);
	}

	@Override
	public void append(Color c, String s) {
		List<String> lines = TextUtil.getLinesFromString(s);
		ProcessDescriptor descriptor = controller.getProcessDescriptor();
		if (descriptor.hasCounters()) {
			for (String line : lines) {
				for (CounterState state : counters) {
					state.processLine(line);
				}
			}
		}
	}

	public List<CounterState> getCounters() {
		return Collections.unmodifiableList(counters);
	}

	public List<CounterState> getFirst(int size) {
		List<CounterState> first = new ArrayList<>(size);
		for (int i = 0; i < counters.size() && i < size; i++) {
			first.add(counters.get(i));
		}
		return Collections.unmodifiableList(first);
	}

	private void createCounters() {
		clear();
		ProcessDescriptor descriptor = controller.getProcessDescriptor();
		for (Counter counter : descriptor.getCounters()) {
			CounterState counterState = new CounterState(counter);
			counters.add(counterState);
			JButton counterButton = createCounterButton(counterState);
			counterState.addListener(state -> {
				counterButton.setText(getButtonTitle(state));
				counterButton.repaint();
			});
			buttons.add(counterButton);
		}
	}

	private String getButtonTitle(CounterState state) {
		Color color = state.getColor();
		String colorStr = String.format("rgb(%d, %d, %d);", color.getRed(), color.getGreen(), color.getBlue());
		return "<html><span style='background-color:" + colorStr + "'>&nbsp;&nbsp;" + state.getCount()
		    + "&nbsp;&nbsp;</span> " + state.getName() + "</html>";
	}

	private JButton createCounterButton(CounterState counterState) {
		String actionTitle = getButtonTitle(counterState);
		JButton counterButton = new JButton(new AbstractAction(actionTitle) {
			@Override
			public void actionPerformed(ActionEvent e) {
				counterState.clear();
				controller.updateListeners();
			}
		});
		counterButton.setToolTipText("Click to reset " + counterState.getName());
		return counterButton;
	}

	public void clear() {
		for (CounterState s : counters) {
			s.clear();
		}
	}

}
