package de.schuette.procman;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.ImageIcon;

public class ProcessDescriptor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ImageIcon icon;
	private String title;
	private String command;
	private File executionDirectory;
	private Color color;
	private Charset charset;
	private Map<String, String> environment;

	private List<Counter> counters;

	public ProcessDescriptor() {
		super();
		setCharset(Charset.defaultCharset());
		setIcon(Resources.getTerminal());
		setColor(Color.GREEN);
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

	public File getExecutionDirectory() {
		return executionDirectory;
	}

	public void setExecutionDirectory(File executionDirectory) {
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

}
