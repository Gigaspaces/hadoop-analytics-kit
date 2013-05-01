package com.gigaspaces.hdfs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;

/**
 * Why did I create this rather than just using hadoop FileSystem directly?
 * Because the hdfs library isn't written to interfaces, and so isn't properly testable.
 * Note, this class is not threadsafe.
 * 
 * @author DeWayne
 *
 */
public class HdfsFileSystem implements FileSystem{
	private static final Logger log=Logger.getLogger(HdfsFileSystem.class.getName());
	private String curPath=null;
	org.apache.hadoop.fs.FileSystem fs;
	
	public HdfsFileSystem(String uri,String user){
		Configuration conf=new Configuration();
		if(user!=null)conf.set("hadoop.job.ugi", user);
		try {
			fs=org.apache.hadoop.fs.FileSystem.get(URI.create(uri),conf);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean pathExists(String path) {
		try {
			if(curPath==null || !path.equals(curPath)){
				return fs.exists(new Path(path));
			}
			else{
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	public FileOutputStream append(String path) {
		try {
			FSDataOutputStream fos=fs.append(new Path(path));			
			if(log.isLoggable(Level.FINE))log.fine("after fs.append fos="+fos);
			return new HdfsFileOutputStream(fos,path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public FileOutputStream create(String path) {
		try {
			FSDataOutputStream fos=fs.create(new Path(path));
			if(log.isLoggable(Level.FINE))log.fine("after fs.create fos="+fos);
			return new HdfsFileOutputStream(fos,path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public FileOutputStream createOrAppend(String path) {
		if(pathExists(path)==true){
			if(log.isLoggable(Level.FINE))log.fine("appending "+path);
			return append(path);
		}
		else{
			if(log.isLoggable(Level.FINE))log.fine("creating "+path);
			return create(path);
		}
	}
	
	public static void main(String[] args)throws Exception{
	}

}

