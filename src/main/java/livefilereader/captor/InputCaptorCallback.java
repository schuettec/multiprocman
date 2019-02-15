package livefilereader.captor;

public interface InputCaptorCallback {

	/**
	 * Called by the {@link InputCaptor}.
	 *
	 * @return If <code>true</code> is returned the {@link InputCaptor} will continue I/O processing, otherwise the input
	 *         captor terminates.
	 */
	boolean shouldRun();

	/**
	 * Called by the {@link InputCaptor} to signal, that a new line was read from the input.
	 *
	 * @param lines The current number of lines available.
	 */
	void newLine(int lines);

	/**
	 * Called by the {@link InputCaptor} to signal, that the specified amount of backspace control character is added. The
	 * implementor should check if the last line is currently captured and the change is of interest.
	 *
	 * @param line The line number where the ASCII code occurred.
	 * @param count The number of backspaces counted.
	 */
	void backspace(int line, int count);

	/**
	 * Called by the {@link InputCaptor} to signal, that the content should be appended to the last line. The implementor
	 * should check if the last line is currently captured and the change is of interest.
	 *
	 * @param string The string to append at last line.
	 */
	void append(String string);

}
