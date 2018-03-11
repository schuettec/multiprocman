package com.github.schuettec.multiprocman;

import java.util.Arrays;
import java.util.List;

public class TextUtil {
	public static List<String> getLinesFromString(String s) {
		return Arrays.asList(s.split("\\\n|\\\r\\\n"));
	}
}
