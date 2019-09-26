package com.github.schuettec.multiprocman.process;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.event.EventListenerSupport;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.process.captor.InputCaptor;
import com.github.schuettec.multiprocman.process.captor.InputCaptorCallback;
import com.github.schuettec.multiprocman.process.captor.SwingThreadInputCaptorCallbackDecorator;

public class ProcessObserverImpl extends Thread implements ProcessObserver, ProcessOutputInfo, ViewFrameListener {

	private ProcessBuilder processBuilder;
	private File outputFile;

	private boolean running = false;
	private EventListenerSupport<ProcessCallback> callbacks;
	private Process process;
	private InputCaptor captor;
	private Charset charset;

	public ProcessObserverImpl(ProcessBuilder builder, File outputFile, Charset charset)
	    throws ProcessBufferOutputException {
		requireNonNull(builder, "Builder must not be null!");
		requireNonNull(outputFile, "Output file must not be null.");
		this.charset = charset;
		this.processBuilder = builder;
		this.processBuilder.redirectErrorStream(true);
		this.outputFile = outputFile;
		this.callbacks = new EventListenerSupport<>(ProcessCallback.class);
	}

	public void addListener(ProcessCallback listener) {
		callbacks.addListener(listener);
	}

	public void removeListener(ProcessCallback listener) {
		callbacks.removeListener(listener);
	}

	@Override
	public void run() {
		try {
			this.process = processBuilder.start();
			running = true;
			InputStream inputStr = process.getInputStream();
			try (BufferedInputStream input = new BufferedInputStream(inputStr);
			    FileOutputStream output = new FileOutputStream(outputFile);) {

				doInSwing(new Runnable() {
					@Override
					public void run() {
						callbacks.fire()
						    .started(ProcessObserverImpl.this, outputFile, charset);
					}
				});
				this.captor = new InputCaptor(getInputCaptorCallback(), input, output);
				captor.run();

				_waitFor();

			} catch (Exception e) {
				stopProcess();
				doInSwing(new Runnable() {
					@Override
					public void run() {
						callbacks.fire()
						    .cannotWriteOutput(outputFile, e);
					}
				});
			}
		} catch (IOException e) {
			stopProcess();
			doInSwing(new Runnable() {
				@Override
				public void run() {
					callbacks.fire()
					    .cannotStartProcess(e);
				}
			});
		}
	}

	private InputCaptorCallback getInputCaptorCallback() {
		return new SwingThreadInputCaptorCallbackDecorator(new InputCaptorCallback() {

			@Override
			public boolean shouldRun() {
				return process.isAlive();
			}

			@Override
			public void newLine(int lines, String line) {
				callbacks.fire()
				    .output(lines, line);
			}

			@Override
			public void append(String string) {
				callbacks.fire()
				    .append(string);
			}

			@Override
			public void jumpToLastLine(int lines) {
				callbacks.fire()
				    .jumpToLastLine(lines);
			}
		});
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

	private void _waitFor() throws InterruptedException {
		boolean exited = process.waitFor(10l, TimeUnit.SECONDS);
		if (exited) {
			int exitValue = process.exitValue();
			doInSwing(new Runnable() {
				@Override
				public void run() {
					callbacks.fire()
					    .exited(exitValue);
				}
			});
		} else {
			doInSwing(new Runnable() {
				@Override
				public void run() {
					callbacks.fire()
					    .abandoned();
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see livefilereader.ProcessObserver#stopProcess()
	 */
	@Override
	public void stopProcess() {
		if (nonNull(this.process)) {
			this.process.destroy();
			running = false;
		}
	}

	@Override
	public void waitFor() {
		try {
			_waitFor();
		} catch (InterruptedException e) {
			ExceptionDialog.showException(null, e, "Error while waiting for process to finish!");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see livefilereader.ProcessObserver#getLines()
	 */
	@Override
	public long getLines() {
		return captor.getLines();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see livefilereader.ProcessObserver#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void startProcess() {
		if (running) {
			throw new IllegalStateException("Process is already started.");
		} else {
			start();
		}
	}

	@Override
	public File getFile() {
		return outputFile;
	}

	@Override
	public int getStartOffset(int lineNumber) {
		return captor.getStartOffset(lineNumber);
	}

	@Override
	public int getEndOffset(int lineNumber) {
		return captor.getEndOffset(lineNumber);
	}

	@Override
	public void viewFrameChanged(int viewFrameLines) {
		if (nonNull(captor)) {
			captor.setViewFrame(viewFrameLines);
		}
	}

}
