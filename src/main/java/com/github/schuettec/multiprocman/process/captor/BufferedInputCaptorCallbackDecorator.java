package com.github.schuettec.multiprocman.process.captor;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.schuettec.multiprocman.common.EventJoin;

public class BufferedInputCaptorCallbackDecorator implements InputCaptorCallback {

	InputCaptorCallback delegate;

	enum Op {
		newLine,
		append,
		jumpToLastLine;
	}

	private Map<Op, EventJoin> eventJoinMap = new Hashtable<>();
	private TimeUnit timeUnit;
	private long frequency;

	public BufferedInputCaptorCallbackDecorator(long frequency, TimeUnit timeUnit, InputCaptorCallback delegate) {
		super();
		this.frequency = frequency;
		this.timeUnit = timeUnit;
		this.delegate = delegate;
		createEventJoinTable(eventJoinMap);
	}

	private void createEventJoinTable(Map<Op, EventJoin> eventJoinMap) {
		// Arrays.stream(Op.values())
		// .forEach(op -> eventJoinMap.put(op, new EventJoin(getCallbackFor(op), frequency, timeUnit)));
	}

	@Override
	public boolean shouldRun() {
		return delegate.shouldRun();
	}

	@Override
	public void newLine(int lines, String string) {

	}

	@Override
	public void append(String string) {

	}

	@Override
	public void jumpToLastLine(int lines) {

	}

}
