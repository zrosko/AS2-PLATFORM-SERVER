package hr.as2.inf.server.session.bl;

import hr.as2.inf.common.session.AS2Session;
import hr.as2.inf.server.session.AS2SessionFactory;
import hr.as2.inf.server.session.da.AS2SessionDao;
import hr.as2.inf.server.session.da.jdbc.AS2SessionJdbc;
import hr.as2.inf.server.session.da.memory.AS2SessionDaoMemory;

public class AS2SessionBo { 

	AS2SessionDao dao;
	
	public AS2SessionBo(int sessionType) {
		dao = getSessionDao(sessionType);
	}
	private AS2SessionDao getSessionDao(int sessionType){
		if(sessionType == AS2SessionFactory.AS2_SESSION_TYPE_MEMORY)
			dao = new AS2SessionDaoMemory();
		else if(sessionType == AS2SessionFactory.AS2_SESSION_TYPE_JDBC)
			dao = new AS2SessionJdbc();
		return dao;
	}

	public AS2Session create (AS2Session value) throws Exception {
		return dao.daoCreate(value);
	}

	public void invalidate (AS2Session value) throws Exception {
		dao.daoRemove(value);
	}
	
	public AS2Session load (AS2Session value) throws Exception {
		return dao.daoLoad(value);
	}

	public void setValue (AS2Session session, String name, String value) throws Exception {
		dao.daoSetValue(session, name, value);
	}
	public void removeValue (AS2Session session, String name) throws Exception {
		dao.daoRemoveValue(session, name);		
	}
}
