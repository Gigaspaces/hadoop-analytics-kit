package com.gigaspaces.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;

/**
 * Manages the lifecycle of dynamic accumulators (DynaAccumulatorService).
 * One instance per partition.
 * 
 * @author DeWayne
 */
@EventDriven @Polling
public class PollingContainerAgent {
	private static Logger log=Logger.getLogger(PollingContainerAgent.class.getName());
	private Map<String,SimplePollingEventListenerContainer> containers=new HashMap<String,SimplePollingEventListenerContainer>();
	@GigaSpaceContext
	private GigaSpace space;
	
	@EventTemplate
	public Object eventTemplate(){
		return new PollingContainerAgentCommand();
	}
	
	@SpaceDataEvent
	public PollingContainerAgentCommand commandHandler(PollingContainerAgentCommand cmd){
		if(cmd.getCommand().equals("new")){
			log.info("got \"new\" command");
			String ename=cmd.getName();
			if(containers.containsKey(ename)){
				log.severe("accumulator container '"+ename+"' already running");
				return null;
			}
			Event template=new Event();
			template.setName(ename);
			DynaAccumulatorContainer das=new DynaAccumulatorContainer(space,ename,template);
			MultiTakeReceiveOperationHandler receiveHandler = new MultiTakeReceiveOperationHandler();
			receiveHandler.setMaxEntries((Integer)cmd.getParms().get("batchSize"));
			SimplePollingEventListenerContainer container = new SimplePollingContainerConfigurer(space)
				.eventListenerAnnotation(das)
				.dynamicTemplateAnnotation(das)
				.passArrayAsIs(true)
				.receiveOperationHandler(receiveHandler)
				.concurrentConsumers((Integer)cmd.getParms().get("instances"))
				.create();
			containers.put(ename, container);
		}
		else if(cmd.getCommand().equals("delete")){
			
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
