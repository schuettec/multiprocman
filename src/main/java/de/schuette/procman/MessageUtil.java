package de.schuette.procman;

import javax.swing.JOptionPane;

public class MessageUtil {
	public static void showError(String title, String messageFormat, Object... params) {
		JOptionPane.showMessageDialog(null, String.format(messageFormat, params), title, JOptionPane.ERROR_MESSAGE);
	}
}
