package com.github.schuettec.multiprocman.git;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.github.schuettec.multiprocman.Counter;
import com.github.schuettec.multiprocman.ProcessDescriptor;

public class BranchSelection {

	private ProcessDescriptor processDescriptor;

	private String selectedBranch;

	private Boolean isSaveToCheckOut;

	public BranchSelection(ProcessDescriptor processDescriptor) {
		super();
		this.processDescriptor = processDescriptor;
	}

	public String getSelectedBranch() {
		return selectedBranch;
	}

	public boolean isPullAfterCheckout() {
		return processDescriptor.isPullAfterCheckout();
	}

	public void checkoutBranch(Component component, String branchName, ProgressMonitorView monitor) throws GitException {
		processDescriptor.checkoutBranch(component, branchName, monitor);
	}

	public void setPullAfterCheckout(boolean pullBeforeCheckout) {
		processDescriptor.setPullAfterCheckout(pullBeforeCheckout);
		clearCache();
		try {
			isSaveToCheckout();
		} catch (GitException e) {
			this.isSaveToCheckOut = false;
		}
	}

	public void setSelectedBranch(String selectedBranch) {
		this.selectedBranch = selectedBranch;
		clearCache();
	}

	public ProcessDescriptor getProcessDescriptor() {
		return processDescriptor;
	}

	public boolean isPullBeforeCheckout() {
		return processDescriptor.isPullAfterCheckout();
	}

	@Override
	public int hashCode() {
		return processDescriptor.hashCode();
	}

	public String getCurrentBranch() throws GitException {
		return processDescriptor.getCurrentBranch();
	}

	public List<String> getAllBranches() throws GitException {
		return processDescriptor.getAllBranches();
	}

	public boolean isEnableGitSupport() {
		return processDescriptor.isEnableGitSupport();
	}

	public void setEnableGitSupport(boolean enableGitSupport) {
		processDescriptor.setEnableGitSupport(enableGitSupport);
	}

	public int getMaxLineNumbers() {
		return processDescriptor.getMaxLineNumbers();
	}

	public boolean isSaveToCheckout() throws GitException {
		if (this.isSaveToCheckOut == null) {
			if (processDescriptor.getCurrentBranch()
			    .equals(selectedBranch) && !isPullAfterCheckout()) {
				this.isSaveToCheckOut = true;
			} else {
				this.isSaveToCheckOut = processDescriptor.isSaveToCheckout();
			}
		}
		return this.isSaveToCheckOut;
	}

	public void setMaxLineNumbers(int maxLineNumbers) {
		processDescriptor.setMaxLineNumbers(maxLineNumbers);
	}

	public boolean isVariableSubstitution() {
		return processDescriptor.isVariableSubstitution();
	}

	public void setVariableSubstitution(boolean variableSubstitution) {
		processDescriptor.setVariableSubstitution(variableSubstitution);
	}

	public boolean hasEnvironmentVariables() {
		return processDescriptor.hasEnvironmentVariables();
	}

	public void setCounters(List<Counter> counters) {
		processDescriptor.setCounters(counters);
	}

	public List<Counter> getCounters() {
		return processDescriptor.getCounters();
	}

	public boolean hasCounters() {
		return processDescriptor.hasCounters();
	}

	public ImageIcon getIcon() {
		return processDescriptor.getIcon();
	}

	@Override
	public boolean equals(Object obj) {
		return processDescriptor.equals(obj);
	}

	public Color getColor() {
		return processDescriptor.getColor();
	}

	public void setColor(Color color) {
		processDescriptor.setColor(color);
	}

	public void setIcon(BufferedImage icon) {
		processDescriptor.setIcon(icon);
	}

	public void setIcon(ImageIcon icon) {
		processDescriptor.setIcon(icon);
	}

	public String getTitle() {
		return processDescriptor.getTitle();
	}

	public void setTitle(String title) {
		processDescriptor.setTitle(title);
	}

	public Charset getCharset() {
		return processDescriptor.getCharset();
	}

	public void setCharset(Charset charset) {
		processDescriptor.setCharset(charset);
	}

	public String getCommand() {
		return processDescriptor.getCommand();
	}

	public void setCommand(String command) {
		processDescriptor.setCommand(command);
	}

	public String getExecutionDirectory() {
		return processDescriptor.getExecutionDirectory();
	}

	public void setExecutionDirectory(String executionDirectory) {
		processDescriptor.setExecutionDirectory(executionDirectory);
	}

	public Map<String, String> getEnvironment() {
		return processDescriptor.getEnvironment();
	}

	public void setEnvironment(Map<String, String> environment) {
		processDescriptor.setEnvironment(environment);
	}

	public boolean hasExecutionDirectory() {
		return processDescriptor.hasExecutionDirectory();
	}

	@Override
	public String toString() {
		return processDescriptor.toString();
	}

	public boolean isSupportAsciiCodes() {
		return processDescriptor.isSupportAsciiCodes();
	}

	public void setSupportAsciiCodes(boolean supportAsciiCodes) {
		processDescriptor.setSupportAsciiCodes(supportAsciiCodes);
	}

	public boolean isTerminationVariableSubstitution() {
		return processDescriptor.isTerminationVariableSubstitution();
	}

	public void setTerminationVariableSubstitution(boolean terminationVariableSubstitution) {
		processDescriptor.setTerminationVariableSubstitution(terminationVariableSubstitution);
	}

	public boolean isUseTerminationCommand() {
		return processDescriptor.isUseTerminationCommand();
	}

	public void setUseTerminationCommand(boolean useTerminationCommand) {
		processDescriptor.setUseTerminationCommand(useTerminationCommand);
	}

	public String getTerminationCommand() {
		return processDescriptor.getTerminationCommand();
	}

	public void setTerminationCommand(String terminationCommand) {
		processDescriptor.setTerminationCommand(terminationCommand);
	}

	public String getExecutionDirectoryForExecution() {
		return processDescriptor.getExecutionDirectoryForExecution();
	}

	public void clearCache() {
		this.isSaveToCheckOut = null;
	}

	public void fetch() throws GitException {
		processDescriptor.fetch();
	}

}
