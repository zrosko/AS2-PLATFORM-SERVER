package hr.as2.inf.server.connections.as400;

import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.common.exceptions.AS2ConnectionException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.connections.J2EEConnection;
import hr.as2.inf.server.connections.J2EEConnectionManager;

import java.util.Properties;

import com.ibm.as400.access.AS400;

/**
 * This class is a AS400 Connection Manager.
 */
public final class AS2ConnectionManagerAS400 extends J2EEConnectionManager
{
	public static String _class = "hr.as2.inf.server.connection.as400.AS2ConnectionManagerAS400";

	private int _SERVICETYPE;

	private String _DATAQIN;

	private String _DATAQOUT;

	/*
	 * Initialize from properties. Call base class "initialize".
	 */
	public AS2ConnectionManagerAS400(Properties p) throws AS2ConnectionException {
		String bsDeviceId = "TODO";
				//BSAApplicationContext.getInstance().getDeviceID();

		try {
			_SERVICETYPE = AS2Helper.getIntProperty(p, _class + ".SERVICETYPE", AS400.DATAQUEUE);
			_DATAQIN = p.getProperty(_class + ".DATAQIN", "/QSYS.LIB/BSA1.LIB/" + bsDeviceId
					+ "I.DTAQ");
			_DATAQOUT = p.getProperty(_class + ".DATAQOUT", "/QSYS.LIB/BSA1.LIB/" + bsDeviceId
					+ "O.DTAQ");
			String pgmThreadSafe = p.getProperty("com.ibm.as400.access.ProgramCall.threadSafe",
					"false");
			String cmdThreadSafe = p.getProperty("com.ibm.as400.access.CommandCall.threadSafe",
					"false");
			Properties systemProperties = System.getProperties();
			systemProperties.put("com.ibm.as400.access.ProgramCall.threadSafe", pgmThreadSafe);
			systemProperties.put("com.ibm.as400.access.CommandCall.threadSafe", cmdThreadSafe);
			System.setProperties(systemProperties);

			super.initialize(p);

		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, this, e.toString());
			AS2ConnectionException ex = new AS2ConnectionException("266");
			ex.addCauseException(e);
			throw ex;
		}
	}

	/**
	 * Create AS400 data source connection.
	 */
	protected J2EEConnection createConnection() throws AS2ConnectionException {
		AS2ConnectionAS400 conn = null;

		try	{
			conn = new AS2ConnectionAS400(this, _HOST, _USER, _PASSWORD, _SERVICETYPE, _DATAQIN,
					_DATAQOUT);

		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, this, e.toString());
			AS2ConnectionException ex = new AS2ConnectionException("288");
			ex.addCauseException(e);
			throw ex;
		}
		return conn;
	}

	public J2EEConnection getConnectionAPS() throws AS2ConnectionException	{
		return getConnectionByNameAPS(_APS_POOL_NAME);
	}

	public J2EEConnection getConnectionByNameAPS(String name) throws AS2ConnectionException	{
		//TODO ovdje dodati connection pool for as400
		return null;
	}

	public void returnConnectionToPoolAPS(J2EEConnection conn)	{
	}
}
