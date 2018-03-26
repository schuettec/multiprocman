package com.github.schuettec.multiprocman.common;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class defines the {@link EventJoin} that joins multiple events into one by taking a specified maximum event
 * frequency into account.
 */
public class EventJoin {

	@FunctionalInterface
	public interface Callback {
		public void eventCallback();
	}

	private Timer timer;
	private AtomicBoolean timerIsWaiting = new AtomicBoolean(false);

	private Semaphore lock = new Semaphore(1);
	private Long lastEvent;
	private Long frequency;
	private Callback callback;

	public EventJoin(Callback callback, long frequency, TimeUnit unit) {
		requireNonNull(callback, "Callback must not be null!");
		requireNonNull(unit, "TimeUnit must not be null!");

		this.callback = callback;
		this.timer = new Timer();
		this.frequency = unit.toNanos(frequency);
	}

	private TimerTask timerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				try {
					fireEvent();
				} finally {
					timerIsWaiting.set(false);
				}
			}
		};
	}

	public void noticeEvent() {
		// Try to get the lock. If the lock cannot be acquired, another process is processing an event, so this
		// noticeEvent() call will be ignored since it is most likely to undercut the frequency.
		boolean acquired = lock.tryAcquire();
		if (acquired) {
			try {
				if (isNull(lastEvent)) {
					fireEvent();
				} else {
					long now = System.nanoTime();
					long elapsed = now - lastEvent;
					if (elapsed >= frequency) {
						fireEvent();
					} else {
						if (!timerIsWaiting.get()) {
							long delay = frequency - elapsed;
							timer.schedule(timerTask(), TimeUnit.NANOSECONDS.toMillis(delay));
							timerIsWaiting.set(true);
						}
					}
				}
			} finally {
				lock.release();
			}
		}
	}

	private void fireEvent() {
		callback.eventCallback();
		lastEvent = System.nanoTime();
	}

}
