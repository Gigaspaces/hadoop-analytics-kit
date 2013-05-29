package org.openspaces.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openspaces.analytics.support.AbstractMessageServer;
import org.openspaces.analytics.support.AsyncMessage.MessageType;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;


/**
 * Manages the lifecycle of dynamic accumulators (DynaAccumulatorContainer).
 * One instance per partition.
 * 
 * @author DeWayne
 */

public class DynaAccumulatorAgent extends AbstractMessageServer<DynaAccumulatorAgentCommand>{
	private static Logger log=Logger.getLogger(DynaAccumulatorAgent.class.getName());
	private Map<String,SimplePollingEventListenerContainer> containers=new HashMap<String,SimplePollingEventListenerContainer>();

	public DynaAccumulatorAgent(GigaSpace space) {
		super(space, DynaAccumulatorAgentCommand.class);
	}
	
	@PostConstruct
	public void postConstruct(){
		log.info("starting listener");
		startListener();
	}

	@PreDestroy
	public void preDestroy(){
		super.stopListener();
	}
		
	@Override
	public DynaAccumulatorAgentCommand handleMessage(DynaAccumulatorAgentCommand cmd){
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
				template.setProcessed(false);
				DynaAccumulatorContainer das=new DynaAccumulatorContainer(space,ename,template,"groovy",code);
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
				cmd.setHadError(false);
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
			//delete accumulator object
			Accumulator acc=new Accumulator();
			acc.setName(name);
			space.clear(acc);
			
			cmd.setHadError(false);
			log.info("container destroyed:"+name);
			space.write(cmd,10000);
		}
		return null;
	}
	
	private void respondFailed(DynaAccumulatorAgentCommand cmd,String message){
		cmd.setHadError(true);
		cmd.setErrorMessage(message);
		log.severe(message);
		space.write(cmd,10000);
	}


}
