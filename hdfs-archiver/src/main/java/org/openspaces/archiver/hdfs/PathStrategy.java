package org.openspaces.archiver.hdfs;

/**
 * PathStrategy defines a target stream for a supplied array of objects.  Its purpose is
 * to both naming strategies, and rollover strategies.
 * 
 * @author DeWayne
 *
 */
public interface PathStrategy {
	
	/**
	 * Return an open stream for the supplied args
	 * 
	 * @param fs - the FileSystem to open streams on
	 * @param curStream - the current output stream, if any 
	 * @param objs - the batch of objects to be persisted
	 * @return - a stream suitable for writing the supplied objects
	 */
	FileOutputStream openPath(FileSystem fs,FileOutputStream curStream,Object[] objs);
}
