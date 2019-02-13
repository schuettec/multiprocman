package livefilereader;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessObserverImpl extends Thread implements ProcessObserver, ProcessOutputInfo {

	private ProcessBuilder processBuilder;
	private File outputFile;

	private ConcurrentHashMap<Integer, Integer> lineXrefs = new ConcurrentHashMap<>();

	private int lines = 0;
	private boolean running = false;
	private ProcessCallback callback;
	private Process process;

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

	/**
	 * Returns the start byte offset of the specified line. The lines are counted starting with 0.
	 *
	 * @param line The line number starting with 0.
	 */
	@Override
	public int getLineByteOffset(int line) {
		if (line <= 0) {
			return 0;
		} else if (lineXrefs.size() > line) {
			return lineXrefs.get(line);
		} else {
			System.out.println("Size: " + lineXrefs.size() + " line: " + line);
			throw new IllegalArgumentException(
			    String.format("Index out of bounds. Got %d lines but requesting line %d.", lineXrefs.size(), line));
		}
	}

	@Override
	public void run() {
		// TODO: Use charset of launcher config here.
		Charset defaultCharset = Charset.defaultCharset();
		try {
			this.process = processBuilder.start();
			running = true;
			boolean addNew = true;
			InputStream inputStr = process.getInputStream();
			try (BufferedInputStream input = new BufferedInputStream(inputStr);
			    FileOutputStream output = new FileOutputStream(outputFile);) {
				callback.started(this, outputFile, defaultCharset);
				while (process.isAlive()) {
					int b = input.read();
					output.write(b);
					output.flush();
					if (addNew) {
						lines++;
						if (lineXrefs.isEmpty()) {
							lineXrefs.put(lines, 1);
						} else {
							int lastLine = lines - 1;
							Integer byteOffset = lineXrefs.get(lastLine);
							lineXrefs.put(lines, byteOffset + 1);
						}
						callback.output(lines);
					} else {
						Integer byteOffset = lineXrefs.get(lines);
						lineXrefs.put(lines, byteOffset + 1);
						callback.lastLineChanged(lines);
					}
					if (b == '\n') {
						addNew = true;
					} else {
						addNew = false;
					}
				}
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
		return lines;
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

}
