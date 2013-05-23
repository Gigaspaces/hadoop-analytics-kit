package org.openspaces.analytics.support;

import java.util.logging.Logger;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.adapter.MethodEventListenerAdapter;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler;

/**
 * Base class to simplify creation of services that use simple message passing.
 * Used by analytics infrastructure components to pass and respond to commands
 * 
 * @author DeWayne
 *
 * @param <T>
 */
public abstract class AbstractMessageServer<T extends AsyncMessage> {
	private static final Logger log=Logger.getLogger(AbstractMessageServer.class.getName());
	protected GigaSpace space;
	protected SimplePollingEventListenerContainer container;
	protected Class<T> asyncMessageClass;

	protected AbstractMessageServer(GigaSpace space,Class<T> asyncMessageClass ){
		this.space=space;
		this.asyncMessageClass=asyncMessageClass;
	}

	public void startListener(){
		try{
			if(container!=null)return;

			AsyncMessage msgTemplate=asyncMessageClass.newInstance();
			msgTemplate.setMessageType(AsyncMessage.MessageType.REQUEST);

			container = new SimplePollingContainerConfigurer(space)
				.eventListenerMethod(new Listener<T>(this),"handleEvent")
				.template(msgTemplate)
				.autoStart(true)
				.receiveOperationHandler(new SingleTakeReceiveOperationHandler())
				.concurrentConsumers(1)
				.create();
			
			log.info("started container");
		}
		catch(Exception e){
			e.printStackTrace();
			if(e instanceof RuntimeException)throw (RuntimeException)e;
			else throw new RuntimeException(e);
		}
	}

	public void stopListener(){
		if(container!=null){
			container.destroy();
			container=null;
		}
	}

	public abstract T handleMessage(T message);
}

final class Listener<T extends AsyncMessage> extends MethodEventListenerAdapter{
	Logger log=Logger.getLogger(Listener.class.getName());

	public AbstractMessageServer<T> delegate;

	public Listener(AbstractMessageServer<T> delegate){
		this.delegate=delegate;
		super.setMethodName("handleEvent");
	}

	//default handler method
	public T handleEvent(T message){
		//call subclass implementation
		//log.info("calling delegate with "+message);
		System.out.println("calling delegate");
		return delegate.handleMessage(message);
	}

}

