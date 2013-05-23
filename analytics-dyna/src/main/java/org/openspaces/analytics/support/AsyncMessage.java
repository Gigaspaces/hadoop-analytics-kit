package org.openspaces.analytics.support;

import java.io.Serializable;

/**
 * This interface exists to allow opaque message types for
 * client/pu communication
 * 
 * @author DeWayne
 *
 */
public interface AsyncMessage extends Serializable {
	public static enum MessageType{
		REQUEST,
		RESPONSE
	}
	
	void setId(String id);
	String getId();
	
	MessageType getMessageType();
	void setMessageType(MessageType messageType);
	
	/* fields to deal with response status */
	Boolean getHadError();
	void setHadError(Boolean status);
	
	String getErrorMessage();
	void setErrorMessage(String message);
}
