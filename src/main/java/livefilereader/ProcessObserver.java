package livefilereader;

import java.util.List;

public interface ProcessObserver {

	void startProcess();

	void stopProcess();

	long getLines();

	/**
	 * Returns the specified number of last lines inclusive the last line specified by the last line number.
	 *
	 * @param lastLineInclusive The line number of the last line to be included.
	 * @param maxLines The number of max lines to read backwards.
	 * @return Returns the list of lines.
	 */
	List<String> getLines(int lastLineInclusive, int maxLines);

	boolean isRunning();

}