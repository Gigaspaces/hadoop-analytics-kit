package com.gigaspaces.analytics.rest;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.SimpleNotifyContainerConfigurer;
import org.openspaces.events.notify.SimpleNotifyEventListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.gigaspaces.annotation.pojo.SpaceClass;

/**
 * Handles requests for the application home page.
 */
@Controller
public class RestController {
	private static final Logger log = Logger.getLogger(RestController.class.getName());
	SimpleNotifyEventListenerContainer notifyEventListenerContainer;
	ScriptEngine engine;
	private String mapperLanguage="groovy"; //groovy as default
	EventMapper mapper;
	CompiledScript script;
	@Autowired ServletContext context;
	GigaSpace space;
	
	public RestController(){
		log.info("constructor");
	}
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView home(Locale locale, Model model) {
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		ModelAndView mv=new ModelAndView("ItemView");
		mv.addObject(new Item(space==null?"null":space.toString()));
		return mv;
	}
	
	@RequestMapping(value="/eventMapper", method= RequestMethod.GET)
	public ModelAndView getEventMapper(){
		ModelAndView mv=new ModelAndView("ItemView");
		mv.addObject(mapper);
		return mv;
	}

	/**
	 * Write/update the event mapper.  Expects json body with a two entries
	 * map: "language" and "code" for keys.  
	 * 
	 * Example:
	 * {"language":"groovy","code","collector.add(\"type1\",\"val1\",\"val2\")"}
	 * 
	 * 
	 * @param body
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/eventMapper", method= RequestMethod.POST)
	@ResponseBody
	public ModelAndView postEventMapper(@RequestBody String body, HttpServletResponse response){
		ObjectMapper om=new ObjectMapper();
		Map<?,?> json=null;
		try {
			json=om.readValue(body, Map.class);
		} catch (Exception e){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			ModelAndView mv=new ModelAndView("ItemView");
			mv.addObject(e);
			return(mv);
		}
		String language=(String)json.get("language");
		String code=(String)json.get("code");
		if(language==null || code==null){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			ModelAndView mv=new ModelAndView("ItemView");
			mv.addObject("null code or language");
			return(mv);
		}
		EventMapper ev=new EventMapper();
		ev.setCode(code);
		ev.setLanguage(language);
		space.write(ev);
		ModelAndView mv=new ModelAndView("ItemView");
		return mv;
	}
	
	@RequestMapping(value="/events",method=RequestMethod.PUT)
	@ResponseBody
	public String addEvents(@RequestBody String body, HttpServletResponse response){
		
		if(script==null){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return "no handler script defined";
		}
		try {
			engine.getBindings(ScriptContext.GLOBAL_SCOPE).put("payload",body);
			OutputCollector collector=new OutputCollector();
			engine.getBindings(ScriptContext.GLOBAL_SCOPE).put("collector",collector);
			script.eval();
			log.info("collector returned cnt="+collector.getVals().size());
			if(collector.getVals().size()==0)return "";
			List<Item> objs=new ArrayList<Item>();
			for(Map<String,List<String>> val:collector.getVals()){
				for(Map.Entry<String,List<String>> entry:val.entrySet()){
					Item obj=new Item(entry.getKey());
					obj.fields=new ArrayList<String>();
					obj.fields.addAll(entry.getValue());
					objs.add(obj);
				}
			}
			space.writeMultiple(objs.toArray());
			
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		ModelAndView mv=new ModelAndView("ItemView");
		mv.addObject(new Item(space==null?"null":space.toString()));
		return "";
	}
	
	@PostConstruct
	public void postConstruct(){
		log.info("in post construct");
		//set up event listener so dynamic rest handler can be loaded
		space=(GigaSpace)context.getAttribute("gigaSpace");
		
		notifyEventListenerContainer = new SimpleNotifyContainerConfigurer(space)
        .template(new EventMapper())
        .eventListenerAnnotation(new Object() {
            @SpaceDataEvent
            public void eventHappened(EventMapper event) {
            	log.info("eventHappened");
            	if(!mapperLanguage.equals(event.getLanguage())){
            		log.info("invalid language:"+event.getLanguage());
            		throw new RuntimeException("invalid language:"+event.getLanguage());
            	}
            	if(event.getCode()==null || event.getCode().length()==0){
            		log.info("null code supplied");
            		throw new RuntimeException("null code supplied");
            	}
            	mapper=event;
           		try {
           			log.info("loading script, lang:"+mapper.getLanguage());
					script=((Compilable)engine).compile(mapper.getCode());
					log.info("compile complete");
				} catch (ScriptException e) {
					throw new RuntimeException(e);
				}
            }
        }).notifyContainer();		
		
		engine=new ScriptEngineManager().getEngineByName(mapperLanguage);
		if(!(engine instanceof Compilable))throw new RuntimeException("language "+mapperLanguage+" not compilable");
	}
	
	public String getHandlerLanguage() {
		return mapperLanguage;
	}

	public void setHandlerLanguage(String mapperLanguage) {
		this.mapperLanguage = mapperLanguage;
	}
	
	public static void main(String[] args)throws Exception{
		ObjectMapper om=new ObjectMapper();
		Map<?,?> m=om.readValue("{\"key\":[\"value\"]}", Map.class);
		System.out.println(m);
		System.out.println(m.get("key").getClass().getName());
		if(true)System.exit(0);
		
		UrlSpaceConfigurer us=new UrlSpaceConfigurer("jini://*/*/space");
		GigaSpace space=new GigaSpaceConfigurer(us.space()).gigaSpace();
		EventMapper e=new EventMapper();
		e.setLanguage("groovy");
		e.setCode("collector.add(\"blorf\",\"a\",\"b\")");
		space.write(e);
		us.destroy();
		
		/*ScriptEngine e=new ScriptEngineManager().getEngineByName("groovy");
		Bindings b=e.getBindings(ScriptContext.GLOBAL_SCOPE);
		b.put("payload", "blorf");
		CompiledScript script=((Compilable)e).compile("[\"a\":payload]");
		Object obj=script.eval();
		log.info(obj);*/
	}

	@SpaceClass
	public static class Item{
		private String name;
		private List<String> fields;
		private Boolean processed;
		
		public Item(){}
		
		public Item(String name){
			this.name=name;
			fields=new ArrayList<String>();
			processed=false;
		}

		public List<String> getFields() {
			return fields;
		}
		public void setFields(List<String> fields) {
			this.fields = fields;
		}
		public Boolean getProcessed() {
			return processed;
		}
		public void setProcessed(Boolean processed) {
			this.processed = processed;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
