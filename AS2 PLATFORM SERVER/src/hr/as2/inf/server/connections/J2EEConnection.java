package hr.as2.inf.server.connections;

import hr.as2.inf.common.exceptions.AS2ConnectionException;
import hr.as2.inf.common.logging.AS2Trace;
/**
 * The J2EEConnection defines the base data source connection class.
 * J2EEConnection is base class for JDBC, CICS, AS400 and MQ connection class.
 * In the future AS/400, OS/390 connection classes may inherit
 * from J2EEConnection. The J2EETransaction uses connections to
 * handle the logical unit of work(LUW) accross multiple type of
 * data sources. LUW accross different data sources (JDBC,MQ,CICS,AS400) 
 * can be done using a common conneciton class such as J2EEConnection.
 *
 * @version 1.0 
 * @author 	Zdravko Rosko
 */
 /*
 The Adapter pattern is typically used to allow the reuse of a class that is similar, but not the
same, as the class the client class would like to see. Typically the original class is capable of
supporting the behavior the client class needs, but does not have the interface the client class
expects, and it is not possible or practical to alter the original class. Perhaps the source code is
not available, or it is used elsewhere and changing the interface is inappropriate.
*/
public abstract class J2EEConnection {
	/**
	 * The connection manager which created the connection.
	 * Used for returning the connection back to the pool.
	 */
	public J2EEConnectionManager _connectionManager;
	/**
	 * User Id to access a data source (JDBC, MQ, CICS...).
	 */
	protected String _USER;
	/**
	 * Password to access a data source (JDBC, MQ, CICS...).
	 */
	protected String _PASSWORD;
	/**
	 * IP or host name of the data source being accessed.
	 */
	protected String _HOST;
	/**
	 * Connection name (optional). Used if needed to retrieve
	 * a connection by name.
	 */
	protected String _NAME="";
	/**
	 * Connection session name (optional). Used if needed to retrieve
	 * a connection by name of the session (in BSA case it we use device id).
	 */
	protected String _SESSION_NAME="";
	/**
	 * Port number on data source server.
	 */
	protected int _PORT;
	/**
	 * Auto commit indicator for a data source.
	 * By default it is set to "true". If J2EEConnection is
	 * being added to the transaction then 
	 * J2EEConnectionTransactionService set the mAutoCommit 
	 * to "false". When connection is returned back to the
	 * connection pool it is set back to "true" again. 
	 */
	protected boolean _autoCommit = true;
  	/**
  	 * Indicates if APS pool is used.
  	 */
  	protected boolean _APS_POOL = false;
  	/** 
  	 * Autocommit mode indicates whether to use "setAutocommit"
  	 * for the connection. If the controler is JTA or EJB then
  	 * we can't set autocommit mode in global transaction.
  	 */
  	protected boolean _AUTO_COMMIT_IND = true; 
	/**
	 * Ddata source transaction isolation level (not used yet).
	 */
	protected int _txnIsolationLevel = 0;
	protected boolean _inUse = false;
	protected boolean _valid = true;
	protected long _lastTimeUsed = 0;
/**
 * Commit the logical unit of work to the data source.
 */
public abstract void commit() throws AS2ConnectionException;
/**
 * Connect to the data source.
 */
public abstract void connect() throws AS2ConnectionException;
/**
 * Disconnect from the data source.
 */
public abstract void disconnect() throws AS2ConnectionException;
/**
 * Disconnect from the data source when J2EEConnection is
 * "garbage collected".
 */
public void finalize() {
	try {
		if(!_APS_POOL)
			disconnect();
	} catch (Exception e) {
		AS2Trace.trace(AS2Trace.E, e, "J2EEConnection.finalize ?");
	}
}
/**
 * Get the _autoCommit indicator.
 */
public boolean getAutoCommit() {
	return _autoCommit;
}
/**
 * Get the Connection Manager this Connection is created by.
 */
public J2EEConnectionManager getConnectionManager() {
	return _connectionManager;
}
public boolean getInUse() {
	return _inUse;
}
public long getLastTimeUsed() {
	return _lastTimeUsed;
}
/**
 * Get the connection name.
 */
public String getName() {
	return _NAME;
}
/**
 * Ping the data source. Just to make the connection not time out.
 */
public boolean ping() {
	_valid = true;
	AS2Trace.trace(AS2Trace.I, "J2EEConnection.ping not implemented. Here is the super.");
	return _valid;
}
/**
 * Rollback the data source changes.
 */
public abstract void rollback() throws AS2ConnectionException;
/**
 * Set the _autoCommit indicator.
 */
public void setAutoCommit(boolean value) {
	_autoCommit = value;
}
public void setInUse(boolean value) {
	_inUse = value;
}
public void setLastTimeUsed(long value) {
	_lastTimeUsed = value;
}
public void setName(String value) {
	_NAME = value;
}
public void setTransactionIsolation(int value) {
	_txnIsolationLevel = value;	
}
/**
 * Return the String state of the Connection.
 */
public String toString() {
	return (
		"\n USER 			 		" + _USER + 
		//"\n PASSWORD				" + _PASSWORD +
		"\n HOST  	 				" + _HOST +
		"\n NAME  	 				" + _NAME +
		"\n PORT  	 				" + _PORT +
		"\n AutoCommit				" + _autoCommit +
		"\n TxnIsolationLevel		" + _txnIsolationLevel +
		"\n");
}
}
