package com.github.schuettec.multiprocman;

import java.util.LinkedList;
import java.util.List;

public class TextUtil {
	public static List<String> getLinesFromString(String s) {
		List<String> lines = new LinkedList<>();
		StringBuilder line = new StringBuilder("");
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\r') {
				continue;
			} else if (c == '\n') {
				lines.add(line.toString());
				line = new StringBuilder();
			} else {
				line.append(c);
			}
		}
		lines.add(line.toString());
		return lines;
	}
}
