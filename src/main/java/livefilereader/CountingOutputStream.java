package livefilereader;

import java.io.IOException;
import java.io.OutputStream;

public class CountingOutputStream extends OutputStream {

	private int currentByte = 0;
	private OutputStream delegate;

	public CountingOutputStream(OutputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
		currentByte++;
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	public int getCurrentByte() {
		return currentByte;
	}

}
