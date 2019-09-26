package com.github.schuettec.multiprocman.common;

import static com.github.schuettec.multiprocman.common.CumulatedEventJoin.returnOnNull;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class CumulatedEventJoinTest {

	@Test
	public void shouldCumulate() {

		CumulatedEventJoin<String> eventJoin = new CumulatedEventJoin<String>(returnOnNull((str1, str2) -> str1 + str2),
		    System.out::println, 100, TimeUnit.MILLISECONDS);
		eventJoin.noticeEvent("1-");
		eventJoin.noticeEvent("2-");
		eventJoin.noticeEvent("3-");
		eventJoin.noticeEvent("4-");
		eventJoin.noticeEvent("5");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
