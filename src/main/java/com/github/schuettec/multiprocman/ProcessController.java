package com.github.schuettec.multiprocman;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.event.EventListenerSupport;

import com.github.schuettec.multiprocman.console.AnsiColorTextPane;
import com.github.schuettec.multiprocman.console.ScrollableAnsiColorTextPaneContainer;
import com.github.schuettec.multiprocman.consolepreview.ConsolePreview;
import com.github.schuettec.multiprocman.themes.ThemeUtil;
import com.github.schuettec.multiprocman.themes.console.AnsiColorTextPaneTheme;

public class ProcessController {

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

	private ScrollableAnsiColorTextPaneContainer consoleScroller;
	private AnsiColorTextPane textPane;
	private ConsolePreview consolePreview;
	private ProcessDescriptor processDescriptor;
	private CounterExpressions counterExpressions;
	private Thread processObserver;

	private Process process;
	private State state;

	public ProcessController(ProcessDescriptor processDescriptor) {
		this.processDescriptor = processDescriptor;
		this.textPane = new AnsiColorTextPane();
		ThemeUtil.theme(textPane, AnsiColorTextPaneTheme.class);
		this.consoleScroller = new ScrollableAnsiColorTextPaneContainer(textPane);
		this.consolePreview = new ConsolePreview(this);
		this.textPane.addAppendListener(consolePreview);
		processListener.addListener(consolePreview);
		this.counterExpressions = new CounterExpressions(this);

		this.textPane.addAppendListener(counterExpressions);
		this.state = State.NOT_STARTED;
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
			File workingDir = null;
			if (processDescriptor.hasExecutionDirectory()) {
				workingDir = processDescriptor.getExecutionDirectory();
			}
			String[] env = null;
			if (processDescriptor.hasEnvironmentVariables()) {
				Map<String, String> environment = processDescriptor.getEnvironment();
				env = new String[environment.size()];
				Iterator<Entry<String, String>> it = environment.entrySet()
				    .iterator();
				int i = 0;
				while (it.hasNext()) {
					Entry<String, String> next = it.next();
					env[i] = next.getKey() + "=" + next.getValue();
				}
			}
			this.process = Runtime.getRuntime()
			    .exec(processDescriptor.getCommand(), env, workingDir);
			startProcessObserver();
		} catch (IOException e) {
			ExceptionDialog.showException(e, "Error while starting the application.");
			return false;
		}
		return true;
	}

	private void startProcessObserver() {
		this.processObserver = new Thread(new Runnable() {

			@Override
			public void run() {
				if (process.isAlive()) {
					updateState(State.RUNNING);
					controllers.add(ProcessController.this);

					// Read outputs
					final InputStream inputStream = process.getInputStream();
					final InputStream errorStream = process.getErrorStream();

					Charset charset = processDescriptor.getCharset();
					try {
						do {
							readNext(charset, inputStream);
							readNext(charset, errorStream);
						} while (process.isAlive());
					} catch (Exception e) {
						updateState(State.ABANDONED);
					}
					try {
						int exitValue = process.waitFor();
						if (exitValue == 0) {
							updateState(State.STOPPED_OK);
						} else {
							updateState(State.STOPPED_ALERT);
						}
						controllers.remove(ProcessController.this);
					} catch (InterruptedException e) {
					}
				}
			}

			private void readNext(Charset charset, InputStream stream) throws IOException {
				int available = 0;
				if ((available = stream.available()) > 0) {
					byte[] data = new byte[available];
					int amount = stream.read(data);
					appendInEDT(new String(data, 0, amount, charset));
				}
			}

			private void appendInEDT(String nextLine) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							consoleScroller.appendANSI(nextLine, processDescriptor.isSupportAsciiCodes());
							processListener.fire()
							    .processUpdate(ProcessController.this);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		processObserver.start();
	}

	private void updateState(State state) {
		this.state = state;
		processListener.fire()
		    .processUpdate(ProcessController.this);
	}

	public void stop(boolean waitFor) {
		updateState(State.STOPPING);
		this.process.destroy();
		waitForOnDemand(waitFor);
	}

	public void stopForce(boolean waitFor) {
		updateState(State.STOPPING);
		this.process.destroyForcibly();
		waitForOnDemand(waitFor);
	}

	private void waitForOnDemand(boolean waitFor) {
		if (waitFor) {
			try {
				this.process.waitFor();
			} catch (InterruptedException e) {
				// Nothing to do.
			}
		}
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
				controller.stopForce(true);
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

}
