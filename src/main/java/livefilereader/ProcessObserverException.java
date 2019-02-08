package livefilereader;

public class ProcessObserverException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ProcessObserverException() {
	}

	public ProcessObserverException(String message) {
		super(message);
	}

	public ProcessObserverException(Throwable cause) {
		super(cause);
	}

	public ProcessObserverException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessObserverException(String message, Throwable cause, boolean enableSuppression,
	    boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
