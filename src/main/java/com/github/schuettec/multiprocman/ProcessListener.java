package com.github.schuettec.multiprocman;

public interface ProcessListener {

	public void processUpdate(ProcessController controller);

	/**
	 * Called if a process output was printed.
	 */
	public void processOutput(ProcessController processController);

}
