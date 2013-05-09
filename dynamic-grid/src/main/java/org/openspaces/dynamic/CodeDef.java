package org.openspaces.dynamic;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

/**
 * A message class for updating DynamicBase subclasses.  The
 * name field is used to target a particular receiver.
 * 
 * @author DeWayne
 *
 */
@SpaceClass
public class CodeDef {
	private String name;
	private String code;
	private String language;
	
	public CodeDef(){}
	public CodeDef(String name,String code,String language){
		this.name=name;
		this.code=code;
		this.language=language;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	@SpaceId
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String toString(){
		return String.format("{name=%s,language=%s,code=%s}",name,language,code);
	}
}
