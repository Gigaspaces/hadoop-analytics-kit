package org.openspaces.analytics.archive;

import org.openspaces.analytics.archive.DynamicArchiverContainer.ArchiverCommand;
import org.openspaces.analytics.archive.DynamicArchiverContainer.ArchiverCommand.Command;
import org.openspaces.analytics.support.Messenger;
import org.openspaces.core.GigaSpace;

public class ArchiverMessenger extends Messenger<ArchiverCommand> {

	public ArchiverMessenger(GigaSpace space){
		super(space, ArchiverCommand.class);
	}

	public void setMode(DynamicArchiverContainer.ArchiverCommand.Mode mode)throws Exception{
		ArchiverCommand cmd=new ArchiverCommand();
		cmd.setCommand(Command.SET_MODE);
		cmd.setMode(mode);
		cmd=super.sendSync(cmd,5000);
		if(cmd==null)throw new Exception("Send timed out");
		if(cmd.getHadError())throw new Exception("Error:"+cmd.getErrorMessage());
	}
	
	public ArchiverCommand getMode()throws Exception{
		ArchiverCommand cmd=new ArchiverCommand();
		cmd.setCommand(Command.GET_MODE);
		cmd=super.sendSync(cmd,5000);
		if(cmd==null)throw new Exception("Send timed out");
		if(cmd.getHadError())throw new Exception("Error:"+cmd.getErrorMessage());
		return cmd;
	}
}
