package livefilereader;

import java.io.File;

/**
 * This interface provides line to byte offset translation that should be used to access the output capturing file
 * efficiently.
 */
public interface ProcessOutputInfo {

	public File getFile();

	public int getStartOffset(int lineNumber);

	public int getEndOffset(int lineNumber);

}
