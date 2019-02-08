package livefilereader;

public class ProcessStartException extends ProcessObserverException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ProcessStartException() {
	}

	public ProcessStartException(String message) {
		super(message);
	}

	public ProcessStartException(Throwable cause) {
		super(cause);
	}

	public ProcessStartException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessStartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
