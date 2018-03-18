package com.github.schuettec.multiprocman;

import java.awt.Color;

public interface Appendable {

	/**
	 * Appends the specified string in the specified color.
	 *
	 * @param c The string to append
	 * @param s The color to use.
	 * @param supportBackspace If <code>true</code> the ASCII backspace code is recognized to provide rudimental support
	 *        for layouted appends.
	 */
	public void append(Color c, String s, boolean isSupportAsciiCodes);

	/**
	 * @param s Appends this string with support for ANSI color codes.
	 * @param supportBackspace If <code>true</code> the ASCII backspace code is recognized to provide rudimental
	 *        support
	 *        for layouted appends.
	 */
	public void appendANSI(String s, boolean isSupportAsciiCodes);

}
