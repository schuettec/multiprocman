package com.github.schuettec.multiprocman.process;

public class ProcessBufferOutputException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ProcessBufferOutputException() {
	}

	public ProcessBufferOutputException(String message) {
		super(message);
	}

	public ProcessBufferOutputException(Throwable cause) {
		super(cause);
	}

	public ProcessBufferOutputException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessBufferOutputException(String message, Throwable cause, boolean enableSuppression,
	    boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
