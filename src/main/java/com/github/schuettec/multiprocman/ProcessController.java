package com.github.schuettec.multiprocman;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.event.EventListenerSupport;

import com.github.schuettec.multiprocman.console.AnsiColorTextPane;
import com.github.schuettec.multiprocman.consolepreview.ConsolePreview;
import com.github.schuettec.multiprocman.process.ProcessCallback;
import com.github.schuettec.multiprocman.process.ProcessObserverImpl;
import com.github.schuettec.multiprocman.process.ProcessOutputInfo;
import com.github.schuettec.multiprocman.process.ReaderController;
import com.github.schuettec.multiprocman.process.ViewFrameListener;

public class ProcessController implements ProcessCallback, ViewFrameListener {
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

	private ConsolePreview consolePreview;
	private ProcessDescriptor processDescriptor;
	private CounterExpressions counterExpressions;

	private State state;

	private Statistics statistics;

	private ReaderController controller;

	private ProcessObserverImpl processObserver;

	private File outputFile;

	public ProcessController(ProcessDescriptor processDescriptor) {
		this.controller = new ReaderController();

		this.processDescriptor = processDescriptor;
		this.statistics = new Statistics();
		this.consolePreview = new ConsolePreview(this);
		processListener.addListener(consolePreview);
		this.counterExpressions = new CounterExpressions(this);
		this.state = State.NOT_STARTED;

		this.getTextPane()
		    .addAppendListener(counterExpressions);
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

	public boolean start() {
		try {
			statistics.clear();
			startProcessObserver();
		} catch (Exception e) {
			ExceptionDialog.showException(getTextPane(), e, "Error while starting the application.");
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
		try {
			controller.setJumpToLastLine(true);
			Charset charset = processDescriptor.getCharset();
			ProcessBuilder builder = setupProcessBuilderCommand(false, processDescriptor);
			this.outputFile = createOutputFile();
			this.processObserver = new ProcessObserverImpl(builder, outputFile, charset);
			this.processObserver.addListener(consolePreview);
			this.processObserver.addListener(controller);
			this.processObserver.addListener(this);
			this.controller.addListener(processObserver);
			this.controller.addListener(this);
			processObserver.startProcess();
		} catch (Exception e) {
			ExceptionDialog.showException(getTextPane(), e, "Exception occurred while starting the process.");
			updateState(State.NOT_STARTED);
		}
	}

	private File createOutputFile() throws IOException {
		File file = new File("multiprocman_" + UUID.randomUUID()
		    .toString() + ".txt");
		System.out.println("Output capturing file created: " + file.getAbsolutePath());
		return file;
	}

	public void deleteOutputFile() {
		if (state == State.RUNNING) {
			int answer = JOptionPane.showConfirmDialog(controller.getTextView(),
			    "Cannot delete the output capturing file because process is running. Do you want to stop the process and delete?",
			    "Delete output file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.YES_OPTION) {
				stop(false);
			} else {
				return;
			}
		}
		try {
			boolean deleted = closeAndDeleteOutputFileSilent();
			if (!deleted) {
				if (state == State.STOPPED_OK || state == State.STOPPED_ALERT) {
					JOptionPane.showMessageDialog(controller.getTextView(),
					    "Cannot delete output file: " + outputFile.getAbsolutePath(), "Delete output file",
					    JOptionPane.WARNING_MESSAGE);
				}
			}
		} catch (Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					ExceptionDialog.showException(controller.getTextView(), e,
					    "Cannot delete output capturing file: " + outputFile.getAbsolutePath());
				}
			});
		}
	}

	private boolean closeAndDeleteOutputFileSilent() {
		controller.close();
		boolean deleted = this.outputFile.delete();
		return deleted;
	}

	private void updateState(State state) {
		this.state = state;
		processListener.fire()
		    .processUpdate(ProcessController.this);
	}

	public void stop(boolean waitFor) {
		if (state == State.RUNNING || state == State.ABANDONED) {
			updateState(State.STOPPING);
			_stopProcess();
			waitForOnDemand(waitFor);
		}
	}

	private void _stopProcess() {
		if (processDescriptor.isUseTerminationCommand()) {
			executeTermination();
		} else {
			this.processObserver.stopProcess();
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
						stop(false);
						JOptionPane.showMessageDialog(getTextPane(), String.format(
						    "The application %s was stopped with the termination command but the command did not respond. The applicationwas killed to force termination.",
						    processDescriptor.getTitle()), "Termination command", JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {
					stop(false);
					JOptionPane.showMessageDialog(getTextPane(), String.format(
					    "The application %s was stopped with the termination command but the command threw an error. A kill signal is used to force termination.",
					    processDescriptor.getTitle()), "Termination command", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		termination.start();
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
				controller.closeAndDeleteOutputFileSilent();
			}
		}
	}

	@Override
	public void started(ProcessOutputInfo processOutputInfo, File outputFile, Charset charset) {
		updateState(State.RUNNING);
	}

	@Override
	public void exited(int exitValue) {
		if (exitValue == 0) {
			updateState(State.STOPPED_OK);
		} else {
			updateState(State.STOPPED_ALERT);
		}
	}

	@Override
	public void abandoned() {
		updateState(State.ABANDONED);
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

	@Override
	public void autoScrollStateChanged(boolean autoScrollNewValue) {
		updateListeners();
	}

	public CounterExpressions getCounterExpressions() {
		return counterExpressions;
	}

	public JScrollBar getTextViewScroller() {
		return controller.getLineScroller();
	}

	public void setAutoScrollToBottom(boolean autoScroll) {
		controller.setJumpToLastLine(autoScroll);
	}

	public void scrollToBottom() {
		controller.jumpToLastLine();
	}

	public boolean setAutoScrollToBottom() {
		return controller.isJumpToLastLine();
	}

	public String getLastLines(int linesCount) {
		return controller.getLastLines(linesCount);
	}

	@Override
	public void cannotStartProcess(Exception e) {
		updateState(State.NOT_STARTED);
	}

	@Override
	public void cannotWriteOutput(File outputFile, Exception cause) {
		updateState(State.NOT_STARTED);
	}

	@Override
	public void output(int lines, String line) {
		processListener.fire()
		    .processOutput(this);
		statistics.setOverallOutputAmount(controller.getCaptureFileSize());
	}

	@Override
	public void append(String string) {
		processListener.fire()
		    .processOutput(this);
		statistics.setOverallOutputAmount(controller.getCaptureFileSize());

	}

	@Override
	public void jumpToLastLine(int lines) {
		processListener.fire()
		    .processOutput(this);
		statistics.setOverallOutputAmount(controller.getCaptureFileSize());
	}

}
