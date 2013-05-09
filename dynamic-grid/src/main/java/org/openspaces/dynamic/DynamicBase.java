package org.openspaces.dynamic;

import java.util.logging.Logger;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;

/**
 * Base class for beans supplying dynamic behavior.  Supports
 * dynamic code introduction/updating and compilation.  Doesn't
 * presume a particular language, but assumes it can be compiled.
 * 
 * Command of the behavior occurs via message passing in the space using
 * the CodeDef class
 * 
 * @author DeWayne
 *
 */
public abstract class DynamicBase {
	protected static Logger log=Logger.getLogger(DynamicBase.class.getName());
	protected GigaSpace space;
	protected ScriptEngine engine;
	protected CompiledScript script;
	protected String name;
	protected CodeDef codeDef;
	protected SimplePollingEventListenerContainer codeUpdateContainer;

	protected DynamicBase(GigaSpace space, String name, String defaultLang, String defaultScript){
		this.space=space;
		this.name=name;
		if(defaultLang!=null && defaultScript!=null){
			codeDef=new CodeDef(name,defaultScript,defaultLang);
			compile();
		}
	}

	/**
	 * Implementer notified of code arrival here prior to compilation
	 * 
	 * @param script 
	 */
	protected abstract void onPreCompile();

	/**
	 * Implementer notified of compilation complete
	 */
	protected abstract void onPostCompile();

	protected void start(){

		codeUpdateContainer = new SimplePollingContainerConfigurer(space)
		.template(new CodeDef(name,null,null))
		.eventListenerAnnotation(new Object() {

			@SpaceDataEvent
			public void gotCodeDef(CodeDef event) {

				log.info("got code def:"+event);
				
				log.info("code def received");
				if(event.getCode()==null || event.getCode().length()==0){
					log.severe("null code supplied");
					throw new RuntimeException("null code supplied");
				}
				if(event.getLanguage()==null || event.getLanguage().length()==0){
					log.severe("null language supplied");
					throw new RuntimeException("null language supplied");
				}
				
				codeDef=event;
				
				compile();
				
			}
		}).pollingContainer();
		codeUpdateContainer.start();
		
	}
	
	protected void stop(){
		if(codeUpdateContainer!=null)codeUpdateContainer.stop();
	}

	protected void destroy() {
		if(codeUpdateContainer!=null)codeUpdateContainer.destroy();
	}
	
	/**
	 * Allow subclasses to modify code prior to compile.  Use case:
	 * user supplies event handler code, and subclass wishes to wrap
	 * it in a loop
	 * 
	 * @param code
	 * @return
	 */
	protected String preCompile(String code) {
		return code;
	}
	
	protected void compile(){
		onPreCompile();
		
		engine=new ScriptEngineManager().getEngineByName(codeDef.getLanguage());
		if(!(engine instanceof Compilable))throw new RuntimeException("language "+codeDef.getLanguage()+" not compilable");
		
		try {
			log.info("loading script, lang:"+codeDef.getLanguage());
			synchronized(this){
				//script=((Compilable)engine).compile("events.each{fields->"+codeDef.getCode()+"}");
				script=((Compilable)engine).compile(preCompile(codeDef.getCode()));
			}
			log.info("compile complete");
		} catch (ScriptException e) {
			log.severe("exception compiling script:"+e.getMessage());
			throw new RuntimeException(e);
		}
		
		onPostCompile();
	}

}
