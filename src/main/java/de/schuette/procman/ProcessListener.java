package de.schuette.procman;

public interface ProcessListener {

	public void processStarted();

	public void processStopped(int exitValue);

	public void processAbandoned();

	public void processUpdate();

}
