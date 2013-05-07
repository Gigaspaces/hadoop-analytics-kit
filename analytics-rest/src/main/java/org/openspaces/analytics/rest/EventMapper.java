package org.openspaces.analytics.rest;

import com.gigaspaces.annotation.pojo.SpaceClass;

/**
 * Event mappers are simple converters that change raw events posted to the
 * REST API.  They must be implemented in a supported JVM language (Groovy for now),
 * accept a String as input (a POST body), and using the supplied OutputCollector,
 * convert the body into one or more named lists (@see com.gigaspaces.analytics.Event)
 * 
 * @author DeWayne
 *
 */
@SpaceClass
public class EventMapper {
	private String language;
	private String code;
	
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

}
