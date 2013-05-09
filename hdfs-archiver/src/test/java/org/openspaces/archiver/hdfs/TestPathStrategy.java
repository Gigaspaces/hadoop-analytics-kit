package org.openspaces.archiver.hdfs;

import org.openspaces.archiver.hdfs.FileOutputStream;
import org.openspaces.archiver.hdfs.FileSystem;
import org.openspaces.archiver.hdfs.PathStrategy;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;

public class TestPathStrategy implements PathStrategy,ClusterInfoAware {
	private ClusterInfo ci;

	public FileOutputStream openPath(FileSystem fs,FileOutputStream curStream,Object[] objs){
		if(curStream==null){
			return new TestFileSystem.TestFileOutputStream("/files/file_"+ci.getInstanceId());
		}
		else
			return curStream;
	}

	public void setClusterInfo(ClusterInfo ci) {
		this.ci=ci;
	}

}
