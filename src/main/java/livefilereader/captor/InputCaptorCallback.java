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
	 * Called by the {@link InputCaptor} to signal, that an ASCII control character was detected at the end of the
	 * specified line. The implementor
	 * should check if the last line is currently captured and the change is of interest.
	 *
	 * @param lines The line number where the ASCII code ocurred.
	 * @param ascii The ASCII control character.
	 */
	void asciiCode(int lines, int ascii);

	/**
	 * Called by the {@link InputCaptor} to signal, that the content should be appended to the last line. The implementor
	 * should check if the last line is currently captured and the change is of interest.
	 *
	 * @param string The string to append at last line.
	 */
	void append(String string);

}
