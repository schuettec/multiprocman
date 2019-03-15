package com.github.schuettec.multiprocman.process;

import java.io.File;
import java.nio.charset.Charset;

import com.github.schuettec.multiprocman.process.captor.InputCaptor;

public interface ProcessCallback {

	/**
	 * Called by the {@link ProcessObserver} to signal that the output file could not be written.
	 *
	 * <p>
	 * <b>Implementors should make sure that any UI access as a reaction to this call is performed in the correct
	 * thread.</b>
	 * </p>
	 *
	 * @param outputFile The output file that cannot be written to.
	 * @param cause The exception as cause.
	 */
	default void cannotWriteOutput(File outputFile, Exception cause) {
	}

	/**
	 * Called by the {@link ProcessObserver} to signal that the process cannot be started.
	 *
	 * <p>
	 * <b>Implementors should make sure that any UI access as a reaction to this call is performed in the correct
	 * thread.</b>
	 * </p>
	 *
	 * @param e The exception as cause.
	 */
	default void cannotStartProcess(Exception e) {
	}

	/**
	 * Called by the {@link ProcessObserver} to signal that the application output can be captured through the specified
	 * file.
	 * <p>
	 * <b>Implementors should make sure that any UI access as a reaction to this call is performed in the correct
	 * thread.</b>
	 * </p>
	 *
	 * @param processOutputInfo Object that translated line numbers to byte offsets.
	 * @param outputFile The file to capture the application output.
	 * @param charset Chaset to be used for reading application output.
	 */
	default void started(ProcessOutputInfo processOutputInfo, File outputFile, Charset charset) {
	}

	/**
	 * Called by the {@link ProcessObserver} to signal that new output was written by the process.
	 * <p>
	 * <b>Implementors should make sure that any UI access as a reaction to this call is performed in the correct
	 * thread.</b>
	 * </p>
	 *
	 * @param lines Number of lines written.
	 * @param line The line to append.
	 */
	default void output(int lines, String line) {
	}

	/**
	 * Called by the {@link ProcessObserver} to signal that the process terminated.
	 *
	 * <p>
	 * <b>Implementors should make sure that any UI access as a reaction to this call is performed in the correct
	 * thread.</b>
	 * </p>
	 *
	 * @param exitValue The return code of the process.
	 */
	default void exited(int exitValue) {
	}

	/**
	 * Called by the {@link InputCaptor} to signal, that the content should be appended to the last line. The implementor
	 * should check if the last line is currently captured and the change is of interest. The string may include ASCII
	 * control chars that have to be interpreted.
	 *
	 * @param string The string to append at last line.
	 */
	default void append(String string) {

	}

	/**
	 * Called by the {@link InputCaptor} to signal, that the process status cannot be determined.
	 */
	default void abandoned() {

	}

}
