package com.gigaspaces.analytics;

import java.io.Serializable;

/**
 * This interface exists to allow opaque message types for
 * client/pu communication
 * 
 * @author DeWayne
 *
 */
public interface Message extends Serializable {
	void setId(String id);
	String getId();
}
