package com.github.schuettec.multiprocman.git;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class DefaultCredentialsCallback implements CredentialsCallback {

	private String passphrase;
	private String password;

	@Override
	public String getPassphrase() {
		String p = passphrase;
		this.passphrase = null;
		return p;
	}

	@Override
	public String getPassword() {
		String p = password;
		this.password = null;
		return p;
	}

	@Override
	public void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message, "GIT message", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public boolean promptPassword(String message) {
		JPanel panel = new JPanel();
		JLabel label = new JLabel(message);
		JPasswordField pass = new JPasswordField();
		panel.add(label);
		panel.add(pass);
		String[] options = new String[] { "OK", "Cancel" };
		int option = JOptionPane.showOptionDialog(null, panel, "GIT credentials", JOptionPane.NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
		if (option == 0) {
			this.password = new String(pass.getPassword());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean promptPassphrase(String message) {
		JPanel panel = new JPanel();
		JLabel label = new JLabel(message);
		JPasswordField pass = new JPasswordField();
		panel.add(label);
		panel.add(pass);
		String[] options = new String[] { "OK", "Cancel" };
		int option = JOptionPane.showOptionDialog(null, panel, "GIT credentials", JOptionPane.NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
		if (option == 0) {
			this.passphrase = new String(pass.getPassword());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean promptYesNo(String message) {
		int option = JOptionPane.showConfirmDialog(null, message, "GIT question", JOptionPane.YES_NO_OPTION);
		if (option == 0) {
			return true;
		} else {
			return false;
		}
	}

}
