package livefilereader;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import livefilereader.captor.InputCaptor;
import livefilereader.captor.InputCaptorCallback;

public class ProcessObserverImpl extends Thread implements ProcessObserver, ProcessOutputInfo {

	private ProcessBuilder processBuilder;
	private File outputFile;

	private boolean running = false;
	private ProcessCallback callback;
	private Process process;
	private InputCaptor captor;

	public ProcessObserverImpl(ProcessBuilder builder, File outputFile, ProcessCallback callback)
	    throws ProcessBufferOutputException {
		requireNonNull(builder, "Builder must not be null!");
		requireNonNull(outputFile, "Output file must not be null.");
		requireNonNull(callback, "The callback must not be null.");
		this.processBuilder = builder;
		this.processBuilder.redirectErrorStream(true);
		this.outputFile = outputFile;
		this.callback = callback;
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
				callback.started(this, outputFile, defaultCharset);
				this.captor = new InputCaptor(new InputCaptorCallback() {

					@Override
					public boolean shouldRun() {
						return process.isAlive();
					}

					@Override
					public void newLine(int lines) {
						callback.output(lines);
					}

					@Override
					public void append(String string) {
						callback.append(string);
					}
				}, input, output);
				captor.run();
				callback.exited();
			} catch (Exception e) {
				stopProcess();
				callback.cannotWriteOutput(outputFile, e);
			}
		} catch (IOException e) {
			stopProcess();
			callback.cannotStartProcess(e);
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
			// Wait for or destroy forcibly.
			running = false;
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

	@Override
	public int getLineByteOffset(int line) {
		return captor.getLineByteOffset(line);
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
	public int getLineEnd(int line) {
		return captor.getLineEnd(0);
	}

}
