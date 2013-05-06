package com.gigaspaces.analytics.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.TaskGigaSpace;

import com.gigaspaces.analytics.Message;
import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncResult;

/**
 * This class encapsulates logic that requires broadcast communication
 * to various polling containers.  Doesn't do much except simplify
 * calling code.
 * 
 * @author DeWayne
 *
 */
public class Scatterer {

	public static <T extends Message> List<T> scatter(GigaSpace space,T message,T template, long timeout) throws InterruptedException, ExecutionException{
		DistTask<T> task=new DistTask<T>(message,template,timeout);
		AsyncFuture<List<T>> future = space.execute(task);
		return future.get();
	}
}


class DistTask<T extends Message> implements DistributedTask<T, List<T>> {
	@TaskGigaSpace
	private transient GigaSpace space;
	private T message;
	private T template;
	private long timeout;
	
	public DistTask(){}
	
	public DistTask(T message, T template, long timeout){
		this.message=message;
		this.template=template;
		this.timeout=timeout;
	}
	/**
	 * Write command/message into local space and wait for response
	 */
	public T execute() throws Exception {
		space.write(message,10000L);
		template.setId(message.getId());
		return space.take(template,timeout);
	}

	public List<T> reduce(List<AsyncResult<T>> results) throws Exception {
		List<T> outlist=new ArrayList<T>();
		for (AsyncResult<T> result : results) {
			if (result.getException() != null) {
				throw result.getException();
			}
			outlist.add(result.getResult());
		}
		return outlist;
	}
}
