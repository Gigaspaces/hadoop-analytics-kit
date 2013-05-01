package com.gigaspaces.hdfs;

import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;


public class ArchiverTest {
	@Test
	public void test() throws Exception{

		IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();		
		ClusterInfo clusterInfo = new ClusterInfo();
		//clusterInfo.setSchema("default");
		clusterInfo.setNumberOfInstances(1);
		//clusterInfo.setNumberOfBackups(0);
		clusterInfo.setInstanceId(1);
		provider.setClusterInfo(clusterInfo);

		// set the config location (override the default one - classpath:/META-INF/spring/pu.xml)
		provider.addConfigLocation("classpath:/test-pu.xml");

		// Build the Spring application context and "start" it
		ProcessingUnitContainer container = provider.createContainer();

		UrlSpaceConfigurer us=new UrlSpaceConfigurer("jini://*/*/space");
		GigaSpace space=new GigaSpaceConfigurer(us.space()).gigaSpace();
		space.writeMultiple(new TestClass[]{
				new TestClass(),		
				new TestClass(),		
				new TestClass(),		
				new TestClass(),		
				new TestClass(),		
				new TestClass(),		
				new TestClass(),		
				new TestClass(),		
				new TestClass(),		
				new TestClass(),		
		});

		container.close();		
		
	}
	
}

@SpaceClass
class TestClass{
	private String id;

	@SpaceId(autoGenerate=true)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String toString(){
		return "test\n";
	}
}
