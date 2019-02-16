package livefilereader.captor;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InputCaptorTest {

	private static final String BACKSPACE = String.valueOf((char) 0x8);
	private static final String LINES_WITH_ANSI = "Line0\r\nLine1\n" + BACKSPACE + "1234" + BACKSPACE + BACKSPACE
			+ BACKSPACE + BACKSPACE + "Line3\n";

	@Mock
	InputCaptorCallback callback;

	@Before
	public void setup() {
		when(callback.shouldRun()).thenReturn(true, true, true, true, true, false);
	}

	@Test
	public void shouldWriteInputToOutput() throws IOException {
		byte[] bytes = LINES_WITH_ANSI.getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		InputCaptor captor = new InputCaptor(callback, in, out);
		captor.run();

		assertArrayEquals(bytes, out.toByteArray());
	}

}
