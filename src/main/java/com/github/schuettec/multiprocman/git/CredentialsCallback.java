package com.github.schuettec.multiprocman.git;

import java.awt.Component;

public interface CredentialsCallback {

	public String getPassphrase();

	public String getPassword();

	public boolean promptPassword(String message, Component rootComponent);

	public boolean promptPassphrase(String message, Component rootComponent);

	public boolean promptYesNo(String message, Component rootComponent);

	public void showMessage(String message, Component rootComponent);

	public void clearCache();
}
