package livefilereader.captor;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InputCaptorTest {

	private static final String LINES_WITH_ANSI = "Line0\r\nLine1\n\b1234\b\b\b\bLine3\n";

	@Mock
	InputCaptorCallback callback;

	@Test
	public void shouldWriteInputToOutput() throws IOException {
		// if the InputCaptor is implemented correctly, the callback may not return false even if the stream would be empty
		// on next loop.
		when(callback.shouldRun()).thenReturn(true, true, true, true, false);
		byte[] bytes = LINES_WITH_ANSI.getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputCaptor captor = new InputCaptor(callback, in, out);
		captor.run();
		assertArrayEquals(bytes, out.toByteArray());
		verify(callback).newLine(1, "Line0\r\n");
		verify(callback).newLine(2, "Line1\n");
		verify(callback).append("\b1234");
		verify(callback).append("\b\b\b\bLine3\n");
	}

	@Test
	public void shouldNotBlockIfNoInput_onMainLoop() throws IOException {
		// if the InputCaptor is implemented correctly, the callback may not return false even if the stream would be empty
		// on next loop.
		when(callback.shouldRun()).thenReturn(true, false);
		String expectedString = "ThisIsANotFinishedLine";
		byte[] bytes = expectedString.getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputCaptor captor = new InputCaptor(callback, in, out);
		captor.run();
		assertArrayEquals(bytes, out.toByteArray());
		verify(callback).append(expectedString);
	}

	@Test
	public void shouldNotBlockIfNoInput_onAnsiSequenceLoop() throws IOException {
		// if the InputCaptor is implemented correctly, the callback may not return false even if the stream would be empty
		// on next loop.
		when(callback.shouldRun()).thenReturn(true, true, false);
		String expectedString = "ThisIsAnUnfinishedANSILine\b\b\b";
		byte[] bytes = expectedString.getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputCaptor captor = new InputCaptor(callback, in, out);
		captor.run();
		assertArrayEquals(bytes, out.toByteArray());
		verify(callback).append("ThisIsAnUnfinishedANSILine");
		verify(callback).append("\b\b\b");
	}
}
