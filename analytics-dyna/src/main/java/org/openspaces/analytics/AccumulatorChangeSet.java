package org.openspaces.analytics;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.gigaspaces.client.ChangeSet;

/**
 * An ChangeSet implementation with the following features<b>
 * <ul>
 * <li>Has a method to retrieve the number of changes applied
 * <li>Accumulates multiple changes internally.
 * <li>Provides a method to summarize the internally tracked changes
 * <li>Is targeted specifically at the Accumulator class (adds "values" to path)
 * </ul>
 * 
 * This class is accessed directly from event handlers/accumulator containers
 * 
 * @author DeWayne
 */
public class AccumulatorChangeSet extends ChangeSet {
	private static final Logger log=Logger.getLogger(AccumulatorChangeSet.class.getName());
	private int changeCount=0;
	final Map<String,Integer> netints=new HashMap<String,Integer>();
	final Map<String,Double> netdoubles=new HashMap<String,Double>();
	final Map<String,Byte> netbytes=new HashMap<String,Byte>();
	final Map<String,Float> netfloats=new HashMap<String,Float>();
	final Map<String,Long> netlongs=new HashMap<String,Long>();
	final Map<String,Short> netshorts=new HashMap<String,Short>();
	
	@Override
	public ChangeSet addAllToCollection(String path,
			Collection<? extends Serializable> newItems) {
		changeCount++;
		return super.addAllToCollection(path, newItems);
	}

	@Override
	public ChangeSet addAllToCollection(String path, Serializable... newItems) {
		changeCount++;
		return super.addAllToCollection(path, newItems);
	}

	@Override
	public ChangeSet addToCollection(String path, Serializable newItem) {
		changeCount++;
		return super.addToCollection(path, newItem);
	}

	@Override
	public ChangeSet decrement(String item, byte delta) {
		changeCount++;
		Byte val=netbytes.get("values."+item);
		if(val==null){
			netbytes.put("values."+item,(byte)-delta);
		}
		else{
			byte nativeval=val;
			netbytes.put("values."+item, (byte)(nativeval-delta));
		}
		return this;
	}

	@Override
	public ChangeSet decrement(String item, double delta) {
		changeCount++;
		Double val=netdoubles.get("values."+item);
		if(val==null){
			netdoubles.put("values."+item,-delta);
		}
		else{
			double nativeval=val;
			netdoubles.put("values."+item, nativeval-delta);
		}
		return this;
	}

	@Override
	public ChangeSet decrement(String item, float delta) {
		changeCount++;
		Float val=netfloats.get("values."+item);
		if(val==null){
			netfloats.put("values."+item,-delta);
		}
		else{
			float nativeval=val;
			netfloats.put("values."+item, nativeval-delta);
		}
		return this;
	}

	@Override
	public ChangeSet decrement(String item, int delta) {
		changeCount++;
		Integer val=netints.get("values."+item);
		if(val==null){
			netints.put("values."+item,-delta);
		}
		else{
			int nativeval=val;
			netints.put("values."+item, nativeval-delta);
		}
		return this;
	}

	@Override
	public ChangeSet decrement(String item, long delta) {
		changeCount++;
		Long val=netlongs.get("values."+item);
		if(val==null){
			netlongs.put("values."+item,-delta);
		}
		else{
			long nativeval=val;
			netlongs.put("values."+item, nativeval-delta);
		}
		return this;
	}

	@Override
	public ChangeSet decrement(String item, short delta) {
		changeCount++;
		Short val=netshorts.get("values."+item);
		if(val==null){
			netshorts.put("values."+item,(short)(-delta));
		}
		else{
			short nativeval=val;
			netshorts.put("values."+item, (short)(nativeval-delta));
		}
		return this;
	}

	@Override
	public ChangeSet increment(String item, byte delta) {
		changeCount++;
		Byte val=netbytes.get("values."+item);
		if(val==null){
			netbytes.put("values."+item,delta);
		}
		else{
			long nativeval=val;
			netbytes.put("values."+item, (byte)(nativeval+delta));
		}
		return this;
	}

	@Override
	public ChangeSet increment(String item, double delta) {
		changeCount++;
		Double val=netdoubles.get("values."+item);
		if(val==null){
			netdoubles.put("values."+item,delta);
		}
		else{
			double nativeval=val;
			netdoubles.put("values."+item, nativeval+delta);
		}
		return this;
	}

	@Override
	public ChangeSet increment(String item, float delta) {
		changeCount++;
		Float val=netfloats.get("values."+item);
		if(val==null){
			netfloats.put("values."+item,delta);
		}
		else{
			float nativeval=val;
			netfloats.put("values."+item, nativeval+delta);
		}
		return this;
	}

	@Override
	public ChangeSet increment(String item, int delta) {
		changeCount++;
		Integer val=netints.get("values."+item);
		if(val==null){
			netints.put("values."+item,delta);
		}
		else{
			int nativeval=val;
			netints.put("values."+item, nativeval+delta);
		}
		return this;
	}

	@Override
	public ChangeSet increment(String item, long delta) {
		changeCount++;
		Long val=netlongs.get("values."+item);
		if(val==null){
			netlongs.put("values."+item,delta);
		}
		else{
			long nativeval=val;
			netlongs.put("values."+item, nativeval+delta);
		}
		return this;
	}

	@Override
	public ChangeSet increment(String item, short delta) {
		changeCount++;
		Short val=netshorts.get("values."+item);
		if(val==null){
			netshorts.put("values."+item,delta);
		}
		else{
			Short nativeval=val;
			netshorts.put("values."+item, (short)(nativeval+delta));
		}
		return this;
	}

	/**
	 * Count of changes in this set
	 * 
	 * @return
	 */
	public int getChangeCount() {
		return changeCount;
	}

	/**
	 * Processed cached increments and decrements
	 */
	public void summarize(){
		for(Map.Entry<String,Integer> entry:netints.entrySet())super.increment(entry.getKey(),entry.getValue());
		for(Map.Entry<String,Double> entry:netdoubles.entrySet())super.increment(entry.getKey(),entry.getValue());
		for(Map.Entry<String,Byte> entry:netbytes.entrySet())super.increment(entry.getKey(),entry.getValue());
		for(Map.Entry<String,Float> entry:netfloats.entrySet())super.increment(entry.getKey(),entry.getValue());
		for(Map.Entry<String,Long> entry:netlongs.entrySet())super.increment(entry.getKey(),entry.getValue());
		for(Map.Entry<String,Short> entry:netshorts.entrySet())super.increment(entry.getKey(),entry.getValue());
	}
}
