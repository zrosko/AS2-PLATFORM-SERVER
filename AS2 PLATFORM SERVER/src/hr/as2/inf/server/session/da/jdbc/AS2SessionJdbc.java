package hr.as2.inf.server.session.da.jdbc;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.session.AS2Session;
import hr.as2.inf.server.session.da.AS2SessionDao;
//
public class AS2SessionJdbc implements AS2SessionDao {

	@Override
	public AS2Session daoCreate(AS2Session value) throws Exception {
		return null;
	}

	@Override
	public void daoRemove(AS2Session value) throws Exception {
	}

	@Override
	public void daoStore(AS2Session value) throws Exception {
	}

	@Override
	public AS2Session daoLoad(AS2Session value) throws Exception {
		return null;
	}

	@Override
	public void daoSetValue(AS2Session session, String name, String value)
			throws Exception {
		AS2Record vo = new AS2Record();
		vo.set("session_id", session.getSessionId());
		vo.set("name", name);
		vo.set("value", value);
		new AS2SessionValueJdbc().daoCreate(vo);
		
	}

	@Override
	public void daoRemoveValue(AS2Session session, String name) throws Exception {
		AS2Record vo = new AS2Record();
		vo.set("session_id", session.getSessionId());
		vo.set("name", name);
		//TODO find key for session value
		new AS2SessionValueJdbc().daoRemove(vo);
		
	}

}
