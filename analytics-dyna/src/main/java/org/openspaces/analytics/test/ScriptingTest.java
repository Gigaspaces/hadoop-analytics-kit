package org.openspaces.analytics.test;

import groovy.util.GroovyScriptEngine;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


// Assess impact of calling scripts many times from event handler
// vs patching a batch
//
// Groovy is the clear performance winner.

public class ScriptingTest {
	private static final int ITERATIONS=10000;

	public static void main(String[] args)throws Exception{
		//GROOVY
		
		System.out.println("GROOVY");
		ScriptEngine engine=new ScriptEngineManager().getEngineByName("groovy");
		CompiledScript script=((Compilable)engine).compile("int cum;"+ITERATIONS+".each(){ cum=it;} ");
		long now=System.currentTimeMillis();
		script.eval();
		System.out.println(System.currentTimeMillis()-now);
		
		script=((Compilable)engine).compile("int cum; cum=3;");
		now=System.currentTimeMillis();
		for(int i=0;i<ITERATIONS;i++){
			script.eval();
		}
		System.out.println(System.currentTimeMillis()-now);
		
		System.out.println("JAVASCRIPT");
		engine=new ScriptEngineManager().getEngineByName("javascript");
		script=((Compilable)engine).compile("var cum;for(var it=0;it<"+ITERATIONS+";it++){ cum=it;} ");
		now=System.currentTimeMillis();
		script.eval();
		System.out.println(System.currentTimeMillis()-now);
		
		script=((Compilable)engine).compile("var cum; cum=3;");
		now=System.currentTimeMillis();
		for(int i=0;i<ITERATIONS;i++){
			script.eval();
		}
		System.out.println(System.currentTimeMillis()-now);
		
		/**
		 * Discovered clojure-jsr223 doesn't really support Compilable
		 */
		System.out.println("CLOJURE");
		engine=new ScriptEngineManager().getEngineByName("Clojure");
		//script=((Compilable)enfgine).compile("var cum;for(var it=0;it<"+ITERATIONS+";it++){ cum=it;} ");
		//script=((Compilable)engine).compile("(let [cum 0] (dotimes "+ITERATIONS+" (inc cum))");
		now=System.currentTimeMillis();
		//script.eval();
		engine.eval("(let [cum 0] (dotimes [n "+ITERATIONS+"] (inc cum)))");
		System.out.println(System.currentTimeMillis()-now);
		
		//script=((Compilable)engine).compile("(let [cum 3] ())");
		now=System.currentTimeMillis();
		for(int i=0;i<ITERATIONS;i++){
			engine.eval("(let [cum 3] ())");
			//script.eval();
		}
		System.out.println(System.currentTimeMillis()-now);
		
	}
}
