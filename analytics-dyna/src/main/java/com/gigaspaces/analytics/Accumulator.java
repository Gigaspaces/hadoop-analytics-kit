package com.gigaspaces.analytics;

import java.util.HashMap;
import java.util.Map;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 * This class is what actually accumulates information in the space by the
 * analytics code.
 * 
 * @author DeWayne
 *
 */
@SpaceClass
public class Accumulator {
	private String name;
	private Map<String,Integer> values=null;
	
	public Accumulator(){}
	
	public Accumulator(String name){
		this.name=name;
		values=new HashMap<String,Integer>();
	}
	
	@SpaceId(autoGenerate=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@SpaceRouting
	public Integer getRouting() {
		return null;
	}
	public void setRouting(Integer routing) {}

	public Map<String,Integer> getValues() {
		return values;
	}
	public void setValues(Map<String,Integer> values) {
		this.values = values;
	}
	
	public static void main(String[] args)throws Exception{
	}


}

