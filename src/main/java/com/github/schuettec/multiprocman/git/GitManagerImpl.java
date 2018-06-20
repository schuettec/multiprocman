package com.github.schuettec.multiprocman.git;

import static java.util.Objects.nonNull;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

public class GitManagerImpl implements AutoCloseable, GitManager {

	private Repository repository;
	private Git git;

	public GitManagerImpl(String gitDir) throws GitException {
		super();
		initialize(gitDir);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.github.schuettec.multiprocman.git.GitManagerI#checkoutBranch(java.lang.String)
	 */
	@Override
	public void checkoutBranch(Component parent, String branchName, boolean pullAfterCheckout) throws GitException {
		try {
			CheckoutCommand checkout = git.checkout();
			checkout.setCreateBranch(false);
			checkout.setName(branchName);
			checkout.call();

		} catch (RefNotFoundException e) {
			throw GitException.referenceNotFound(branchName, e);
		} catch (InvalidRefNameException e) {
			throw GitException.referenceNotFound(branchName, e);
		} catch (CheckoutConflictException e) {
			throw GitException.checkoutConflict(branchName, e);
		} catch (GitAPIException e) {
			throw GitException.general(e);
		}
	}

	@Override
	public String currentBranch() throws GitException {
		try {
			return repository.getFullBranch();
		} catch (IOException e) {
			throw GitException.general(e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.github.schuettec.multiprocman.git.GitManagerI#hasUncomittedChanges()
	 */
	@Override
	public boolean hasUncomittedChanges() throws GitException {
		try {
			StatusCommand status = git.status();
			Status statusResult = status.call();
			return (!statusResult.getUncommittedChanges()
			    .isEmpty());
		} catch (Exception e) {
			throw GitException.general(e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.github.schuettec.multiprocman.git.GitManagerI#branchList()
	 */
	@Override
	public List<String> branchList() throws GitException {
		try {
			return git.branchList()
			    .setListMode(ListMode.ALL)
			    .call()
			    .stream()
			    .map(Ref::getName)
			    .collect(Collectors.toList());
		} catch (Exception e) {
			throw GitException.general(e);
		}
	}

	private void initialize(String gitDir) throws GitException {
		try {
			this.repository = new RepositoryBuilder().setMustExist(true)
			    .setWorkTree(new File(gitDir))
			    .build();
			this.git = new Git(repository);
		} catch (Exception e) {
			finish();
			throw GitException.general(e);
		}
	}

	private void finish() {
		try {
			if (nonNull(git)) {
				git.close();
			}
		} catch (Exception e) {
		}
		try {
			if (nonNull(repository)) {
				repository.close();
			}
		} catch (Exception e) {
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.github.schuettec.multiprocman.git.GitManagerI#close()
	 */
	@Override
	public void close() throws Exception {
		finish();
	}

	public static GitManager noop() {
		return new GitManager() {

			@Override
			public boolean hasUncomittedChanges() throws GitException {
				return true;
			}

			@Override
			public void close() throws Exception {
			}

			@Override
			public List<String> branchList() throws GitException {
				return Collections.EMPTY_LIST;
			}

			@Override
			public String currentBranch() {
				return null;
			}

			@Override
			public void checkoutBranch(Component parent, String branchName, boolean pullAfterCheckout) throws GitException {

			}
		};
	}

}
