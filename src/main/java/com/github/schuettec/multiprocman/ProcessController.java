package com.github.schuettec.multiprocman;

import java.awt.Component;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JScrollBar;

import org.apache.commons.lang3.event.EventListenerSupport;

import com.github.schuettec.multiprocman.console.AnsiColorTextPane;
import com.github.schuettec.multiprocman.consolepreview.ConsolePreview;
import com.github.schuettec.multiprocman.process.ProcessObserverImpl;
import com.github.schuettec.multiprocman.process.ReaderController;
import com.github.schuettec.multiprocman.themes.ThemeUtil;
import com.github.schuettec.multiprocman.themes.console.AnsiColorTextPaneTheme;

public class ProcessController {

	private static final int WAIT_FOR_STREAM = 75;

	private static final int MAX_READ_AMOUNT_SIZE = 512;

	private static Queue<ProcessController> controllers = new ConcurrentLinkedQueue<>();

	static {
		Runtime.getRuntime()
		    .addShutdownHook(new Thread(new Runnable() {
			    @Override
			    public void run() {
				    shutdown();
			    }
		    }));
	}

	public enum State {
		NOT_STARTED,
		RUNNING,
		STOPPING,
		STOPPED_OK,
		STOPPED_ALERT,
		ABANDONED;
	}

	private EventListenerSupport<ProcessListener> processListener = new EventListenerSupport<>(ProcessListener.class);

	private AnsiColorTextPane textPane;
	private ConsolePreview consolePreview;
	private ProcessDescriptor processDescriptor;
	private CounterExpressions counterExpressions;

	private State state;

	private Statistics statistics;

	private ReaderController controller;

	private ProcessObserverImpl processObserver;

