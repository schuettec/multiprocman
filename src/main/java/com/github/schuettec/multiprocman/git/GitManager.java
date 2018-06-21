package com.github.schuettec.multiprocman.git;

import java.awt.Component;
import java.util.List;

public interface GitManager {

	public void checkoutBranch(Component parent, String branchName, boolean pullAfterCheckout) throws GitException;

	public default void checkoutBranch(String branchName, boolean pullAfterCheckout) throws GitException {
		checkoutBranch(null, branchName, pullAfterCheckout);
	}

	public abstract boolean hasUncomittedChanges() throws GitException;

	public abstract List<String> branchList() throws GitException;

	public abstract void close() throws Exception;

	public abstract String currentBranch() throws GitException;

	public abstract void pull() throws GitException;

}