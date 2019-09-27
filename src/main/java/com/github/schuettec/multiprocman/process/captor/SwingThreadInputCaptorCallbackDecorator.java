package com.github.schuettec.multiprocman.process.captor;

import javax.swing.SwingUtilities;

public class SwingThreadInputCaptorCallbackDecorator implements InputCaptorCallback {

	private InputCaptorCallback delegate;

	public SwingThreadInputCaptorCallbackDecorator(InputCaptorCallback delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public boolean shouldRun() {
		return delegate.shouldRun();
	}

	@Override
	public void newLine(int lines, String string) {
		doInSwing(() -> delegate.newLine(lines, string));
	}

	@Override
	public void append(String string) {
		doInSwing(() -> delegate.append(string));
	}

	@Override
	public void jumpToLastLine(int lines) {
		doInSwing(() -> delegate.jumpToLastLine(lines));
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	private void doInSwing(Runnable run) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					run.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
