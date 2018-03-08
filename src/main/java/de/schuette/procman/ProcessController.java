package de.schuette.procman;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.event.EventListenerSupport;

import de.schuette.procman.console.AnsiColorTextPane;
import de.schuette.procman.console.ScrollableAnsiColorTextPaneContainer;
import de.schuette.procman.consolepreview.ConsolePreview;
import de.schuette.procman.themes.ThemeUtil;
import de.schuette.procman.themes.console.AnsiColorTextPaneTheme;

public class ProcessController {

	private EventListenerSupport<ProcessListener> processListener = new EventListenerSupport<>(ProcessListener.class);

	private ScrollableAnsiColorTextPaneContainer consoleScroller;
	private AnsiColorTextPane textPane;
	private ConsolePreview consolePreview;
	private ProcessDescriptor processDescriptor;

	private Thread processObserver;

	private Process process;

	public ProcessController(ProcessDescriptor processDescriptor) {
		this.processDescriptor = processDescriptor;
		this.textPane = new AnsiColorTextPane();
		ThemeUtil.theme(textPane, AnsiColorTextPaneTheme.class);
		this.consoleScroller = new ScrollableAnsiColorTextPaneContainer(textPane);
		this.consolePreview = new ConsolePreview();
		this.textPane.addAppendListener(consolePreview);
		processListener.addListener(consolePreview);
	}

	public void addProcessListener(ProcessListener l) {
		processListener.addListener(l, false);
	}

	public void removeProcessListener(ProcessListener l) {
		processListener.removeListener(l);
	}

	public ProcessController start() {
		String command = "";
		File directory = null;
		Map<String, String> environment;
		try {
			this.process = new ProcessBuilder("ping", "google.de", "-n", "10000").directory(directory).start();
			startProcessObserver();
		} catch (IOException e) {
			ExceptionDialog.showException(e, "Error while starting the application.");
		}
		return this;
	}

	private void startProcessObserver() {
		this.processObserver = new Thread(new Runnable() {

			@Override
			public void run() {
				if (process.isAlive()) {
					processListener.fire().processStarted();
					try {
						int exitValue = process.waitFor();
						processListener.fire().processStopped(exitValue);
					} catch (InterruptedException e) {
					}
				}
			}
		});
	}

	public void stop() {
		this.process.destroy();
	}

	public void stopForce() {
		this.process.destroyForcibly();
	}

	public ScrollableAnsiColorTextPaneContainer getConsoleScroller() {
		return consoleScroller;
	}

	public AnsiColorTextPane getTextPane() {
		return textPane;
	}

	public ConsolePreview getConsolePreview() {
		return consolePreview;
	}

}
