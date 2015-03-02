package hr.as2.inf.server.session.da;

import hr.as2.inf.common.session.AS2Session;

public interface AS2SessionDao {

	public AS2Session daoCreate(AS2Session value) throws Exception;

	public void daoRemove(AS2Session value) throws Exception;

	public void daoStore(AS2Session value) throws Exception;
	
	public AS2Session daoLoad(AS2Session value) throws Exception;
	
	public void daoSetValue(AS2Session session, String name, String value) throws Exception;

	public void daoRemoveValue(AS2Session session, String name) throws Exception;

}
