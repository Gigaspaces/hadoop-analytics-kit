package com.gigaspaces.hdfs;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.openspaces.core.cluster.ClusterInfo;

import com.gigaspaces.hdfs.strategies.SimplePatternPathStrategy;

public class SimplePatternPathStrategyTest {
	@Test
	public void test(){
		SimplePatternPathStrategy sp=new SimplePatternPathStrategy("/files/%D_%p");
		ClusterInfo ci=new ClusterInfo();
		ci.setInstanceId(1);
		sp.setClusterInfo(ci);
		FileOutputStream fos=sp.openPath(new TestFileSystem(),null,null);
		String date=new SimpleDateFormat("yyyyMMdd").format(new Date());
		org.junit.Assert.assertEquals("/files/"+date+"_1",fos.getPath());
	}
}
