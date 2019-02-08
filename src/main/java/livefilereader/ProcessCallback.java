package livefilereader;

import java.io.File;

public interface ProcessCallback {

	/**
	 * Called by the {@link ProcessObserver} to signal that the output file could not be written.
	 *
	 * @param outputFile The output file that cannot be written to.
	 * @param cause The exception as cause.
	 */
	void cannotWriteOutput(File outputFile, Exception cause);

	/**
	 * Called by the {@link ProcessObserver} to signal that the process cannot be started.
	 *
	 * @param e The exception as cause.
	 */
	void cannotStartProcess(Exception e);

	/**
	 * Called by the {@link ProcessObserver} to signal that new output was written by the process.
	 * 
	 * @param lines Number of lines written.
	 */
	void output(long lines);

}
