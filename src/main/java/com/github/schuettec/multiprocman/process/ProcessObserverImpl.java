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

import org.apache.commons.lang3.event.EventListenerSupport;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.process.captor.InputCaptor;
import com.github.schuettec.multiprocman.process.captor.InputCaptorCallback;

public class ProcessObserverImpl extends Thread implements ProcessObserver, ProcessOutputInfo, ViewFrameListener {

	private ProcessBuilder processBuilder;
	private File outputFile;

	private boolean running = false;
	private EventListenerSupport<ProcessCallback> callbacks;
	private Process process;
	private InputCaptor captor;

	public ProcessObserverImpl(ProcessBuilder builder, File outputFile) throws ProcessBufferOutputException {
		requireNonNull(builder, "Builder must not be null!");
		requireNonNull(outputFile, "Output file must not be null.");
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
		// TODO: Use charset of launcher config here.
		Charset defaultCharset = Charset.defaultCharset();
		try {
			this.process = processBuilder.start();
			running = true;
			InputStream inputStr = process.getInputStream();
			try (BufferedInputStream input = new BufferedInputStream(inputStr);
			    FileOutputStream output = new FileOutputStream(outputFile);) {
				callbacks.fire()
				    .started(this, outputFile, defaultCharset);
				this.captor = new InputCaptor(new InputCaptorCallback() {

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
				}, input, output);
				captor.run();

				try {
					boolean exited = process.waitFor(10l, TimeUnit.SECONDS);
					if (exited) {
						int exitValue = process.exitValue();
						callbacks.fire()
						    .exited(exitValue);
					} else {
						callbacks.fire()
						    .abandoned();
					}
				} catch (InterruptedException e) {
				}

			} catch (Exception e) {
				stopProcess();
				callbacks.fire()
				    .cannotWriteOutput(outputFile, e);
			}
		} catch (IOException e) {
			stopProcess();
			callbacks.fire()
			    .cannotStartProcess(e);
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
			// TODO: Wait for or destroy forcibly.
			running = false;
		}
	}

	@Override
	public void stopProcessForcibly() {
		ExceptionDialog.showException(null, new Exception("Not implemented!"), "Stop process forcibly not implemented!");
	}

	@Override
	public void waitFor() {
		ExceptionDialog.showException(null, new Exception("Not implemented!"), "Wait for process not implemented!");
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
		captor.setViewFrame(viewFrameLines);
	}

}
