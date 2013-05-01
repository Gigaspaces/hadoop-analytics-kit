package com.gigaspaces.hdfs;

/**
 * Abstraction to enable testing
 * 
 * @author DeWayne
 *
 */
public interface FileSystem {
	/**
	 * Tests whether a path exists in the file system
	 * 
	 * @param path
	 * @return
	 */
	boolean pathExists(String path);
	/**
	 * Returns an append stream to the given path.  Note that append
	 * is an optional capability of HDFS.
	 * 
	 * @param path
	 * @return the stream to append to
	 */
	FileOutputStream append(String path);
	/**
	 * Creates a file for writing.
	 * 
	 * @param path
	 * @return the stream to write to
	 */
	FileOutputStream create(String path);
	/**
	 * Convenience method that creates if the path doesn't exist,
	 * appends otherwise.
	 * 
	 * @param path
	 * @return the stream to write or append to.
	 */
	FileOutputStream createOrAppend(String path);
}
