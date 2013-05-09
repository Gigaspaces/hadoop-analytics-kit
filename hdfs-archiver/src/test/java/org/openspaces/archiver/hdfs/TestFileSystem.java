package org.openspaces.archiver.hdfs;

import java.util.logging.Logger;

import org.openspaces.archiver.hdfs.FileOutputStream;
import org.openspaces.archiver.hdfs.FileSystem;


public class TestFileSystem implements FileSystem {
	private static final Logger log=Logger.getLogger(TestFileSystem.class.getName());

	public TestFileSystem(){
		log.info("testfilesystem starting");
	}
	public boolean pathExists(String path) {
		return false;
	}

	public FileOutputStream append(String path) {
		return new TestFileOutputStream(path);
	}

	public FileOutputStream create(String path) {
		return new TestFileOutputStream(path);
	}

	public FileOutputStream createOrAppend(String path) {
		return new TestFileOutputStream(path);
	}
	
	public static class TestFileOutputStream implements FileOutputStream{
		Logger log=Logger.getLogger(TestFileOutputStream.class.getName());
		private String path;
		public TestFileOutputStream(String path){
			this.path=path;
			log.info("opening file "+path);
		}
		public void write(byte[] bytes) {
			log.info("writing "+new String(bytes));
		}
		public String getPath() {
			return path;
		}
		@Override
		public void flush() {
		}
	}
}
