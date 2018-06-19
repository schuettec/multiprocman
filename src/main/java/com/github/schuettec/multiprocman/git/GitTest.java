package com.github.schuettec.multiprocman.git;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

public class GitTest {

	public static void main(String[] args) throws IOException, RefAlreadyExistsException, RefNotFoundException,
	    InvalidRefNameException, CheckoutConflictException, GitAPIException {
		try (Repository repository = new RepositoryBuilder().setMustExist(true)
		    .setWorkTree(new File("C:\\Users\\schuettec\\git\\article-service"))
		    .build()) {
			try (Git git = new Git(repository)) {

				List<Ref> branchList = git.branchList()
				    .call();
				System.out.println("The following local branches are available:");
				for (Ref ref : branchList) {
					System.out.println(" - " + ref.getName());
				}
				List<Ref> remoteBranchList = git.branchList()
				    .setListMode(ListMode.REMOTE)
				    .call();
				System.out.println("The following remote branches are available:");
				for (Ref ref : remoteBranchList) {
					System.out.println(" - " + ref.getName());
				}

				StatusCommand status = git.status();
				Status statusResult = status.call();
				if (!statusResult.getUncommittedChanges()
				    .isEmpty()) {
					System.out.println("There are uncommitted changes. Aborting.");
					return;
				}

				CheckoutCommand checkout = git.checkout();
				checkout.setCreateBranch(false);
				checkout.setName("develop");
				Ref checkoutResult = checkout.call();
				System.out.println("Branch checked out successfully.");
			}
		}
	}

}
