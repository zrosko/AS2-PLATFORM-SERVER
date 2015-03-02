package hr.as2.inf.server.connections.cassandra;

import hr.as2.inf.common.exceptions.AS2ConnectionException;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.connections.J2EEConnection;
import hr.as2.inf.server.connections.J2EEConnectionManager;

/**
 * zrosko@gmail.com
 */
public final class AS2ConnectionCassandra extends J2EEConnection {
   
    public AS2ConnectionCassandra(J2EEConnectionManager aConnectionManager, String host, String user, String password) throws AS2Exception {
        _connectionManager = aConnectionManager;
        _HOST = host;
        _USER = user;
        _PASSWORD = password;
        connect();
    }
    public void commit() throws AS2ConnectionException {
        return;
    }
    public void connect() throws AS2ConnectionException {
        try {
            // --------------------------------------------
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, this, e.toString());
            AS2ConnectionException ex = new AS2ConnectionException("289");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public void disconnect() throws AS2ConnectionException {
        try {
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, this, e.toString());
            AS2ConnectionException exc = new AS2ConnectionException("282");
            exc.addCauseException(e);
            throw exc;
        }
    }

    public boolean ping() {
        //String ping = ""; 
       	return true;
    }
    public void rollback() throws AS2ConnectionException {
    }
    public String toString() {
        return "";
    }
 
}