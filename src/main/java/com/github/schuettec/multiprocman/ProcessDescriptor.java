package com.github.schuettec.multiprocman;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import com.github.schuettec.multiprocman.git.DefaultCredentialsCallback;
import com.github.schuettec.multiprocman.git.GitException;
import com.github.schuettec.multiprocman.git.GitManager;
import com.github.schuettec.multiprocman.git.GitManagerImpl;
import com.github.schuettec.multiprocman.git.ProgressMonitorView;

public class ProcessDescriptor implements Serializable {

	private static final int MAX_LINES_DEFAULT = 200;

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private ImageIcon icon;
	private String title;
	private String command;
	private String executionDirectory;
	private Color color;
	private Charset charset;
	private Map<String, String> environment;
	private boolean supportAsciiCodes;
	private boolean variableSubstitution;
	private boolean terminationVariableSubstitution;
	private boolean useTerminationCommand;
	private String terminationCommand;

	private int maxLineNumbers;

	private List<Counter> counters;

	private boolean enableGitSupport;
	private boolean pullAfterCheckout;

	private transient GitManager gitManager;

	public ProcessDescriptor() {
		super();
		setCharset(Charset.defaultCharset());
		setIcon(Resources.getTerminal());
		setVariableSubstitution(true);
		setColor(Color.GREEN);
		setMaxLineNumbers(MAX_LINES_DEFAULT);
		setEnableGitSupport(false);
		setPullAfterCheckout(true);
	}

	private GitManager gitOperation() throws GitException {
		if (isEnableGitSupport()) {
			if (isNull(this.gitManager)) {
				if (!hasExecutionDirectory()) {
					this.gitManager = GitManagerImpl.noop();
				}
				this.gitManager = new GitManagerImpl(new DefaultCredentialsCallback(), getExecutionDirectoryForExecution());
			}
		} else {
			this.gitManager = GitManagerImpl.noop();
		}
		return this.gitManager;
	}

	public String getCurrentBranch() throws GitException {
		return gitOperation().currentBranch();
	}

	public List<String> getAllBranches() throws GitException {
		return gitOperation().branchList();
	}

	public boolean isPullAfterCheckout() {
		return pullAfterCheckout;
	}

	public boolean isSaveToCheckout() throws GitException {
		return !gitOperation().hasUncomittedChanges();
	}

	public void checkoutBranch(Component component, String branchName, ProgressMonitorView monitor) throws GitException {
		gitOperation().checkoutBranch(component, branchName, this.pullAfterCheckout, monitor);
	}

	public void setPullAfterCheckout(boolean pullBeforeCheckout) {
		this.pullAfterCheckout = pullBeforeCheckout;
	}

	public static String substituteCommand(String command) {
		Map<String, String> getenv = System.getenv();
		Iterator<Entry<String, String>> it = getenv.entrySet()
		    .iterator();
		String substitute = command;
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String substitution = getVariablePlaceholder(entry.getKey());
			substitute = substitute.replaceAll("(?i)" + Pattern.quote(substitution),
			    Matcher.quoteReplacement(entry.getValue()));
		}
		return substitute;
	}

	public boolean isEnableGitSupport() {
		return enableGitSupport;
	}

	public void setEnableGitSupport(boolean enableGitSupport) {
		this.enableGitSupport = enableGitSupport;
	}

	public int getMaxLineNumbers() {
		return maxLineNumbers;
	}

	public void setMaxLineNumbers(int maxLineNumbers) {
		this.maxLineNumbers = maxLineNumbers;
	}

	public boolean isVariableSubstitution() {
		return variableSubstitution;
	}

	public void setVariableSubstitution(boolean variableSubstitution) {
		this.variableSubstitution = variableSubstitution;
	}

	public boolean hasEnvironmentVariables() {
		return nonNull(environment);
	}

	public void setCounters(List<Counter> counters) {
		this.counters = counters;
	}

	public List<Counter> getCounters() {
		return counters;
	}

	public boolean hasCounters() {
		return nonNull(counters) && !counters.isEmpty();
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		requireNonNull(color, "Color may not be null!");
		this.color = color;
	}

	public void setIcon(BufferedImage icon) {
		requireNonNull(icon, "Icon may not be null!");
		setIcon(new ImageIcon(Resources.getTerminal()));
	}

	public void setIcon(ImageIcon icon) {
		requireNonNull(icon, "Icon may not be null!");
		this.icon = icon;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		requireNonNull(title, "Title may not be null!");
		this.title = title;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		requireNonNull(charset, "Charset may not be null!");
		this.charset = charset;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		requireNonNull(command, "Must not be null!");
		this.command = command;
		setTitleOnDemand(command);
	}

	private void setTitleOnDemand(String string) {
		if (Objects.isNull(title)) {
			title = string;
		}
	}

	public String getExecutionDirectory() {
		return executionDirectory;
	}

	public void setExecutionDirectory(String executionDirectory) {
		this.executionDirectory = executionDirectory;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}

	public boolean hasExecutionDirectory() {
		return nonNull(executionDirectory);
	}

	@Override
	public String toString() {
		return "ProcessDescriptor [title=" + title + ", command=" + command + ", executionDirectory=" + executionDirectory
		    + ", color=" + color + ", charset=" + charset + "]";
	}

	public boolean isSupportAsciiCodes() {
		return supportAsciiCodes;
	}

	public void setSupportAsciiCodes(boolean supportAsciiCodes) {
		this.supportAsciiCodes = supportAsciiCodes;
	}

	/**
	 * @return Returns the substituted command if environment variable substitution
	 *         was enabled. Otherwise the command is returned without any
	 *         modification.
	 */
	public String getCommandForExecution() {
		if (variableSubstitution) {
			return substituteCommand(command);
		} else {
			return command;
		}
	}

	/**
	 * @return Returns the substituted command if environment variable substitution
	 *         was enabled. Otherwise the command is returned without any
	 *         modification.
	 */
	public String getTerminationCommandForExecution() {
		if (terminationVariableSubstitution) {
			return substituteCommand(terminationCommand);
		} else {
			return terminationCommand;
		}
	}

	public boolean isTerminationVariableSubstitution() {
		return terminationVariableSubstitution;
	}

	public void setTerminationVariableSubstitution(boolean terminationVariableSubstitution) {
		this.terminationVariableSubstitution = terminationVariableSubstitution;
	}

	public boolean isUseTerminationCommand() {
		return useTerminationCommand;
	}

	public void setUseTerminationCommand(boolean useTerminationCommand) {
		this.useTerminationCommand = useTerminationCommand;
	}

	public String getTerminationCommand() {
		return terminationCommand;
	}

	public void setTerminationCommand(String terminationCommand) {
		this.terminationCommand = terminationCommand;
	}

	public static String getVariablePlaceholder(String variable) {
		return "${" + variable + "}";
	}

	public String getExecutionDirectoryForExecution() {
		if (variableSubstitution) {
			return substituteCommand(this.executionDirectory);
		} else {
			return executionDirectory;
		}
	}

}
