package de.schuette.procman;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.event.EventListenerSupport;

import de.schuette.procman.console.AnsiColorTextPane;
import de.schuette.procman.console.ScrollableAnsiColorTextPaneContainer;
import de.schuette.procman.consolepreview.ConsolePreview;
import de.schuette.procman.themes.ThemeUtil;
import de.schuette.procman.themes.console.AnsiColorTextPaneTheme;

public class ProcessController {

	private static Queue<ProcessController> controllers = new ConcurrentLinkedQueue<>();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				shutdown();
			}
		}));
	}

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
		this.consolePreview = new ConsolePreview(processDescriptor);
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
		try {
			ProcessBuilder builder = new ProcessBuilder(processDescriptor.getCommand())
					.directory(processDescriptor.getExecutionDirectory()).redirectErrorStream(true);

			if (processDescriptor.hasEnvironmentVariables()) {
				Map<String, String> environment = builder.environment();
				environment.putAll(processDescriptor.getEnvironment());
			}

			this.process = builder.start();
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
					controllers.add(ProcessController.this);

					// Read outputs
					final InputStream inputStream = process.getInputStream();
					final InputStream errorStream = process.getErrorStream();

					try (Scanner input1 = new Scanner(inputStream, processDescriptor.getCharset().name());
							Scanner input2 = new Scanner(errorStream, processDescriptor.getCharset().name())) {
						while (process.isAlive()) {
							if (input1.hasNextLine()) {
								String nextLine = input1.nextLine();
								textPane.appendANSI(nextLine + "\n");
								processListener.fire().processUpdate();
							}
							if (input2.hasNextLine()) {
								String nextLine = input2.nextLine();
								textPane.appendANSI(nextLine + "\n");
								processListener.fire().processUpdate();
							}
						}
					} catch (IllegalStateException e) {
						processListener.fire().processAbandoned();
					}
					try {
						int exitValue = process.waitFor();
						processListener.fire().processStopped(exitValue);
						controllers.remove(ProcessController.this);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		processObserver.start();
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

	/**
	 * Shuts down all available {@link ProcessController}s.
	 */
	public static void shutdown() {
		if (!controllers.isEmpty()) {
			List<ProcessController> toShutdown = new LinkedList<>(controllers);
			for (ProcessController controller : toShutdown) {
				controller.stopForce();
			}
		}
	}

	/**
	 * @return Returns <code>true</code> if there are active processes.
	 */
	public static boolean hasActiveProcesses() {
		return !controllers.isEmpty();
	}
}