	public ProcessController(ProcessDescriptor processDescriptor) {
		this.controller = new ReaderController();
		this.processDescriptor = processDescriptor;
		this.statistics = new Statistics();
		this.textPane = new AnsiColorTextPane();
		ThemeUtil.theme(textPane, AnsiColorTextPaneTheme.class);
		this.consolePreview = new ConsolePreview(this);
		this.textPane.addAppendListener(consolePreview);
		processListener.addListener(consolePreview);
		this.counterExpressions = new CounterExpressions(this);
		this.textPane.addAppendListener(counterExpressions);
		this.state = State.NOT_STARTED;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public ProcessDescriptor getProcessDescriptor() {
		return processDescriptor;
	}

	public void addProcessListener(ProcessListener l) {
		processListener.addListener(l, false);
	}

	public void removeProcessListener(ProcessListener l) {
		processListener.removeListener(l);
	}

	public boolean startWithVariables(Component parent) {
		boolean cancelled = processDescriptor.promptVariables(parent);
		if (cancelled) {
			return cancelled;
		}
		return !start();
	}

	public boolean start() {
		try {
			statistics.clear();
			startProcessObserver();
		} catch (Exception e) {
			ExceptionDialog.showException(textPane, e, "Error while starting the application.");
			return false;
		}
		return true;
	}

	private ProcessBuilder setupProcessBuilderCommand(boolean forTermination, ProcessDescriptor processDescriptor) {
		String command = null;
		if (forTermination) {
			command = processDescriptor.getTerminationCommandForExecution();
		} else {
			command = processDescriptor.getCommandForExecution();
		}

		File workingDir = null;
		if (processDescriptor.hasExecutionDirectory()) {
			workingDir = new File(processDescriptor.getExecutionDirectoryForExecution());
		}

		// Tokenize command string
		StringTokenizer st = new StringTokenizer(command);
		List<String> cmdList = new ArrayList<>(st.countTokens());
		for (int i = 0; st.hasMoreTokens(); i++) {
			cmdList.add(st.nextToken());
		}

		// Setup process builder
		ProcessBuilder builder = new ProcessBuilder(cmdList);
		builder.redirectErrorStream(true);
		builder.directory(workingDir);
		if (processDescriptor.hasEnvironmentVariables()) {
			Map<String, String> environment = processDescriptor.getEnvironment();
			builder.environment()
			    .putAll(environment);
		}

		return builder;
	}

	private void startProcessObserver() {
		updateState(State.RUNNING);
		controllers.add(ProcessController.this);
		Charset charset = processDescriptor.getCharset();
		try {
			// TODO: Use Reader Controller here

			// TODO: Set auto-scroll to bottom to true initially
			ProcessBuilder builder = setupProcessBuilderCommand(false, processDescriptor);
			this.processObserver = new ProcessObserverImpl(builder, new File("output.txt"), controller);
			processObserver.startProcess();
		} catch (Exception e) {
			ExceptionDialog.showException(textPane, e, "Exception occurred while starting the process.");
			updateState(State.NOT_STARTED);
		}

		// TODO: Where should we do this?:
		// try {
		// boolean exited = process.waitFor(10l, TimeUnit.SECONDS);
		// if (exited) {
		// int exitValue = process.exitValue();
		// if (exitValue == 0) {
		// updateState(State.STOPPED_OK);
		// } else {
		// updateState(State.STOPPED_ALERT);
		// }
		// } else {
		// updateState(State.ABANDONED);
		// }
		// controllers.remove(ProcessController.this);
		// } catch (InterruptedException e) {
		// }
	}

	private void updateState(State state) {
		this.state = state;
		processListener.fire()
		    .processUpdate(ProcessController.this);
	}

	public void stop(boolean waitFor) {
		if (state == State.RUNNING || state == State.ABANDONED) {
			updateState(State.STOPPING);
			_stopProcess(false);
			waitForOnDemand(waitFor);
		}
	}

	private void _stopProcess(boolean force) {
		if (processDescriptor.isUseTerminationCommand()) {
			executeTermination();
		} else {
			if (force) {
				this.processObserver.stopProcessForcibly();
			} else {
				this.processObserver.stopProcess();
			}
		}
	}

	private void executeTermination() {
		Thread termination = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ProcessBuilder termProcess = setupProcessBuilderCommand(true, processDescriptor);
					Process process = termProcess.start();
					boolean terminated = process.waitFor(8000, TimeUnit.MILLISECONDS);
					if (!terminated) {
						stopForce(false);
						JOptionPane.showMessageDialog(textPane, String.format(
						    "The application %s was stopped with the termination command but the command did not respond. The applicationwas killed to force termination.",
						    processDescriptor.getTitle()), "Termination command", JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {
					stopForce(false);
					JOptionPane.showMessageDialog(textPane, String.format(
					    "The application %s was stopped with the termination command but the command threw an error. A kill signal is used to force termination.",
					    processDescriptor.getTitle()), "Termination command", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		termination.start();
	}

	public void stopForce(boolean waitFor) {
		updateState(State.STOPPING);
		_stopProcess(true);
		waitForOnDemand(waitFor);
	}

	private void waitForOnDemand(boolean waitFor) {
		if (waitFor) {
			this.processObserver.waitFor();
		}
	}

	public AnsiColorTextPane getTextPane() {
		return controller.getTextView();
	}

	public ConsolePreview getConsolePreview() {
		return consolePreview;
	}

	public void updateListeners() {
		processListener.fire()
		    .processUpdate(ProcessController.this);
	}

	/**
	 * Shuts down all available {@link ProcessController}s.
	 */
	public static void shutdown() {
		if (!controllers.isEmpty()) {
			List<ProcessController> toShutdown = new LinkedList<>(controllers);
			for (ProcessController controller : toShutdown) {
				controller.stop(true);
			}
		}
	}

	/**
	 * @return Returns <code>true</code> if there are active processes.
	 */
	public static boolean hasActiveProcesses() {
		return !controllers.isEmpty();
	}

	public State getState() {
		return state;
	}

	public void clearConsole() {
		getTextPane().setText("");
		getConsolePreview().clear();
		counterExpressions.clear();
		updateListeners();
	}

	public CounterExpressions getCounterExpressions() {
		return counterExpressions;
	}

	public JScrollBar getTextViewScroller() {
		return controller.getLineScroller();
	}

}
