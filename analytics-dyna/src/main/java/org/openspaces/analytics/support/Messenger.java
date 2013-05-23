package org.openspaces.analytics.support;

import org.openspaces.analytics.support.AsyncMessage.MessageType;
import org.openspaces.core.GigaSpace;

public class Messenger<T extends AsyncMessage> {
	private GigaSpace space;
	private Class<T> messageClass;

	public Messenger(GigaSpace space,Class<T> messageClass){
		this.space=space;
		this.messageClass=messageClass;
	}
	
	public T sendSync(T message, int timeout) throws Exception{ 
		message.setHadError(false);
		message.setMessageType(MessageType.REQUEST);
		space.write(message,1000L);
		AsyncMessage template=messageClass.newInstance();
		template.setMessageType(MessageType.RESPONSE);
		template.setId(message.getId());
		return (T)space.take(template,timeout);
	}
}
