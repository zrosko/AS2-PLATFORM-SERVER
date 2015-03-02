package hr.as2.inf.server.da.cassandra;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.connections.as400.AS2ConnectionAS400;
import hr.as2.inf.server.da.J2EEDataAccessObject;
import hr.as2.inf.server.da.datasources.J2EEDefaultAS400Service;

/**
 * @author: zrosko@yahoo.com
 */
public class AS2DataAccessObjectCassandra extends J2EEDataAccessObject
		implements AS2Constants {

	public AS2ConnectionAS400 getConnection() {
		try {
			return (AS2ConnectionAS400) J2EEDefaultAS400Service.getInstance()
					.getConnection();
		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, e);
		}
		return null;
	}

}