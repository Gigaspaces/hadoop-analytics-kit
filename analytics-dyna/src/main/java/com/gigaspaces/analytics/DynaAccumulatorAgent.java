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
import org.openspaces.events.polling.ReceiveHandler;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;
import org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler;

import com.gigaspaces.analytics.DynaAccumulatorAgentCommand.MessageType;

/**
 * Manages the lifecycle of dynamic accumulators (DynaAccumulatorContainer).
 * One instance per partition.
 * 
 * @author DeWayne
 */
@EventDriven @Polling
public class DynaAccumulatorAgent {
	private static Logger log=Logger.getLogger(DynaAccumulatorAgent.class.getName());
	private Map<String,SimplePollingEventListenerContainer> containers=new HashMap<String,SimplePollingEventListenerContainer>();
	@GigaSpaceContext
	private GigaSpace space;

	@EventTemplate
	public Object eventTemplate(){
		DynaAccumulatorAgentCommand template= new DynaAccumulatorAgentCommand();
		template.setMessageType(MessageType.REQUEST);
		return template;
	}

	@ReceiveHandler
	public ReceiveOperationHandler receiveHandler(){
		return new SingleTakeReceiveOperationHandler();
	}

	@SpaceDataEvent
	public DynaAccumulatorAgentCommand commandHandler(DynaAccumulatorAgentCommand cmd){
		cmd.setMessageType(MessageType.RESPONSE);
		if(cmd.getCommand().equals("new")){
			try{
				log.info("got \"new\" command");
				String ename=(String)cmd.getParms().get("name");
				if(containers.containsKey(ename)){
					respondFailed(cmd,"accumulator container '"+ename+"' already running");
					return null;
				}
				//default code just counts field
				String code="fields.each(){changes.increment(it,1);}";
				int batchSize=1;
				int instances=1;
				
				String pcode=(String)cmd.getParms().get("code");
				if(pcode!=null){
					code=pcode;
				}
				else{
					log.info("no code supplied: default counting accumulator used");
				}
				Integer pbatchSize=(Integer)cmd.getParms().get("batchSize");
				if(pbatchSize!=null)batchSize=pbatchSize;
				Integer pinstances=(Integer)cmd.getParms().get("instances");
				if(pinstances!=null)instances=pinstances;
				
				log.info("-----------------------------------");
				log.info("Creating accumulator with parms:");
				log.info("  name="+ename);
				log.info("  code="+code);
				log.info("  batch size="+batchSize);
				log.info("  instances="+instances);
				log.info("-----------------------------------");
				
				Event template=new Event();
				template.setName(ename);
				DynaAccumulatorContainer das=new DynaAccumulatorContainer(space,ename,template);
				MultiTakeReceiveOperationHandler receiveHandler = new MultiTakeReceiveOperationHandler();
				receiveHandler.setMaxEntries(batchSize);
				SimplePollingEventListenerContainer container = new SimplePollingContainerConfigurer(space)
				.eventListenerAnnotation(das)
				.dynamicTemplateAnnotation(das)
				.passArrayAsIs(true)
				.receiveOperationHandler(receiveHandler)
				.concurrentConsumers(instances)
				.create();
				container.start();
				containers.put(ename, container);
				
				//Now deploy the accumulator code
				AccumulatorDef def=new AccumulatorDef();
				def.setName(ename);
				def.setLanguage("groovy");  //hardcoded for now (simplicity)
				def.setCode(code);
				space.write(def,10000L);
				
				//Wait for accumulator to appear
				Accumulator atemplate=new Accumulator();
				atemplate.setName(ename);
				Accumulator acc=null;
				for(int i=0;i<10;i++){
					Thread.sleep(1000L);
					acc=space.read(atemplate,1000L);
					if(acc!=null)break;
				}
				if(acc==null){
					respondFailed(cmd,"accumulator creation timed out");
				}
				cmd.setSuccess(true);
			}
			catch(Throwable e){
				respondFailed(cmd,e.getMessage());
				return null;
			}
			space.write(cmd,10000);
		}
		else if(cmd.getCommand().equals("delete")){
			String name=(String)cmd.getParms().get("name");
			if(name==null){
				respondFailed(cmd,"request to delete with no accumulator name supplied");
				return null;
			}
			SimplePollingEventListenerContainer cont=containers.get(name);
			if(cont==null){
				respondFailed(cmd,"delete request:container not found for name:"+name);
				return null;
			}
			try{
				cont.destroy();
				containers.remove(name);
			}
			catch(Throwable t){
				respondFailed(cmd,"container destroy failed:"+t.getMessage());
				return null;
			}
			cmd.setSuccess(true);
			log.info("container destroyed:"+name);
			space.write(cmd,10000);
		}
		return null;
	}
	
	private void respondFailed(DynaAccumulatorAgentCommand cmd,String message){
		cmd.setSuccess(false);
		log.severe(message);
		space.write(cmd,10000);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
