package hr.as2.inf.server.connections;

import hr.as2.inf.common.logging.AS2Trace;

import java.util.Enumeration;
import java.util.Stack;
/**
 * Thread to refresh the connections in the pool. There are two scenarios:
 * 		First: If the data source connection (server we are connected to) has a 
 * time out limit, this class is used to keep the connections active while the 
 * server is running. Otherwise the server we are connected to, can close the 
 * connection to keep its resources available to other users.
 * 		Second: We would like to release some connections up to MIN number specified 
 * by connection manager to let other users get them from the server we are connecting to. 
 * 
 * The EJB specification may be heart by this but we can change this at the moment 
 * EJB is considered as the application development technology.
 *
 * @author: zrosko@yahoo.com
 */
public class J2EEConnectionWatcher implements Runnable {
	/**
	 * The connection manager keeping the connections for refresh.
	 */
	private J2EEConnectionManager _connectionManager = null;
	/**
	 * The interval to refresh the data source connection in case server closses inactive
	 * connection or cleanup needs to be done. 
	 * Also this period can be used to close some of the inactive connections
	 * if the inactive period is greated then _keepAliveTimeout;
	 */
	private long _refreshInterval = 0;
	/* The number of miliseconds the connection manager will wait for a subsequent usage 
	 * of the connection before closing the connection. Once a connection has been used, 
	 * the timeout value specified by the _keepAliveTimeout directive applies.
	 */
	protected long _keepAliveTimeout = 0;
/**
 * J2EEConnectionPing constructor comment.
 */
public J2EEConnectionWatcher(J2EEConnectionManager aConnectionManager, long aRefreshInterval, long aKeepAliveTimeout) {
	_connectionManager = aConnectionManager;
	_refreshInterval = aRefreshInterval;
	_keepAliveTimeout = aKeepAliveTimeout;
}
/**
 * Ping the connections from the pool.
 * Shrink the number of connections to the MIN_CONNECTIONS.
 */
public void run() {
    
	while (true) {
        
	    try {
            Thread.sleep(_refreshInterval);

            AS2Trace.trace(
                AS2Trace.I,
                "Refresh connections for " + _connectionManager.getClass().getName());

            Stack aPool = _connectionManager.getPool();
            
            if(_connectionManager._pingInterval > 0){
            	Enumeration E = aPool.elements();
            	while (E.hasMoreElements()) {
                	J2EEConnection conn = (J2EEConnection) E.nextElement();
                	conn.ping(); // ping
            	}
            }

            if (_keepAliveTimeout > 0) {
	            
                synchronized (aPool) {//synchronized (aPool) not sure will cause problem
                	int poolSize = aPool.size();

                    for(int i = 1; i <= poolSize; i++) {
                        J2EEConnection conn = (J2EEConnection) aPool.pop();
                        long currentTime = System.currentTimeMillis();
                        long lastUsedTime = conn.getLastTimeUsed();
                        //timeout expired, close the connections up to MIN
                        if ((currentTime - lastUsedTime) > _keepAliveTimeout) {
                            int minConnections = _connectionManager._min;
                            int currentPoolSize = aPool.size()+1; //+1 for holding connection up here
                            if (currentPoolSize > minConnections) {
                                conn.disconnect();
                                _connectionManager._cur--;
                                conn=null;
                            } else {
                                aPool.push(conn);
                                //notifyAll(); throws exceptions
                            }
                        } else {
	                    	aPool.push(conn);
                        }
                    }
                }
            } 
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.W, e, "Problem to Ping or Refresh the connections in the pool.");
            try {
                Thread.sleep(_refreshInterval);
            } catch (Exception ex) {
                AS2Trace.trace(
                    AS2Trace.W,
                    ex,
                    "Thread ? problem while Ping or Refersh the connections.");
            }

        }
    }//while
}
}
