package com.gigaspaces.analytics;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 * Event translators are simple converters that change raw events posted to the
 * REST.  They must be implemented in a supported JVM language (Groovy for now),
 * accept a String as input (a POST body), and return a list of maps of String
 * value pairs. The handler must be able to handle any arbitrary event or
 * group of events.  The output maps must each have a key "_name_", which is
 * later used to trigger event handlers.
 * 
 * @author DeWayne
 *
 */
@SpaceClass
public class AccumulatorDef {
	private String name;
	private String language;
	private String code;
	
	public AccumulatorDef(){}
	
	public AccumulatorDef(String name, String language, String code) {
		super();
		this.name = name;
		this.language = language;
		this.code = code;
	}
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
	@SpaceId
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@SpaceRouting
	public Integer getRouting(){
		return null;
	}
	public void setRouting(Integer routing){}
}
