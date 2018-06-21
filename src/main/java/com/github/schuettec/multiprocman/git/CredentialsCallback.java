package com.github.schuettec.multiprocman.git;

public interface CredentialsCallback {

	public String getPassphrase();

	public String getPassword();

	public boolean promptPassword(String message);

	public boolean promptPassphrase(String message);

	public boolean promptYesNo(String message);

	public void showMessage(String message);
}
