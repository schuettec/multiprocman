package com.github.schuettec.multiprocman.git;

import static java.util.Objects.nonNull;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.TrackingRefUpdate;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class GitManagerImpl implements GitManager, AutoCloseable {

    private static final List<GitManagerImpl> instances = new LinkedList<>();

    public static void closeAll() {
        for (GitManagerImpl impl : instances) {
            impl.finish();

        }
    }

    private Repository repository;
    private Git git;
    private JschConfigSessionFactory sshSessionFactory;
    private Component rootComponent;
    private CredentialsCallback callback;

    public GitManagerImpl(Component rootComponent, CredentialsCallback callback, String gitDir) throws GitException {
        instances.add(this);
        this.callback = callback;
        this.sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setUserInfo(new UserInfo() {
                    @Override
                    public String getPassphrase() {
                        return callback.getPassphrase();
                    }

                    @Override
                    public String getPassword() {
                        return callback.getPassword();
                    }

                    @Override
                    public boolean promptPassword(String message) {
                        return callback.promptPassword(message, rootComponent);
                    }

                    @Override
                    public boolean promptPassphrase(String message) {
                        return callback.promptPassphrase(message, rootComponent);
                    }

                    @Override
                    public boolean promptYesNo(String message) {
                        return callback.promptYesNo(message, rootComponent);
                    }

                    @Override
                    public void showMessage(String message) {
                        callback.showMessage(message, rootComponent);
                    }
                });
            }
        };
        initialize(rootComponent, gitDir);
    }

    @Override
    public void pull(ProgressMonitorView monitor) throws GitException {
        PullCommand pull = git.pull();
        if (nonNull(monitor)) {
            pull.setProgressMonitor(monitor);
        }
        pull.setTransportConfigCallback(transport -> {
            if (transport instanceof SshTransport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
        });
        Component oldRoot = this.rootComponent;
        try {
            this.rootComponent = monitor.getRootComponent();
            pull.call();
        } catch (TransportException e) {
            callback.clearCache();
            throw GitException.transportException(e);
        } catch (GitAPIException e) {
            throw GitException.general(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (nonNull(oldRoot)) {
                this.rootComponent = oldRoot;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.github.schuettec.multiprocman.git.GitManagerI#checkoutBranch(java.lang. String)
     */
    @Override
    public void checkoutBranch(Component parent, String branchName, boolean pullAfterCheckout,
            ProgressMonitorView monitor) throws GitException {

        try {
            if (!branchName.equals(currentBranch())) {
                if (GitManager.isRemoteBranch(branchName)) {
                    String localTrackingName = branchName.replace(REMOTE_PREFIX, "");
                    String remoteOriginName = "origin/" + localTrackingName;
                    git.checkout().setCreateBranch(true).setName(localTrackingName)
                            .setUpstreamMode(SetupUpstreamMode.TRACK).setStartPoint(remoteOriginName).call();

                } else {
                    CheckoutCommand checkout = git.checkout();
                    checkout.setCreateBranch(false);
                    checkout.setName(branchName);
                    checkout.call();
                }
            }

            if (pullAfterCheckout) {
                pull(monitor);
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

    @Override
    public boolean hasUncomittedChanges() throws GitException {
        try {
            StatusCommand status = git.status();
            Status statusResult = status.call();
            return (!statusResult.getUncommittedChanges().isEmpty());
        } catch (Exception e) {
            throw GitException.general(e);
        }
    }

    @Override
    public List<String> branchList() throws GitException {
        try {
            return git.branchList().setListMode(ListMode.ALL).call().stream().map(Ref::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw GitException.general(e);
        }
    }

    private void initialize(Component rootComponent, String gitDir) throws GitException {
        try {
            this.rootComponent = rootComponent;
            this.repository = new RepositoryBuilder().setMustExist(true).setWorkTree(new File(gitDir)).build();
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

    @Override
    public void fetch() throws GitException {
        try {
            FetchCommand fetch = git.fetch();
            fetch.setTransportConfigCallback(transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(sshSessionFactory);
                }
            });
            fetch.setRemote("origin");
            fetch.call();
        } catch (TransportException e) {
            callback.clearCache();
            throw GitException.transportException(e);
        } catch (Exception e) {
            throw GitException.general(e);
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
            public void checkoutBranch(Component parent, String branchName, boolean pullAfterCheckout,
                    ProgressMonitorView monitor) throws GitException {

            }

            @Override
            public void pull(ProgressMonitorView monitor) {

            }

            @Override
            public void fetch() throws GitException {

            }
        };
    }

}
