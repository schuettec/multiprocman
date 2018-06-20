package com.github.schuettec.multiprocman.git;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;

import com.jcraft.jsch.Session;

public class GitManagerImpl implements AutoCloseable, GitManager {

	private Repository repository;
	private Git git;
	private static JschConfigSessionFactory sshSessionFactory;

	// private static Map<File, UsernamePasswordCredentialsProvider> credentialsProvider = new HashMap<>();

	public GitManagerImpl(String gitDir) throws GitException {
		super();
		initializeSSH();
		initialize(gitDir);

	}

	private void initializeSSH() {
		if (isNull(sshSessionFactory)) {
			sshSessionFactory = new JschConfigSessionFactory() {
				@Override
				protected void configure(Host host, Session session) {

					JPanel panel = new JPanel();
					JLabel label = new JLabel("Please enter the password for your ssh key:");
					JPasswordField pass = new JPasswordField(10);
					panel.add(label);
					panel.add(pass);
					String[] options = new String[] {
					    "OK", "Cancel"
					};
					int option = JOptionPane.showOptionDialog(null, panel, "SSH key", JOptionPane.NO_OPTION,
					    JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
					if (option == 0) // pressing OK button
					{
						char[] password = pass.getPassword();
						session.setPassword(new String(password));
					}
				}
			};
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.github.schuettec.multiprocman.git.GitManagerI#pull()
	 */
	@Override
	public void pull(Component parent) throws GitException {
		try {

			PullCommand pull = git.pull();
			// UsernamePasswordCredentialsProvider credentialsProvider = getCredentials(parent, repository);
			// pull.setCredentialsProvider(credentialsProvider);
			pull.setTransportConfigCallback(new TransportConfigCallback() {
				@Override
				public void configure(Transport transport) {
					SshTransport sshTransport = (SshTransport) transport;
					sshTransport.setSshSessionFactory(sshSessionFactory);
				}
			});
			pull.call();
		} catch (GitAPIException e) {
			throw GitException.pull(e);
		}
	}

	// private static UsernamePasswordCredentialsProvider getCredentials(Component parent, Repository repo) {
	// if (!credentialsProvider.containsKey(repo.getDirectory())) {
	//
	// JPanel panel = new JPanel(new BorderLayout(5, 5));
	//
	// JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
	// label.add(new JLabel("E-Mail", SwingConstants.RIGHT));
	// label.add(new JLabel("Password", SwingConstants.RIGHT));
	// panel.add(label, BorderLayout.WEST);
	//
	// JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
	// JTextField username = new JTextField();
	// controls.add(username);
	// JPasswordField password = new JPasswordField();
	// controls.add(password);
	// panel.add(controls, BorderLayout.CENTER);
	//
	// JOptionPane.showMessageDialog(parent, panel, "login", JOptionPane.QUESTION_MESSAGE);
	//
	// credentialsProvider.put(repo.getDirectory(),
	// new UsernamePasswordCredentialsProvider(username.getText(), password.getPassword()));
	// }
	// return credentialsProvider.get(repo.getDirectory());
	// }

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

			if (pullAfterCheckout) {
				pull(parent);
			}

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
			public void pull(Component parent) throws GitException {

			}

			@Override
			public void checkoutBranch(Component parent, String branchName, boolean pullAfterCheckout) throws GitException {

			}
		};
	}

}
