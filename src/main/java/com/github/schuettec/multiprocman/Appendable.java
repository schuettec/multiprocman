package com.github.schuettec.multiprocman;

import java.awt.Color;

public interface Appendable {

	/**
	 * Appends the specified string in the specified color.
	 *
	 * @param c The string to append
	 * @param s The color to use.
	 */
	public void append(Color c, String s);

	/**
	 * @param s Appends this string with support for ANSI color codes.
	 */
	public void appendANSI(String s);

}
