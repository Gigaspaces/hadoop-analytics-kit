package org.openspaces.archiver.hdfs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.apache.hadoop.fs.FSDataOutputStream;

/**
 * Hdfs implementation.
 * @author DeWayne
 *
 */
public class HdfsFileOutputStream implements FileOutputStream {
	private static final Logger log=Logger.getLogger(HdfsFileOutputStream.class.getName());
	FSDataOutputStream fsdos;
	private String path;
	
	public HdfsFileOutputStream(FSDataOutputStream fsdos, String path){
		this.fsdos=fsdos;
		this.path=path;
	}
	public void write(byte[] bytes) {
		try {
			log.info(String.format("writing %d bytes.  thread=%d",bytes.length,Thread.currentThread().getId()));
			fsdos.write(bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public void flush(){
		try{
			fsdos.flush();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	public String getPath() {
		return path;
	}
	
	@PreDestroy
	public void close(){
		if(log.isLoggable(Level.FINE))log.fine("close called");
		if(fsdos!=null)
			try {
				fsdos.close();
			} catch (IOException e) {
			}
	}
}
