package com.github.schuettec.multiprocman.process;

public interface ProcessObserver {

	void startProcess();

	void stopProcess();

	void stopProcessForcibly();

	void waitFor();

	long getLines();

	boolean isRunning();

}