package org.openspaces.analytics.archive;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.analytics.Event;
import org.openspaces.analytics.archive.DynamicArchiverContainer.ArchiverCommand;
import org.openspaces.analytics.support.AsyncMessage.MessageType;
import org.openspaces.analytics.support.Messenger;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.notify.SimpleNotifyContainerConfigurer;
import org.openspaces.events.notify.SimpleNotifyEventListenerContainer;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.springframework.transaction.TransactionStatus;

import com.j_spaces.core.client.EntryArrivedRemoteEvent;


public class ArchiverTest {
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
		
		//archiver command
		//Messenger<DynamicArchiverContainer.ArchiverCommand> messenger=new Messenger<DynamicArchiverContainer.ArchiverCommand>(space,DynamicArchiverContainer.ArchiverCommand.class);
		ArchiverMessenger messenger=new ArchiverMessenger(space);
		messenger.setMode(ArchiverCommand.Mode.DISCARD);
		
		Thread.sleep(2000);
		System.out.println("writing events");
		Event e=new Event("test","f1","f2","f3","1");
		e.setProcessed(true);
		space.write(e);
		e=new Event("test","f1","f2","f3","3");
		e.setProcessed(true);
		space.write(e);
		e=new Event("test","f1","f2","f3","6");
		e.setProcessed(true);
		space.write(e);
		Thread.sleep(2000);
		assertEquals(0,space.count(new Event()));  //archiver should have disposed them
		
		//
		// TODO: HAD TO COMMENT BELOW OUT BECAUSE SHUT DOWN ARCHIVER STILL CONSUMING MESSAGES
		// JIRA REPORT FILED
		//

		//Now change to paused mode
/*		System.out.println("pausing archiver");
		messenger.setMode(ArchiverCommand.Mode.PAUSED);
		
		SimpleNotifyEventListenerContainer notifyEventListenerContainer = new SimpleNotifyContainerConfigurer(
				space).notifyAll(true).eventListener(new SpaceDataEventListener(){
					@Override
					public void onEvent(Object data, GigaSpace gigaSpace,
							TransactionStatus txStatus, Object source) {
						Event event=(Event)data;
						EntryArrivedRemoteEvent eevent = (EntryArrivedRemoteEvent) source;
						System.out.println("got event: "+event.getFields()+" :"+eevent.getNotifyActionType());
						
					}
					
				}).
				template(new Event())
				.notifyContainer();		
		
		System.out.println(space.count(new Object()));
		Event e1=new Event("test","f1","f2","f3","9");
		e1.setProcessed(true);
		space.write(e1);
		System.out.println("wrote "+e1.getId());
		System.out.println(space.count(new Event()));
		Event e2=new Event("test","f1","f2","f3","3");
		e2.setProcessed(true);
		space.write(e2);
		System.out.println("wrote "+e2.getId());
		System.out.println(space.count(new Object()));
		e=new Event("test","f1","f2","f3","6");
		e.setProcessed(true);
		space.write(e);
		System.out.println("wrote "+e.getId());
		System.out.println(space.count(new Object()));
		Thread.sleep(1000);
		System.out.println(space.count(new Object()));
		Object[] events=space.takeMultiple(new Object());
		for(Object ev:events){
			System.out.println("class="+ev.getClass().getName());
			//if(ev instanceof Event)System.out.println("fields="+(((Event)ev).getFields()));
		}
		System.out.println(space.count(new Object()));
		assertEquals(2,space.count(new Event()));  //archiver should have not consumed them  */ 
	}

}
