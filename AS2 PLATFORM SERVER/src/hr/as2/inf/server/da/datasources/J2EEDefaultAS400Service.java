package hr.as2.inf.server.da.datasources;

import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.server.session.AS2SessionFactory;

public class J2EEDefaultAS400Service extends J2EEPersistentService {
	static private J2EEDefaultAS400Service _instance = null;
	public static final String AS400 = "AS400";
public J2EEDefaultAS400Service() {
	super();
}
public  void addToCache(String boFinder, AS2RecordList rs)throws AS2Exception{}
protected String getConnectionManagerType() {
	return AS400;
}
protected String getNameForFindingConnection() throws Exception {
    return AS2SessionFactory.getInstance().getCurrentSession().getSessionId(); //TODO test
}
public static J2EEDefaultAS400Service getInstance(){
	if(_instance==null)
		_instance = new J2EEDefaultAS400Service();
	return _instance;
}
}
