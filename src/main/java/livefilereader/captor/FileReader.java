package livefilereader.captor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.exception.ExceptionUtils;

import livefilereader.ProcessOutputInfo;

public class FileReader {

	private ReentrantLock lock = new ReentrantLock();
	private RandomAccessFile input;
	private Charset charset;
	private ProcessOutputInfo fileInfo;

	public FileReader(Charset charset, ProcessOutputInfo fileInfo) throws FileNotFoundException {
		super();
		this.input = new RandomAccessFile(fileInfo.getFile(), "r");
		this.charset = charset;
		this.fileInfo = fileInfo;
	}

	public String readLinesFromFile(int fromLine, int linesToRead) {
		try {
			lock.tryLock(1000, TimeUnit.SECONDS);
			try {
				return synchronizedReadLinesFromFile(fromLine, linesToRead);
			} finally {
				lock.unlock();
			}
		} catch (InterruptedException e) {
			return "Could not read application error due to another process accessing the output.";
		}
	}

	private String synchronizedReadLinesFromFile(int fromLine, int linesToRead) {
		if (linesToRead == 0) {
			return "";
		}
		try {
			int startOffsets = fileInfo.getStartOffset(fromLine);
			int endOffsets = fileInfo.getEndOffset(fromLine + linesToRead - 1);
			input.seek(startOffsets);
			if (endOffsets - startOffsets < 0) {
				System.out.println("HÄÄÄÄ");
			}
			byte[] data = new byte[endOffsets - startOffsets];
			input.read(data, 0, data.length);
			return new String(data);
		} catch (IOException e) {
			return new StringBuilder().append("Could not read application output.")
			    .append(ExceptionUtils.getStackTrace(e))
			    .toString();
		}
	}

	public void close() {
		try {
			this.input.close();
		} catch (IOException e) {
		}
	}

}
