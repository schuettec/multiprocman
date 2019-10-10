package com.github.schuettec.multiprocman.process;

public interface ProcessObserver {

	void startProcess();

	void stopProcess(boolean waitFor);

	void waitFor();

	long getLines();

	boolean isRunning();

}