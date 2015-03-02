package hr.as2.inf.server.facade;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.server.annotations.AS2FacadeServer;


/**
 * Pattern: Layer Supertype
 * To be inherited by all server side Facade classes.
 */
@AS2FacadeServer
public abstract class AS2FacadeServerLayer {
	private Long _id;
	private String resource_path = null;
	protected AS2FacadeServerLayer() {
		this._id = System.currentTimeMillis();
	}
	public AS2FacadeServerLayer(Long value) {
		this._id = value;
	}
	public Long getId() {
		return this._id;
	}
	public void setId(Long value) {
		this._id = value;
	}
	//TODO annotation
	public void checkServiceSecurity(AS2Record value) throws Exception {
	}
	protected String getReseurcesPath(AS2Record vo){
		if(resource_path == null)
			resource_path=vo.get("war_path")+java.io.File.separator+"module"+java.io.File.separator+"resources"+java.io.File.separator+"reports";
		return resource_path;
	}
}
