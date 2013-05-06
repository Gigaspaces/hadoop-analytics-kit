package com.gigaspaces.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.DynamicEventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.ReceiveHandler;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;

import com.gigaspaces.query.IdQuery;

/**
 * This is the service that executes analytics defined by dynamic code.  Also
 * listens for code introduction events (AccumulatorDef) 
 * 
 * @author DeWayne
 *
 */
public class DynaAccumulatorContainer  {
	private static final Logger log=Logger.getLogger(DynaAccumulatorContainer.class.getName());
	private SimplePollingEventListenerContainer pollingListenerContainer;
	AccumulatorDef accdef;
	private ScriptEngine engine;
	private String language="groovy"; //groovy as default
	private CompiledScript script;
	private Object template;
	private GigaSpace space;
	private String name;

	public DynaAccumulatorContainer(){}

	public DynaAccumulatorContainer(GigaSpace space,String name){
		log.info("starting service: "+name);
		this.space=space;
		this.name=name;
		this.template=new Event(name);
		postConstruct();
	}

	public DynaAccumulatorContainer(GigaSpace space,String name,Object template){
		log.info("starting service: "+name);
		this.space=space;
		this.name=name;
		this.template=template;
		postConstruct();
	}

	@ReceiveHandler
	ReceiveOperationHandler opHandler(){
		MultiTakeReceiveOperationHandler h=new MultiTakeReceiveOperationHandler();
		h.setMaxEntries(10);
		return h;
	}

	@DynamicEventTemplate
	Object unprocessedData() {
		log.fine("template fetched");
		return template;
	}

	@SpaceDataEvent
	public Event[] eventListener(Event[] events) {
		log.info("events received");

		List<List<String>> elist=new ArrayList<List<String>>(); 
		for(Event event:events){
			elist.add(event.getFields());
		}
		try {
			if(script!=null){
				Bindings bindings=engine.createBindings();
				bindings.put("events",elist);
				AccumulatorChangeSet changes=new AccumulatorChangeSet();
				bindings.put("changes",changes);
				script.eval(bindings);
				IdQuery<Accumulator> idQuery = new IdQuery<Accumulator>(Accumulator.class, name );
				if(changes.getChangeCount()>0){
					changes.summarize();
					space.change(idQuery,changes);
				}
			}
			else{
				log.severe("null script execute attempted");
			}
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostConstruct
	public void postConstruct(){
		log.fine("in post construct");

		engine=new ScriptEngineManager().getEngineByName(language);
		if(!(engine instanceof Compilable))throw new RuntimeException("language "+language+" not compilable");

		pollingListenerContainer = new SimplePollingContainerConfigurer(space)
		.template(new AccumulatorDef())
		.eventListenerAnnotation(new Object() {

			@SpaceDataEvent
			public void gotAccumulatorDef(AccumulatorDef event) {
				log.info("accumulator def received");
				if(!language.equals(event.getLanguage())){
					log.severe("invalid language:"+event.getLanguage());
					throw new RuntimeException("invalid language:"+event.getLanguage());
				}
				if(event.getCode()==null || event.getCode().length()==0){
					log.severe("null code supplied");
					throw new RuntimeException("null code supplied");
				}
				accdef=event;
				try {
					log.info("loading script, lang:"+accdef.getLanguage());
					synchronized(this){
						script=((Compilable)engine).compile("events.each{fields->"+accdef.getCode()+"}");
					}
					log.info("compile complete");
				} catch (ScriptException e) {
					log.severe("exception compiling script:"+e.getMessage());
					throw new RuntimeException(e);
				}
				
				//Create accumulator
				Accumulator acc=new Accumulator(accdef.getName());
				space.write(acc);
			}
		}).pollingContainer();

	}

	public void destroy(){
		pollingListenerContainer.destroy();
	}
	
	// Assess impact of calling groovy multiple times as opposed to having
	// script to iteration
	public static void main(String[] args){
		
	}

}
