package livefilereader;

/**
 * This interface provides line to byte offset translation that should be used to access the output capturing file
 * efficiently.
 */
public interface ProcessOutputInfo {

	public int getLineByteOffset(int line);

	public int getLineEnd(int line);

}
