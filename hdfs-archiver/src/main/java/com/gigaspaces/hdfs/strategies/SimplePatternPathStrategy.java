package com.gigaspaces.hdfs.strategies;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;

import com.gigaspaces.hdfs.FileOutputStream;
import com.gigaspaces.hdfs.FileSystem;
import com.gigaspaces.hdfs.PathStrategy;

/**
 * Builds a path from a pattern string.  Ultra-simplistic:
 * 
 * %% = %
 * %D = date (yyyymmdd)
 * %T = time (hhmmss)
 * %p = partition id
 * 
 * @author DeWayne
 *
 */
public class SimplePatternPathStrategy implements PathStrategy,ClusterInfoAware {
	private String pattern;
	private ClusterInfo ci;
	private final SimpleDateFormat datefmt=new SimpleDateFormat("yyyyMMdd");
	private final SimpleDateFormat timefmt=new SimpleDateFormat("HHmmss");
	
	public SimplePatternPathStrategy(String pattern){
		if(pattern==null)throw new IllegalArgumentException("null pattern supplied");
		this.pattern=pattern;
	}

	public FileOutputStream openPath(FileSystem fs,FileOutputStream curStream, Object[] objs) {
		if(fs==null)throw new IllegalArgumentException("null filesystem supplied");
		if(curStream==null){
			return fs.createOrAppend(generate());
		}
		return curStream; //keep super-simple now.  Rollover might be nice feature here.
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setClusterInfo(ClusterInfo ci) {
		this.ci=ci;
	}
	
	private String generate(){
		Date date=new Date();
		String res=pattern.replaceAll("%D", datefmt.format(date));
		res=res.replaceAll("%T", timefmt.format(date));
		res=res.replaceAll("%p", ci.getInstanceId().toString());
		return res;
	}

}
