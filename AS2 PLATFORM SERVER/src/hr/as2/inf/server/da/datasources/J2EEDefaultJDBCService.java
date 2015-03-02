package hr.as2.inf.server.da.datasources;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2Exception;

public class J2EEDefaultJDBCService extends J2EEPersistentService {
	static private J2EEDefaultJDBCService _instance = null;
public J2EEDefaultJDBCService() {
	super();
	AS2Context.setSingletonReference(this);
}
public void addToCache(String boFinder, AS2RecordList rs) throws AS2Exception {
    if(!rs.isValueLineHandlerUsed())
        _OLD_CACHE.put(boFinder, rs);
}
protected String getConnectionManagerType() {
	return "JDBC";
}
public static J2EEDefaultJDBCService getInstance(){
	if(_instance==null)
		_instance = new J2EEDefaultJDBCService();
	return _instance;
}
}
