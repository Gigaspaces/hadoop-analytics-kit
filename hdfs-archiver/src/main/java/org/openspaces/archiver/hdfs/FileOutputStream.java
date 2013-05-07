package org.openspaces.archiver.hdfs;

/**
 * Abstraction of FileOutputStream.  Creating mainly to make mock
 * testing possible.
 * 
 * @author DeWayne
 *
 */
public interface FileOutputStream {
	/**
	 * returns the current path that this stream is writing to.
	 * 
	 * @return the path
	 */
	String getPath();
	void write(byte[] bytes);
	/**
	 * flushes the underlying stream if supported
	 */
	void flush();
}
