package com.github.schuettec.multiprocman.git;

import java.util.List;

public interface GitManager {

	void pull() throws GitException;

	void checkoutBranch(String branchName, boolean pullAfterCheckout) throws GitException;

	boolean hasUncomittedChanges() throws GitException;

	List<String> branchList() throws GitException;

	void close() throws Exception;

	String currentBranch() throws GitException;

}