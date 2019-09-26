package com.github.schuettec.multiprocman.common;

import static com.github.schuettec.multiprocman.common.CumulatedEventJoin.returnOnNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CumulatedEventJoinTest {

	@Mock
	private Consumer<String> consumer;

	@Test
	public void shouldCumulate() throws InterruptedException {

		CumulatedEventJoin<String> eventJoin = new CumulatedEventJoin<String>(() -> "",
		    returnOnNull((str1, str2) -> str1 + str2), consumer, 10l, TimeUnit.MILLISECONDS);
		eventJoin.noticeEvent("1-");
		eventJoin.noticeEvent("2-");
		eventJoin.noticeEvent("3-");
		eventJoin.noticeEvent("4-");
		eventJoin.noticeEvent("5");

		Thread.sleep(100);
		verify(consumer, times(2)).accept(anyString());
	}

}
