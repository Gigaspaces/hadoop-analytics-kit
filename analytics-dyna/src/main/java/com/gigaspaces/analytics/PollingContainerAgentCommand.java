package com.gigaspaces.analytics;

import java.util.HashMap;
import java.util.Map;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;


/**
 * Represents a command for the PollingContainerAgent. There can only be one command of each
 * name 
 * 
 * @author DeWayne
 *
 */
@SpaceClass
public class PollingContainerAgentCommand {
	private String name;
	private String command;
	private Map<String,Object> parms;
	
	public PollingContainerAgentCommand(){}

	public static PollingContainerAgentCommand newAccumulator(String name,int instances,int batchSize){
		PollingContainerAgentCommand cmd=new PollingContainerAgentCommand();

		cmd.setName(name);
		cmd.setCommand("new");
		cmd.setParms(new HashMap<String,Object>());
		cmd.getParms().put("instances",instances);
		cmd.getParms().put("batchSize",batchSize);
		return cmd;
	}
	
	@SpaceId(autoGenerate=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getRoutingId(){
		return null; //broadcast
	}
	public void setRoutingId(Integer rid){}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public Map<String, Object> getParms() {
		return parms;
	}
	public void setParms(Map<String, Object> parms) {
		this.parms = parms;
	}
	
	
}
