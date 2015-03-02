package hr.as2.inf.server.session.da.memory;

import hr.as2.inf.common.session.AS2Session;
import hr.as2.inf.server.session.da.AS2SessionDao;

public class AS2SessionDaoMemory implements AS2SessionDao {
	
	public AS2SessionDaoMemory(){		
	}

	@Override
	public AS2Session daoCreate(AS2Session value) throws Exception {
		return AS2SessionCache.getInstance().create(value);
	}

	@Override
	public void daoRemove(AS2Session value) throws Exception {
		AS2SessionCache.getInstance().remove(value);
	}

	@Override
	public AS2Session daoLoad(AS2Session value) throws Exception {
		return AS2SessionCache.getInstance().load(value);
	}

	@Override
	public void daoSetValue(AS2Session session, String name, String value) throws Exception {
		AS2SessionCache.getInstance().setSessionValue(session, name, value);
	}

	@Override
	public void daoRemoveValue(AS2Session session, String name) throws Exception {
		AS2SessionCache.getInstance().removeSessionValue(session, name);
	}

	@Override
	public void daoStore(AS2Session value) throws Exception {
		AS2SessionCache.getInstance().store(value);
	}
}
