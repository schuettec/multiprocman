package livefilereader;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

public class ProcessObserverImpl extends Thread implements ProcessObserver {

	private ProcessBuilder processBuilder;
	private File outputFile;

	private long lines = 0;
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

	@Override
	public void run() {
		try {
			this.process = processBuilder.start();
			running = true;
			InputStream inputStream = process.getInputStream();
			FileOutputStream fos = new FileOutputStream(outputFile);
			try (Scanner input = new Scanner(inputStream);
			    PrintStream output = new PrintStream(fos, false, Charset.defaultCharset()
			        .displayName())) {
				while (process.isAlive() && input.hasNextLine()) {
					output.println(input.nextLine());
					output.flush();
					lines++;
					callback.output(lines);
				}
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
			this.process = null;
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

	@Override
	public List<String> getLines(int lastLineInclusive, int maxLines) {

		return null;
	}

}
