package org.openspaces.analytics.rest;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.openspaces.analytics.Accumulator;
import org.openspaces.analytics.DynaAccumulatorAgentCommand;
import org.openspaces.analytics.DynaAccumulatorAgentCommand.MessageType;
import org.openspaces.analytics.Event;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.dynamic.DynamicBase;
import org.openspaces.events.notify.SimpleNotifyEventListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;


/**
 * Implements REST API.  NOTE, WON'T WORK WITH A PARTITIONED SPACE (YET)
 */
@Controller
public class RestController extends DynamicBase{
	private static final Logger log = Logger.getLogger(RestController.class.getName());
	private ClusterInfo clusterInfo;
	private int primaryCount=0;
	private SimpleNotifyEventListenerContainer notifyEventListenerContainer;
	private String mapperLanguage="groovy"; //groovy as default
	private EventMapper mapper;
	private @Autowired ServletContext context;

	public RestController(){
		//Define default mapper
		super(null,"restController","groovy","collector.add(\"event\",payload.split())");
	}

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value="/eventMapper", method= RequestMethod.GET)
	public ModelAndView getEventMapper(HttpServletResponse response)throws Exception{
		ModelAndView mv=new ModelAndView("jsonView");
		if(mapper==null){
			throw new ResourceNotFoundException();
		}
		else{
			mv.addObject(mapper);
		}
		return mv;
	}

	/**
	 * Returns list of defined accumulators
	 * 
	 * @return
	 */
	@RequestMapping(value="/accumulators", method= RequestMethod.GET)
	public ModelAndView getAccumulators(){
		ModelAndView mv=new ModelAndView("jsonView");
		Accumulator[] accs=space.readMultiple(new Accumulator());
		if(accs!=null){
			mv.addObject(accs);
		}
		else{
			mv.addObject(new Accumulator[0]);
		}
		return mv;
	}

	/**
	 * Gets the named accumulator
	 * 
	 * @param name
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/accumulator/{name}", method= RequestMethod.GET)
	public ModelAndView getAccumulator(@PathVariable String name,HttpServletResponse response)throws Exception{
		ModelAndView mv=new ModelAndView("jsonView");
		Accumulator acc=space.readById(Accumulator.class,name);
		if(acc!=null){
			mv.addObject(acc);
		}
		else{
			throw new ResourceNotFoundException();
		}
		return mv;
	}

	/**
	 * Deletes an accumulator (both container and data)
	 * 
	 * @param name
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/accumulator/{name}",method=RequestMethod.DELETE)
	@ResponseBody
	public String deleteAccumulator(@PathVariable String name, HttpServletResponse response)throws Exception{
		executeCommand(DynaAccumulatorAgentCommand.deleteAccumulator(name));
		response.setStatus(HttpServletResponse.SC_OK);
		return String.format("accumulator %s deleted",name);
	}

	/**
	 * Creates a new accumulator
	 * 
	 * @param name
	 * @param body
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/accumulator/{name}",method=RequestMethod.POST)
	@ResponseBody
	public String createAccumulator(@PathVariable String name,@RequestBody String body,HttpServletResponse response)throws Exception{
		body=URLDecoder.decode(body,"utf-8");
		log.info("create accumulator called. body="+body);

		Accumulator acc=space.readById(Accumulator.class,name);

		if(acc!=null){
			throw new AlreadyExistsException();
		}

		ObjectMapper om=new ObjectMapper();
		DynaAccumulatorAgentCommand cmd;
		try {
			cmd=om.readValue(body,DynaAccumulatorAgentCommand.class);
			cmd.setMessageType(MessageType.REQUEST);
			cmd.setCommand("new");
			cmd.getParms().put("name",name);
			cmd.setId(UUID.randomUUID().toString());
		} catch (Exception e){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return("exception parsing request body:"+e.getMessage());
		}

		log.info("writing agent command:"+cmd);

		executeCommand(cmd);

		response.setStatus(HttpServletResponse.SC_CREATED);
		return String.format("accumulator %s created",name);
	}

	/**
	 * Write/update the event mapper.  Expects json body with two entries
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
	@RequestMapping(value="/eventMapper", method= RequestMethod.PUT)
	@ResponseBody
	public ModelAndView postEventMapper(@RequestBody String body, HttpServletResponse response){
		ObjectMapper om=new ObjectMapper();
		Map<?,?> json=null;
		try {
			json=om.readValue(body, Map.class);
		} catch (Exception e){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			ModelAndView mv=new ModelAndView("jsonView");
			mv.addObject(e);
			return(mv);
		}
		String language=(String)json.get("language");
		String code=(String)json.get("code");
		if(language==null || code==null){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			ModelAndView mv=new ModelAndView("jsonView");
			mv.addObject("null code or language");
			return(mv);
		}
		EventMapper ev=new EventMapper();
		ev.setCode(code);
		ev.setLanguage(language);
		space.write(ev);
		ModelAndView mv=new ModelAndView("jsonView");
		response.setStatus(HttpServletResponse.SC_CREATED);
		return mv;
	}

	/**
	 * This where events are inserted into the engine.  The POST body
	 * is the source of data, and the user supplied EventMapper is
	 * responsible for transforming the body into one or more events,
	 * using the supplied OutputCollector.
	 * 
	 * @param body
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/events",method=RequestMethod.POST)
	@ResponseBody
	public String addEvents(@RequestBody String body, HttpServletResponse response)throws Exception{
		body=URLDecoder.decode(body,"utf-8");

		if(script==null){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "no handler script defined";
		}
		try {
			engine.getBindings(ScriptContext.GLOBAL_SCOPE).put("payload",body);
			OutputCollector collector=new OutputCollector();
			engine.getBindings(ScriptContext.GLOBAL_SCOPE).put("collector",collector);
			script.eval();
			log.info("collector returned cnt="+collector.getVals().size());
			if(collector.getVals().size()==0)return "";
			List<Event> objs=new ArrayList<Event>();
			for(Map<String,List<String>> val:collector.getVals()){
				for(Map.Entry<String,List<String>> entry:val.entrySet()){
					Event obj=new Event(entry.getKey());
					obj.setFields(new ArrayList<String>());
					obj.getFields().addAll(entry.getValue());
					objs.add(obj);
				}
			}
			space.writeMultiple(objs.toArray());

		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		ModelAndView mv=new ModelAndView("jsonView");
		mv.addObject(new Event(space==null?"null":space.toString()));
		response.setStatus(HttpServletResponse.SC_CREATED);
		return "";
	}

	@PostConstruct
	public void postConstruct(){
		log.info("in post construct");

		clusterInfo=(ClusterInfo)context.getAttribute("clusterInfo");
		space=(GigaSpace)context.getAttribute("gigaSpace");
		primaryCount=clusterInfo.getNumberOfInstances()/(clusterInfo.getNumberOfBackups()+1);

	}

	@PreDestroy
	private void preDestroy(){
		if(notifyEventListenerContainer!=null)notifyEventListenerContainer.destroy();
	}

	public String getHandlerLanguage() {
		return mapperLanguage;
	}

	public void setHandlerLanguage(String mapperLanguage) {
		this.mapperLanguage = mapperLanguage;
	}

	private List<DynaAccumulatorAgentCommand> executeCommand(DynaAccumulatorAgentCommand cmd)throws Exception{
		DynaAccumulatorAgentCommand template=new DynaAccumulatorAgentCommand();
		template.setId(cmd.getId());
		template.setMessageType(MessageType.RESPONSE);
		
		log.info("writing cmd="+cmd);

		List<DynaAccumulatorAgentCommand> responses=Scatterer.scatter(space,cmd,template,5000L);

		if(responses.size()<primaryCount){
			throw new PartialResponseException(String.format("only %d of %d agents responded",responses.size(),primaryCount));
		}
		for(DynaAccumulatorAgentCommand resp:responses){
			if(resp.getSuccess()!=true){
				throw new CommandFailedException();
			}
		}
		return responses;
	}

	@Override
	protected void onPreCompile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onPostCompile() {
		// TODO Auto-generated method stub
		
	}

}

/////////////////////////
// EXCEPTIONS
/////////////////////////

@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.NOT_FOUND)
class ResourceNotFoundException extends Exception{
	public ResourceNotFoundException(){}
	public ResourceNotFoundException(String message){
		super(message);
	}
}
@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.CONFLICT)
class AlreadyExistsException extends Exception{
	public AlreadyExistsException(){}
	public AlreadyExistsException(String message){
		super(message);
	}
}
@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
class PartialResponseException extends Exception{
	public PartialResponseException(){}
	public PartialResponseException(String message){
		super(message);
	}
}
@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
class CommandFailedException extends Exception{
	public CommandFailedException(){}
	public CommandFailedException(String message){
		super(message);
	}
}
