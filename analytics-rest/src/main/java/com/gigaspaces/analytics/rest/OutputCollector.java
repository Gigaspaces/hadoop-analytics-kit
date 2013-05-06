package com.gigaspaces.analytics.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * Passed to user event mapper code to collect tokens produced by parsing REST PUT content.
 * 
 * @author DeWayne
 *
 */
public class OutputCollector {
	private List<Map<String,List<String>>> vals = new ArrayList<Map<String,List<String>>>();
	
	public List<Map<String,List<String>>> getVals(){
		return vals;
	}
	
	public void add(String typeName, String...items){
		if(typeName==null || typeName.length()==0)throw new RuntimeException("no typeName supplied");
		if(items==null ||items.length==0)throw new RuntimeException("no values supplied");

		Map<String,List<String>> entry=new HashMap<String,List<String>>();
		entry.put(typeName,Lists.newArrayList(items));
		vals.add(entry);
	}
	
}
