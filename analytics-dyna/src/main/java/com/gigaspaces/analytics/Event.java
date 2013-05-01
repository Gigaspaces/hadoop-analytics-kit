package com.gigaspaces.analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gigaspaces.annotation.pojo.SpaceClass;

@SpaceClass
public class Event{
	private String name;
	private List<String> fields;
	private Boolean processed;
	
	public Event(){}
	
	public Event(String name){
		this.name=name;
		fields=new ArrayList<String>();
		processed=false;
	}
	
	public Event(String name, String...fields){
		this.name=name;
		this.fields=Arrays.asList(fields);
		processed=false;
	}

	public List<String> getFields() {
		return fields;
	}
	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	public void setFields(String...fields){
		this.fields=Arrays.asList(fields);
	}
	public Boolean getProcessed() {
		return processed;
	}
	public void setProcessed(Boolean processed) {
		this.processed = processed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
