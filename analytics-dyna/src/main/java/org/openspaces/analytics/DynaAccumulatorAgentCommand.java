package org.openspaces.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openspaces.analytics.support.AsyncMessage;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;


/**
 * Represents a command for the PollingContainerAgent. There can only be one command of each
 * name 
 * 
 * @author DeWayne
 *
 */
@SpaceClass
public class DynaAccumulatorAgentCommand implements AsyncMessage {
	private String id=null;
	private Integer routingId=null;
	private String errmsg;
	private Boolean hadError=false;
	private String command=null;
	private Map<String,Object> parms=null;
	private MessageType messageType;
	
	public DynaAccumulatorAgentCommand(){}

	public static DynaAccumulatorAgentCommand newAccumulator(String name,int instances,int batchSize){
		DynaAccumulatorAgentCommand cmd=new DynaAccumulatorAgentCommand();

		cmd.setId(UUID.randomUUID().toString());
		cmd.setMessageType(AsyncMessage.MessageType.REQUEST);
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
		cmd.setMessageType(AsyncMessage.MessageType.REQUEST);
		cmd.setCommand("delete");
		cmd.setMessageType(DynaAccumulatorAgentCommand.MessageType.REQUEST);
		cmd.setParms(new HashMap<String,Object>());
		cmd.getParms().put("name",name);
		return cmd;
	}
	
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
	
	public String toString(){
		return String.format("{id=%s,type=%s,command=%s,parms=%s}",id,getMessageType(),command,parms);
	}

	@SpaceId(autoGenerate=true)
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
	
	@Override
	public void setMessageType(
			org.openspaces.analytics.support.AsyncMessage.MessageType messageType) {
		this.messageType=messageType;
	}

	@Override
	public MessageType getMessageType() {
		return messageType;
	}

	@Override
	public Boolean getHadError() {
		return hadError;
	}

	@Override
	public void setHadError(Boolean status) {
		hadError=status;
	}

	@Override
	public String getErrorMessage() {
		return errmsg;
	}

	@Override
	public void setErrorMessage(String message) {
		errmsg=message;
	}

}
