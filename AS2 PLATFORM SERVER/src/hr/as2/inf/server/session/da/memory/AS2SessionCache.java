package hr.as2.inf.server.session.da.memory;

import hr.as2.inf.common.session.AS2Session;

import java.util.LinkedHashMap;

public class AS2SessionCache  {
	private static AS2SessionCache instance = null;
	
	static private LinkedHashMap<String, AS2Session> data = new LinkedHashMap<String, AS2Session>();
	
	private AS2SessionCache(){		
	}
	public static AS2SessionCache getInstance(){
		if (instance == null)
			instance = new AS2SessionCache();
		return instance;
	}

	public AS2Session create(AS2Session value) throws Exception {
		data.put(value.getSessionId(), value);
		return data.get(value.getSessionId());
	}

	public void remove(AS2Session value) throws Exception {
		data.remove(value.getSessionId());
	}
	
	public void store(AS2Session value) throws Exception {
		data.put(value.getSessionId(), value);
	}

	public AS2Session load(AS2Session value) throws Exception {
		return data.get(value.getSessionId());
	}

	public void setSessionValue(AS2Session session, String name, String value) throws Exception {
		AS2Session temp = data.get(session.getSessionId());
		temp.set(name, value);
		data.put(temp.getSessionId(), temp);
	}

	public void removeSessionValue(AS2Session session, String name) throws Exception {
		AS2Session temp = data.get(session.getSessionId());
		temp.delete(name);
		data.put(temp.getSessionId(), temp);
	}
}
