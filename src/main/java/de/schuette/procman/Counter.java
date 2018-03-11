package de.schuette.procman;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JOptionPane;

public class Counter {
	private String name;
	private String regexp;
	private Color color;
	private transient Pattern pattern;

	public Counter() {
		super();
	}

	public Counter(String name, String regexp, Color c) {
		super();
		this.name = name;
		this.regexp = regexp;
		this.color = c;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean testRegexp() {
		return compile(true);
	}

	public boolean matches(String string) {
		if (this.pattern == null) {
			compile(false);
		}
		Matcher matcher = pattern.matcher(string);
		boolean matches = matcher.matches();
		return matches;
	}

	private boolean compile(boolean raiseException) {
		try {
			this.pattern = Pattern.compile(regexp);
			return true;
		} catch (PatternSyntaxException e) {
			if (raiseException) {
				JOptionPane.showMessageDialog(null,
				    String.format("The expression %s is not a valid regular expression. %s", regexp, e.getMessage()),
				    "Invalid expression", JOptionPane.ERROR_MESSAGE);
			}
			e.printStackTrace();
			return false;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	@Override
	public String toString() {
		return "Counter [name=" + name + ", regexp=" + regexp + ", color=" + color + "]";
	}

}
