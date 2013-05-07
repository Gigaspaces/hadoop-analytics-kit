package org.openspaces.archiver.hdfs;

/**
 * Delays connection until first use
 * 
 * @author DeWayne
 *
 */
public class LazyHdfsFileSystem implements FileSystem {
	private HdfsFileSystem hdfs=null;
	private String uri,user;

	public LazyHdfsFileSystem(String uri, String user) {
		this.uri=uri;
		this.user=user;
	}
	
	private void connect(){
		if(hdfs!=null)return;
		hdfs=new HdfsFileSystem(uri,user);
	}

	@Override
	public boolean pathExists(String path) {
		connect();
		return hdfs.pathExists(path);
	}

	@Override
	public FileOutputStream append(String path) {
		connect();
		return hdfs.append(path);
	}

	@Override
	public FileOutputStream create(String path) {
		connect();
		return hdfs.create(path);
	}

	@Override
	public FileOutputStream createOrAppend(String path) {
		connect();
		return hdfs.createOrAppend(path);
	}

}
