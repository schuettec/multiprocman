package com.github.schuettec.multiprocman.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class GitTest {

	public static void main(String... args) throws IOException, GitAPIException {
		final String REMOTE_URL = "git@github.com:remondis-it/remap.git";
		SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
			@Override
			protected void configure(OpenSshConfig.Host host, Session session) {
				session.setUserInfo(new UserInfo() {
					@Override
					public String getPassphrase() {
						return null;
					}

					@Override
					public String getPassword() {
						return null;
					}

					@Override
					public boolean promptPassword(String message) {
						return true;
					}

					@Override
					public boolean promptPassphrase(String message) {
						return true;
					}

					@Override
					public boolean promptYesNo(String message) {
						return true;
					}

					@Override
					public void showMessage(String message) {
					}
				});
			}
		};
		File localPath = File.createTempFile("TestGitRepository", "");
		localPath.delete();
		try (Git result = Git.cloneRepository()
		    .setURI(REMOTE_URL)
		    .setTransportConfigCallback(transport -> {
			    SshTransport sshTransport = (SshTransport) transport;
			    sshTransport.setSshSessionFactory(sshSessionFactory);
		    })
		    .setDirectory(localPath)
		    .call()) {
			System.out.println("Having repository: " + result.getRepository()
			    .getDirectory());
		}
	}

}
