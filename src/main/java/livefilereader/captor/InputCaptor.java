package livefilereader.captor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class InputCaptor {

	private int lines = 0;

	private ConcurrentHashMap<Integer, Integer> lineXrefs = new ConcurrentHashMap<>();

	private BufferedInputStream input;
	private OutputStream output;

	private InputCaptorCallback callback;

	private class Buffer {
		boolean isAsciiControll;
		byte data[];
		int ascii;

		public Buffer(byte[] data) {
			super();
			this.data = data;
			this.isAsciiControll = false;
		}

		public Buffer(int ascii) {
			super();
			this.ascii = ascii;
			this.isAsciiControll = true;
		}

		public boolean isAsciiControl() {
			return isAsciiControll;
		}

		public byte[] getData() {
			return data;
		}

		public int getAscii() {
			return ascii;
		}

		public int size() {
			return data.length;
		}
	}

	public InputCaptor(InputCaptorCallback callback, InputStream input, OutputStream output) {
		this.callback = callback;
		this.input = new BufferedInputStream(input);
		this.output = output;
	}

	/**
	 * Processes I/O until the next possible evaluation of {@link InputCaptorCallback#shouldRun()} returns
	 * <code>false</code>. To avoid blocking of the I/O operations, the streams should be closed.
	 *
	 * @throws IOException
	 */
	public void run() throws IOException {
		try {
			while (callback.shouldRun()) {
				Buffer buffer = bufferInputUntilNewLineOrAsciiCode(input);
				if (buffer.isAsciiControl()) {
					output.write(buffer.getAscii());
					output.flush();
					// If xrefs is empty create the first line
					if (lineXrefs.isEmpty()) {
						lineXrefs.put(lines, 0);
					}
					Integer byteOffset = lineXrefs.get(lines - 1);
					lineXrefs.put(lines - 1, byteOffset + 1);
					// TODO: Process ascii controll if it ocurred within the observed frame
					callback.asciiCode(lines - 1, buffer.getAscii());
				} else {
					output.write(buffer.getData());
					output.flush();
					lineXrefs.put(lines, buffer.size());
					lines++;
					callback.newLine(lines);
				}
			}
		} catch (IOException e) {
			if (callback.shouldRun()) {
				throw e;
			} else {
				System.out.println("InputCaptor was requested to stop and IO Exception was thrown.");
				e.printStackTrace();
			}
		}
	}

	private Buffer bufferInputUntilNewLineOrAsciiCode(BufferedInputStream input) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		boolean dataRead = false;
		while (true) {
			input.mark(1);
			int b = input.read();
			// If the read char is an ascii code
			if (isSupportedAsciiCode(b)) {
				// ...and data was buffered before,
				if (dataRead) {
					// then return the buffered data and reset the stream to read the ascii code on next loop.
					input.reset();
					byte[] data = buffer.toByteArray();
					return new Buffer(data);
				} else {
					// or there was no buffered data, then return the ascii code to process it immidiately.
					return new Buffer(b);
				}
			}
			// if the char is not an ascii code
			else {
				// ...buffer the data
				buffer.write(b);
				if (isLineDelimiter(b)) {
					// Line finished, return the buffer
					byte[] data = buffer.toByteArray();
					return new Buffer(data);
				} else {
					// go to the next loop but remember already written data.
					dataRead = true;
				}
			}
		}
	}

	private boolean isLineDelimiter(int b) {
		return (b == '\n');
	}

	private boolean isSupportedAsciiCode(int b) {
		return (b == 0x8);
	}

	/**
	 * Returns the start byte offset of the specified line. The lines are counted starting with 0.
	 *
	 * @param line The line number starting with 0.
	 */
	public int getLineByteOffset(int line) {
		if (line <= 0) {
			return 0;
		} else if (lineXrefs.size() > line) {
			return lineXrefs.get(line);
		} else {
			throw new IllegalArgumentException(
			    String.format("Index out of bounds. Got %d lines but requesting line %d.", lineXrefs.size(), line));
		}
	}

	public int getLines() {
		return lines;
	}

}
