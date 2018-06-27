package com.github.schuettec.multiprocman;

public class ASCIICode {
	public static final int BACKSPACE = 0x8;

	public static final int[] SUPPORTED = new int[] {
	    BACKSPACE
	};

	public static boolean isSupported(int code) {
		for (int supported : SUPPORTED) {
			if (code == supported) {
				return true;
			}
		}
		return false;
	}
}
