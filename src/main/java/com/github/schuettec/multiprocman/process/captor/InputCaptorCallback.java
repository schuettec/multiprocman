package com.github.schuettec.multiprocman.process.captor;

public interface InputCaptorCallback {

	/**
	 * Called by the {@link InputCaptor}.
	 *
	 * @return If <code>true</code> is returned the {@link InputCaptor} will continue I/O processing, otherwise the input
	 *         captor terminates.
	 */
	boolean shouldRun();

	/**
	 * Called by the {@link InputCaptor} to signal, that a new line was read from the input. The string may include ASCII
	 * control chars that have to be interpreted.
	 *
	 * @param lines The current number of lines available.
	 * @param string The line to append.
	 */
	void newLine(int lines, String string);

	/**
	 * Called by the {@link InputCaptor} to signal, that the content should be appended to the last line. The implementor
	 * should check if the last line is currently captured and the change is of interest. The string may include ASCII
	 * control chars that have to be interpreted.
	 *
	 * @param string The string to append at last line.
	 */
	void append(String string);

}
