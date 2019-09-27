package com.github.schuettec.multiprocman.process.captor;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

public class StopwatchInputCaptorCallbackDecorator implements InputCaptorCallback {

	private InputCaptorCallback delegate;

	public StopwatchInputCaptorCallbackDecorator(InputCaptorCallback delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public boolean shouldRun() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		boolean retVal = delegate.shouldRun();
		stopWatch.stop();
		System.out.println("Should run took " + stopWatch.getTime(TimeUnit.MILLISECONDS));
		return retVal;
	}

	@Override
	public void newLine(int lines, String string) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		delegate.newLine(lines, string);
		stopWatch.stop();
		System.out.println("newLine took " + stopWatch.getTime(TimeUnit.MILLISECONDS));
	}

	@Override
	public void append(String string) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		delegate.append(string);
		stopWatch.stop();
		System.out.println("append took " + stopWatch.getTime(TimeUnit.MILLISECONDS));
	}

	@Override
	public void jumpToLastLine(int lines) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		delegate.jumpToLastLine(lines);
		stopWatch.stop();
		System.out.println("jumpToLastLine took " + stopWatch.getTime(TimeUnit.MILLISECONDS));
	}

	@Override
	public void clear() {
		delegate.clear();
	}

}
