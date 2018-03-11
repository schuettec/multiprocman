package com.github.schuettec.multiprocman;

import java.awt.Color;

import org.apache.commons.lang3.event.EventListenerSupport;

public class CounterState {

	@FunctionalInterface
	public interface CounterListener {
		public void counterChanged(CounterState state);
	}

	private EventListenerSupport<CounterListener> counterListener = EventListenerSupport.create(CounterListener.class);
	private Counter descriptor;
	private int count;

	public CounterState(Counter descriptor) {
		super();
		this.descriptor = descriptor;
		this.count = 0;
	}

	public void addListener(CounterListener listener) {
		counterListener.addListener(listener);
	}

	public void removeListener(CounterListener listener) {
		counterListener.removeListener(listener);
	}

	public int getCount() {
		return count;
	}

	public void clear() {
		this.count = 0;
		counterListener.fire()
		    .counterChanged(this);
	}

	public void processLine(String line) {
		if (descriptor.matches(line)) {
			count++;
			counterListener.fire()
			    .counterChanged(this);
		}
	}

	public Color getColor() {
		return descriptor.getColor();
	}

	public boolean testRegexp() {
		return descriptor.testRegexp();
	}

	public boolean matches(String string) {
		return descriptor.matches(string);
	}

	public String getName() {
		return descriptor.getName();
	}

	public String getRegexp() {
		return descriptor.getRegexp();
	}

}
