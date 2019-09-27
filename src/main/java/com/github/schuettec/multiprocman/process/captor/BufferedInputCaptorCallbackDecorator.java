package com.github.schuettec.multiprocman.process.captor;

import java.util.concurrent.TimeUnit;

import com.github.schuettec.multiprocman.common.CumulatedEventJoin;

@Deprecated
public class BufferedInputCaptorCallbackDecorator implements InputCaptorCallback {

	InputCaptorCallback delegate;

	private CumulatedEventJoin<NewLineBuffer> newLineCumulator;
	private CumulatedEventJoin<NewLineBuffer> appendCumulator;
	private CumulatedEventJoin<NewLineBuffer> jumpCumulator;

	public BufferedInputCaptorCallbackDecorator(long frequency, TimeUnit timeUnit, InputCaptorCallback delegate) {
		super();
		this.delegate = delegate;
		this.newLineCumulator = new CumulatedEventJoin<NewLineBuffer>(NewLineBuffer::new, (s, t) -> {
			return s.add(t);
		}, (r) -> delegate.newLine(r.lines, r.newLines.toString()), frequency, timeUnit);

		this.appendCumulator = new CumulatedEventJoin<NewLineBuffer>(NewLineBuffer::new, (s, t) -> {
			return s.add(t);
		}, (r) -> delegate.append(r.newLines.toString()), frequency, timeUnit);

		this.jumpCumulator = new CumulatedEventJoin<NewLineBuffer>(NewLineBuffer::new, (s, t) -> {
			return s.add(t);
		}, (r) -> delegate.jumpToLastLine(r.lines), frequency, timeUnit);
	}

	@Override
	public boolean shouldRun() {
		return delegate.shouldRun();
	}

	@Override
	public void newLine(int lines, String string) {
		this.newLineCumulator.noticeEvent(new NewLineBuffer(lines, string));
	}

	@Override
	public void append(String string) {
		// this.appendCumulator.noticeEvent(new NewLineBuffer(string));
		delegate.append(string);
	}

	@Override
	public void jumpToLastLine(int lines) {
		this.jumpCumulator.noticeEvent(new NewLineBuffer(lines));
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	class NewLineBuffer {
		public int lines;
		public StringBuilder newLines;

		public NewLineBuffer() {
			super();
			this.lines = 0;
			this.newLines = new StringBuilder();
		}

		public NewLineBuffer(int lines) {
			super();
			this.lines = lines;
			this.newLines = new StringBuilder();
		}

		public NewLineBuffer(int lines, String newLine) {
			super();
			this.lines = lines;
			this.newLines = new StringBuilder(newLine);
		}

		public NewLineBuffer(String newLine) {
			super();
			this.newLines = new StringBuilder(newLine);
		}

		public NewLineBuffer add(NewLineBuffer toAdd) {
			this.lines = Math.max(this.lines, toAdd.lines);
			this.newLines.append(toAdd.newLines.toString());
			return this;
		}

	}

}
