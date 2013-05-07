package org.openspaces.archiver.hdfs.serializers;

import java.io.ByteArrayOutputStream;

import org.openspaces.archiver.hdfs.HdfsSerializer;


/**
 * Simple serializer that just uses the Object.toString method 
 *  
 * @author DeWayne
 *
 */
public class ToStringHdfsSerializer implements HdfsSerializer {

	public byte[] serialize(Object[] objs) {
		StringBuilder sb=new StringBuilder();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try{
			for(Object obj:objs){
				sb.setLength(0);
				baos.write(sb.append(obj.toString()).append("\n").toString().getBytes());
			}
			return baos.toByteArray();
		}
		catch(Exception e){
			if(e instanceof RuntimeException)throw (RuntimeException)e;
			throw new RuntimeException(e);
		}
	}

}
