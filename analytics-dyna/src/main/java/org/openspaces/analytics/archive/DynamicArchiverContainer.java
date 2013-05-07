package org.openspaces.analytics.archive;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.openspaces.analytics.DynaAccumulatorAgentCommand;
import org.openspaces.analytics.DynaAccumulatorAgentCommand.MessageType;
import org.openspaces.analytics.archive.DynamicArchiverContainer.ArchiverCommand.Mode;
import org.openspaces.archive.ArchiveOperationHandler;
import org.openspaces.archive.ArchivePollingContainer;
import org.openspaces.archive.ArchivePollingContainerConfigurer;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.events.DynamicEventTemplateProvider;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;
import org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler;
import org.springframework.transaction.PlatformTransactionManager;

import com.gigaspaces.annotation.pojo.SpaceClass;

/**
 * A manager that allows real time control of an archiver.
 * 
 * @author DeWayne
 *
 */
public class DynamicArchiverContainer implements DynamicEventTemplateProvider  {
	private static final Logger log=Logger.getLogger(DynamicArchiverContainer.class.getName());
	@GigaSpaceContext
	private GigaSpace space;
	private ArchivePollingContainer container;
	private ArchiveOperationHandler handler;
	private Object template;
	private int batchSize;
	private PlatformTransactionManager txm;
	private Mode currentMode=Mode.PAUSED;

	public DynamicArchiverContainer(PlatformTransactionManager txm,ArchiveOperationHandler handler,Object template, int batchSize){
		this.template=template;
		this.handler=handler;
		this.txm=txm;
		this.batchSize=batchSize;
	}

	@Override
	public Object getDynamicTemplate() {
		ArchiverCommand template= new ArchiverCommand();
		return template;
	}

	public ArchiverCommand commandHandler(ArchiverCommand cmd){
		log.info("received command "+cmd.getMode());
		switch (cmd.getMode()){
		case PAUSED:
			if(currentMode==Mode.PAUSED)break;
			if(handler instanceof DiscardingArchiveHandler)((DiscardingArchiveHandler)handler).setDiscard(false);
			if(container!=null)container.stop();
			break;
		case ACTIVE:
			if(currentMode==Mode.ACTIVE)break;
			if(container==null)createContainer();
			if(handler instanceof DiscardingArchiveHandler)((DiscardingArchiveHandler)handler).setDiscard(false);
			createContainer();
			container.start();
			break;
		case DISCARD:
			if(!(handler instanceof DiscardingArchiveHandler)){
				log.severe("discard mode not supported by archiver");
				return null;
			}
			if(currentMode==Mode.DISCARD)break;
			((DiscardingArchiveHandler)handler).setDiscard(true);
			createContainer();
			container.start();
			break;
		}

		return null;
	}

	private void createContainer() {
		
		if(container==null){
			log.info("creating container");
			container=new ArchivePollingContainerConfigurer(space)           
			.archiveHandler(handler)
			.transactionManager(txm)
			.batchSize(batchSize)
			.dynamicTemplate(new DynamicEventTemplateProvider() {
				@Override
				public Object getDynamicTemplate() {
					return template;
				}}).create();
		}
	}

	public void start(){
		if(container.isRunning())return;
		container.start();
	}

	public void stop(){
		if(!container.isRunning())return;
		container.stop();
	}
	
	public void destroy(){
		if(container==null)return;
		container.destroy();
		container=null;
	}

	@PostConstruct
	public void postConstruct(){

	}

	@SpaceClass
	public static class ArchiverCommand
	{
		public static enum Mode{
			PAUSED,
			ACTIVE,
			DISCARD
		}

		private Mode mode;

		public ArchiverCommand(){}

		public ArchiverCommand(Mode mode){
			this.mode=mode;
		}

		public Mode getMode() {
			return mode;
		}

		public void setMode(Mode mode) {
			this.mode = mode;
		}
	}

}
