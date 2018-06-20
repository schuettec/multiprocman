package com.github.schuettec.multiprocman.git;

import java.awt.Component;
import java.util.List;

public interface GitManager {

	void checkoutBranch(Component parent, String branchName, boolean pullAfterCheckout) throws GitException;

	default void checkoutBranch(String branchName, boolean pullAfterCheckout) throws GitException {
		checkoutBranch(null, branchName, pullAfterCheckout);
	}

	boolean hasUncomittedChanges() throws GitException;

	List<String> branchList() throws GitException;

	void close() throws Exception;

	String currentBranch() throws GitException;

}