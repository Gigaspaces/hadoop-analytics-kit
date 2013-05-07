package org.openspaces.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;


/**
 * Represents a command for the PollingContainerAgent. There can only be one command of each
 * name 
 * 
 * TODO: Command infrastructure needs to work in partitioned spaces
 * 
 * @author DeWayne
 *
 */
@SpaceClass
public class DynaAccumulatorAgentCommand implements Message {
	private String id=null;
	private Integer routingId=null;
	private String command=null;
	private MessageType messageType=null;
	private Boolean success=null; //for responses
	private Map<String,Object> parms=null;
	
	public static enum MessageType{
		REQUEST,
		RESPONSE
	}
	
	public DynaAccumulatorAgentCommand(){}

	public static DynaAccumulatorAgentCommand newAccumulator(String name,int instances,int batchSize){
		DynaAccumulatorAgentCommand cmd=new DynaAccumulatorAgentCommand();

		cmd.setId(UUID.randomUUID().toString());
		cmd.setCommand("new");
		cmd.setMessageType(DynaAccumulatorAgentCommand.MessageType.REQUEST);
		cmd.setParms(new HashMap<String,Object>());
		cmd.getParms().put("name",name);
		cmd.getParms().put("instances",instances);
		cmd.getParms().put("batchSize",batchSize);
		return cmd;
	}
	
	public static DynaAccumulatorAgentCommand deleteAccumulator(String name){
		DynaAccumulatorAgentCommand cmd=new DynaAccumulatorAgentCommand();

		cmd.setId(UUID.randomUUID().toString());
		cmd.setCommand("delete");
		cmd.setMessageType(DynaAccumulatorAgentCommand.MessageType.REQUEST);
		cmd.setParms(new HashMap<String,Object>());
		cmd.getParms().put("name",name);
		return cmd;
	}
	
	@SpaceId(autoGenerate=false)
	@Override
	public String getId() {
		return id;
	}
	@Override
	public void setId(String id) {
		this.id = id;
	}
	@SpaceRouting
	public Integer getRoutingId(){
		return routingId; 
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
	
	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}
