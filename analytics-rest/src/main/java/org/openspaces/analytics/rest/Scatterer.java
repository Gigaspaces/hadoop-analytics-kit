package org.openspaces.analytics.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.openspaces.analytics.rest.Scatterer.Scatterable;
import org.openspaces.analytics.support.AsyncMessage;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.TaskGigaSpace;

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

	/**
	 * Scatter a bunch of read an write interactions.
	 * 
	 * @param space
	 * @param message
	 * @param template
	 * @param timeout
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static <T extends AsyncMessage> List<T> scatter(GigaSpace space,T message,T template, long timeout) throws InterruptedException, ExecutionException{
		DistTask<T> task=new DistTask<T>(message,template,timeout);
		AsyncFuture<List<T>> future = space.execute(task);
		return future.get();
	}
	
	public static <T extends AsyncMessage> List<T> scatter(GigaSpace space,Scatterable<T> task)throws Exception{
		DistTask2<T> disttask=new DistTask2<T>(task);
		AsyncFuture<List<T>> future = space.execute(disttask);
		return future.get();
	}
	
	//Like Callable with a space arg
	public static interface Scatterable<T>{
		T call(GigaSpace space)throws Exception;
	}
}


class DistTask<T extends AsyncMessage> implements DistributedTask<T, List<T>> {
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


class DistTask2<T extends AsyncMessage> implements DistributedTask<T, List<T>> {
	@TaskGigaSpace
	private transient GigaSpace space;
	private Scatterable<T> callable;
	
	public DistTask2(){}
	
	public DistTask2(Scatterable<T> callable){
		this.callable=callable;
	}
	/**
	 * Write command/message into local space and wait for response
	 */
	public T execute() throws Exception {
		return callable.call(space);
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


