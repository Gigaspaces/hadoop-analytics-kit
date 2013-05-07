package org.openspaces.analytics;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

/**
 * This class is what actually accumulates information in the space by the
 * analytics code.  The generic type is a wildcard, and the actual type is determined
 * by the change api call for individual entries.
 * 
 * @author DeWayne
 *
 */
@SpaceClass
public class Accumulator {
	private String name=null;
	private Map<String,?> values=null;
	
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
	@JsonIgnore
	public Integer getRouting() {
		return null;
	}
	public void setRouting(Integer routing) {}

	public Map<String,?> getValues() {
		return values;
	}
	public void setValues(Map<String,?> values) {
		this.values = values;
	}
	
	public static void main(String[] args)throws Exception{
	}


}

