package com.github.schuettec.multiprocman.common;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventJoinTest {

    @Mock
    private Callback callback;

    @Test
    public void shouldPerformFirstCall() {
        EventJoin join = new CallbackEventJoin(callback, 2, TimeUnit.SECONDS);
        join.noticeEvent();
        verify(callback, times(1)).eventCallback();
    }

    @Test
    public void shouldJoinCallsAndFireLast() {
        EventJoin join = new CallbackEventJoin(callback, 500, TimeUnit.MILLISECONDS);
        join.noticeEvent();
        verify(callback, times(1)).eventCallback();
        join.noticeEvent();
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
        }
        verify(callback, times(2)).eventCallback();
    }

    @Test
    public void shouldJoinCallsAndSkipSchedulingIfTimerIsWaiting() {
        EventJoin join = new CallbackEventJoin(callback, 500, TimeUnit.MILLISECONDS);
        join.noticeEvent();
        verify(callback, times(1)).eventCallback();
        join.noticeEvent();
        join.noticeEvent();
        join.noticeEvent();
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
        }
        verify(callback, times(2)).eventCallback();
    }

    @Test
    public void shouldWaitUntilElapsed() {
        EventJoin join = new CallbackEventJoin(callback, 500, TimeUnit.MILLISECONDS);
        join.noticeEvent();
        verify(callback, times(1)).eventCallback();
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
        }
        join.noticeEvent();
        join.noticeEvent();
        join.noticeEvent();
        verify(callback, times(2)).eventCallback();
    }

}
