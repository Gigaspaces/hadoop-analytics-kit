package org.openspaces.archiver.hdfs;

/**
 * Simple interface to provide injectable serialization
 * strategies.
 * 
 * @author DeWayne
 *
 */
public interface HdfsSerializer {
	/**
	 * Takes an array of objects and returns a byte array.
	 * 
	 * @param objs the batch to serialize
	 * @return the serialized data
	 */
	byte[] serialize(Object[] objs);
}
