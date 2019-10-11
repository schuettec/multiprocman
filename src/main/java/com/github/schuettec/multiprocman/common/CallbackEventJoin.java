package com.github.schuettec.multiprocman.common;

import java.util.concurrent.TimeUnit;

/**
 * This class defines the {@link CallbackEventJoin} that joins multiple events into one by taking a specified maximum
 * event frequency into account.
 */
public class CallbackEventJoin extends EventJoin {

    private Callback callback;

    public CallbackEventJoin(Callback callback, long frequency, TimeUnit unit) {
        super(frequency, unit);
        this.callback = callback;
    }

    @Override
    public void acceptEvent() {
        callback.eventCallback();
    }

}
