package org.openspaces.analytics;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.analytics.Accumulator;
import org.openspaces.analytics.DynaAccumulatorAgentCommand;
import org.openspaces.analytics.Event;
import org.openspaces.analytics.archive.DynamicArchiverContainer;
import org.openspaces.analytics.archive.DynamicArchiverContainer.ArchiverCommand;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;


public class AccumulatorTest {
	private static GigaSpace space;
	private ProcessingUnitContainer pu;
	
	
	@Before
	public void before()throws Exception{
		IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
		provider.addConfigLocation("classpath:pu.xml");
		pu = provider.createContainer();
		space=new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/space").space()).gigaSpace();
	}
	
	@After
	public void after(){
		if(pu!=null)pu.close();		
	}

	@Test
	public void test() throws Exception{
		assertNotNull(space);
		
		DynaAccumulatorAgentCommand cmd=DynaAccumulatorAgentCommand.newAccumulator("test",1,1);
		cmd.getParms().put("code","changes.increment(fields[0],1);"+
				"changes.increment(\"count\",fields[3].toInteger());"
				);
		space.write(cmd);
		
		//archiver command
		DynamicArchiverContainer.ArchiverCommand ac=new DynamicArchiverContainer.ArchiverCommand();
		ac.setMode(ArchiverCommand.Mode.DISCARD);
		space.write(ac);
		
		Thread.sleep(5000);
		System.out.println("writing events");
		space.write(new Event("test","f1","f2","f3","1"));
		space.write(new Event("test","f1","f2","f3","3"));
		space.write(new Event("test","f1","f2","f3","6"));
		Thread.sleep(2000);
		Accumulator acc=space.read(new Accumulator());
		assertNotNull(acc);
		assertEquals((Integer)3,acc.getValues().get("f1"));
		assertEquals((Integer)10,acc.getValues().get("count"));
	}

}
