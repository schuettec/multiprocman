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
		boolean isAsciiControl;
		byte data[];
		private boolean lineBreak;

		public Buffer(boolean isAscii, byte[] data, boolean lineBreak) {
			super();
			this.data = data;
			this.lineBreak = lineBreak;
			this.isAsciiControl = isAscii;
		}

		public boolean isAsciiControl() {
			return isAsciiControl;
		}

		public byte[] getData() {
			return data;
		}

		public int size() {
			return data.length;
		}

		public boolean isLineBreak() {
			return lineBreak;
		}

		public boolean isEmpty() {
			return data.length == 0;
		}

	}

	public InputCaptor(InputCaptorCallback callback, InputStream input, OutputStream output) {
		this.callback = callback;
		this.input = new BufferedInputStream(input);
		this.output = output;
	}

	/**
	 * Processes I/O until the next possible evaluation of
	 * {@link InputCaptorCallback#shouldRun()} returns <code>false</code>. To avoid
	 * blocking of the I/O operations, the streams should be closed.
	 *
	 * @throws IOException
	 */
	public void run() throws IOException {
		try {
			int bytesRead = 0;
			while (callback.shouldRun()) {
				Buffer buffer = bufferInputUntilNewLineOrAsciiCode(input);
				if (buffer.isEmpty()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Thread.currentThread()
						    .interrupt();
					}
					continue;
				}
				String string = new String(buffer.getData());
				System.out.print(string);
				bytesRead += buffer.size();
				// If xrefs is empty create the first line
				if (lineXrefs.isEmpty()) {
					lineXrefs.put(lines, 0);
				}
				if (buffer.isAsciiControl()) {
					output.write(buffer.getData());
					output.flush();
					lineXrefs.put(lines - 1, bytesRead);
					callback.append(string);
				} else {
					output.write(buffer.getData());
					output.flush();
					if (buffer.isLineBreak()) {
						lineXrefs.put(lines, bytesRead);
						lines++;
						callback.newLine(lines);
						callback.append(string);
					} else {
						lineXrefs.put(lines - 1, bytesRead);
						callback.append(string);
					}
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
		while (input.available() > 0) {
			input.mark(1);
			int b = input.read();

			// If the read char is an ascii code
			if (isSupportedAsciiCode(b)) {
				// ...and data was buffered before,
				if (dataRead) {
					// then return the buffered data and reset the stream to read the ascii code on
					// next loop.
					input.reset();
					byte[] data = buffer.toByteArray();
					return new Buffer(false, data, false);
				} else {
					// or there was no buffered data, then buffer all upcoming ascii codes until the
					// following sequence was
					// detected:
					// ascii-chars-asci < Stop at reading the first char of the second ascii
					// sequence and reset.
					ByteArrayOutputStream asciiBuffer = new ByteArrayOutputStream();
					asciiBuffer.write(b);
					// The sequence counter is 2 if the second ascii sequence was detected.
					boolean wasLineDelimiter = false;
					int sequenceCounter = 0;
					int next;
					while (input.available() > 0 && sequenceCounter < 2) {
						input.mark(1);
						next = input.read();
						if (isSupportedAsciiCode(next) && sequenceCounter == 0) {
							asciiBuffer.write(next);
						} else if (isLineDelimiter(next)) {
							asciiBuffer.write(next);
							sequenceCounter = 2;
							wasLineDelimiter = true;
						} else if (!isSupportedAsciiCode(next) && sequenceCounter < 2) {
							asciiBuffer.write(next);
							sequenceCounter = 1;
						} else if (isSupportedAsciiCode(next) && sequenceCounter == 1) {
							input.reset();
							sequenceCounter = 2;
						}
					}
					return new Buffer(true, asciiBuffer.toByteArray(), wasLineDelimiter);
				}
			}
			// if the char is not an ascii code
			else {
				// ...buffer the data
				buffer.write(b);
				if (isLineDelimiter(b)) {
					// Line finished, return the buffer
					byte[] data = buffer.toByteArray();
					return new Buffer(false, data, true);
				} else {
					// go to the next loop but remember already written data.
					dataRead = true;
				}
			}
		}
		// If the stream is empty, the return the buffer.
		// Note: It cannot be that the buffer contains ANSI control chars or a line delimiter.
		return new Buffer(false, buffer.toByteArray(), false);
	}

	private boolean isLineDelimiter(int b) {
		return (b == '\n');
	}

	private boolean isSupportedAsciiCode(int b) {
		return (b == 0x8);
	}

	/**
	 * Returns the start byte offset of the specified line. The lines are counted
	 * starting with 0.
	 *
	 * @param line
	 *        The line number starting with 0.
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

	/**
	 * Returns the end offset of the specified line.
	 * 
	 * @param line
	 *        The line to retrieve the end offset for.
	 * @return Returns the end offset of the specified line
	 */
	public int getLineEnd(int line) {
		if (lineXrefs.size() > line) {
			return lineXrefs.get(line);
		} else {
			throw new IllegalArgumentException(
			    String.format("Index out of bounds. Got %d lines but requesting line %d.", lineXrefs.size(), line));
		}
	}

}
