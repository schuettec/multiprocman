package com.github.schuettec.multiprocman;

import static java.lang.Math.min;
import static java.util.Objects.nonNull;

import java.io.ByteArrayOutputStream;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.event.EventListenerSupport;

import com.github.schuettec.multiprocman.common.EventJoin;
import com.github.schuettec.multiprocman.console.AnsiColorTextPane;
import com.github.schuettec.multiprocman.console.LimitedStyledDocument;
import com.github.schuettec.multiprocman.console.ScrollableAnsiColorTextPaneContainer;
import com.github.schuettec.multiprocman.consolepreview.ConsolePreview;
import com.github.schuettec.multiprocman.themes.ThemeUtil;
import com.github.schuettec.multiprocman.themes.console.AnsiColorTextPaneTheme;

public class ProcessController {

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

		this.textPane = new AnsiColorTextPane(new LimitedStyledDocument(processDescriptor.getMaxLineNumbers()));
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
			String command = processDescriptor.getCommandForExecution();
			this.process = executeCommand(command);
			startProcessObserver();
		} catch (IOException e) {
			ExceptionDialog.showException(textPane, e, "Error while starting the application.");
			return false;
		}
		return true;
	}

	private Process executeCommand(String command) throws IOException {
		File workingDir = null;
		if (processDescriptor.hasExecutionDirectory()) {
			workingDir = new File(processDescriptor.getExecutionDirectoryForExecution());
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
		return Runtime.getRuntime()
		    .exec(command, env, workingDir);
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

					ByteArrayOutputStream inputBuffer = new ByteArrayOutputStream();
					AtomicInteger inputBufferSize = new AtomicInteger(0);
					ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
					AtomicInteger errorBufferSize = new AtomicInteger(0);

					EventJoin inputJoin = new EventJoin(new EventJoin.Callback() {

						@Override
						public void eventCallback() {
							if (inputBufferSize.get() > 0) {
								appendInEDT(new String(inputBuffer.toByteArray()));
								inputBuffer.reset();
								inputBufferSize.set(0);
							}
						}
					}, 250, TimeUnit.MILLISECONDS);

					EventJoin errorJoin = new EventJoin(new EventJoin.Callback() {

						@Override
						public void eventCallback() {
							if (errorBufferSize.get() > 0) {
								appendInEDT(new String(errorBuffer.toByteArray()));
								errorBuffer.reset();
								errorBufferSize.set(0);
							}
						}
					}, 250, TimeUnit.MILLISECONDS);

					Charset charset = processDescriptor.getCharset();
					try {
						do {
							buffer(inputStream, inputBufferSize, inputBuffer, charset);
							inputJoin.noticeEvent();

							buffer(errorStream, inputBufferSize, errorBuffer, charset);
							errorJoin.noticeEvent();

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

			private void buffer(final InputStream inputStream, AtomicInteger inputBufferSize,
			    ByteArrayOutputStream inputBuffer, Charset charset) throws IOException {
				Chunk inputChunk = readNext(charset, inputStream);
				if (nonNull(inputChunk)) {
					inputBuffer.write(inputChunk.getData(), 0, inputChunk.getAmount());
					inputBufferSize.addAndGet(inputChunk.getAmount());
				}
			}

			private Chunk readNext(Charset charset, InputStream stream) throws IOException {
				int available = 0;

				if ((available = stream.available()) > 0) {
					available = min(MAX_READ_AMOUNT_SIZE, available);
					byte[] data = new byte[available];
					int amount = stream.read(data);
					Chunk chunk = new Chunk(data, amount);
					return chunk;
				}
				return null;
			}

			private void appendInEDT(String nextLine) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
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
				int incrementAndGet = count.incrementAndGet();
				System.out.println(incrementAndGet + " for " + nextLine.getBytes().length + "bytes");
			}
		});
		processObserver.start();
	}

	public static final AtomicInteger count = new AtomicInteger(0);

	private void updateState(State state) {
		this.state = state;
		processListener.fire()
		    .processUpdate(ProcessController.this);
	}

	public void stop(boolean waitFor) {
		updateState(State.STOPPING);
		_stopProcess(false);
		waitForOnDemand(waitFor);
	}

	private void _stopProcess(boolean force) {
		if (force) {
			this.process.destroyForcibly();
		} else {
			if (processDescriptor.isUseTerminationCommand()) {
				executeTermination();
			} else {
				this.process.destroy();
			}
		}
	}

	private void executeTermination() {
		Thread termination = new Thread(new Runnable() {
			@Override
			public void run() {
				String terminationCommand = processDescriptor.getTerminationCommandForExecution();
				try {
					Process process = executeCommand(terminationCommand);
					boolean terminated = process.waitFor(8000, TimeUnit.MILLISECONDS);
					if (!terminated) {
						stopForce(false);
						JOptionPane.showMessageDialog(textPane,
						    "The application %s was stopped with the termination command but the command did not respond. The applicationwas killed to force termination.",
						    "Termination command", JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {
					stopForce(false);
					JOptionPane.showMessageDialog(textPane,
					    "The application %s was stopped with the termination command but the command threw an error. A kill signal is used to force termination.",
					    "Termination command", JOptionPane.ERROR_MESSAGE);
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
