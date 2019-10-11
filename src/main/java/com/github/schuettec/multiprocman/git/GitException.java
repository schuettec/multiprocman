package com.github.schuettec.multiprocman.git;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;

public class GitException extends Exception {

    public GitException() {
    }

    public GitException(String message) {
        super(message);
    }

    public GitException(Throwable cause) {
        super(cause);
    }

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static GitException general(Exception cause) {
        return new GitException("Initializing GIT failed.", cause);
    }

    public static GitException referenceNotFound(String branchName, Exception e) {
        return new GitException(
                String.format("Cannot checkout branch %s because this reference cannot be found.", branchName), e);
    }

    public static GitException checkoutConflict(String branchName, Exception e) {
        return new GitException(String.format("Cannot checkout branch %s due to checkout conflict.", branchName), e);
    }

    public static GitException pull(GitAPIException e) {
        return new GitException("Cannot pull due to an error while performing this GIT operation.", e);
    }

    public static GitException transportException(TransportException e) {
        return new GitException("Cannot communicate with GIT repository. Wrong credentials supplied?", e);
    }

}
