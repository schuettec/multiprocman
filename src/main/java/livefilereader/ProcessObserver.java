package livefilereader;

public interface ProcessObserver {

	void startProcess();

	void stopProcess();

	long getLines();

	boolean isRunning();

}