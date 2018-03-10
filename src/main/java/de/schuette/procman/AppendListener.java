package de.schuette.procman;

import java.awt.Color;

/**
 * The {@link AppendListener} receives ANSI Color-decoded append operations from implementations of
 * {@link Appendable}.
 */
@FunctionalInterface
public interface AppendListener {
	/**
	 * Appends the specified string in the specified color.
	 *
	 * @param c The string to append
	 * @param s The color to use.
	 */
	public void append(Color c, String s);
}
