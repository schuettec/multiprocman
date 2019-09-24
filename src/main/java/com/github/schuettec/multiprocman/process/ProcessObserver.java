package com.github.schuettec.multiprocman.process;

public interface ProcessObserver {

	void startProcess();

	void stopProcess();

	void waitFor();

	long getLines();

	boolean isRunning();

}