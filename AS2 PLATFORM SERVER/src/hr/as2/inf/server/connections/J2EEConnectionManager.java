package hr.as2.inf.server.connections;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.common.exceptions.AS2ConnectionException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.security.encoding.AS2Base64;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Stack;

/**
 * The J2EEConnectionManager pre-allocates a pool of connections. The pool will
 * grow to a maxmum of 32 connections. It has to be subclassed by new data
 * source connection managers.
 * @version 1.0
 * @author Zdravko Rosko
 */
public abstract class J2EEConnectionManager extends Thread
{
	public static String _class = "hr.adriacomsoftware.inf.server.connection.J2EEConnectionManager";

	/**
	 * Pool of the connections to access a data source (JDBC, MQ, ...).
	 */
	protected Stack _pool = new Stack();

	/**
	 * User Id to access a data source (JDBC, MQ, ...).
	 */
	protected String _USER;

	/**
	 * Password to access a data source (JDBC, MQ, ...).
	 */
	protected String _PASSWORD;

	/**
	 * Encode password using BASE64.
	 */
	protected boolean _ENCODE_BASE64 = false;

	/**
	 * IP or host name of the data source being accessed.
	 */
	protected String _HOST;

	/**
	 * Port number on data source server.
	 */
	protected int _PORT;

	/**
	 * Maximum number of connections to be opened for the data source.
	 */
	protected int _max;

	/**
	 * Minimum number of connections to be opened for the data source.
	 */
	protected int _min;

	/**
	 * Current number of connections to be opened for the data source.
	 */
	protected int _cur;

	/**
	 * Defautl maximum number of connection to be opened. If _max is not set up
	 * in the properties file _maxConnections is used.
	 */
	protected static final int _maxConnections = 32;

	/**
	 * Default minimum number of connection to be opened. If _max is greater
	 * then _minConnections, _minConnections is used as minimum otherwise _max
	 * is used as minimum.
	 */
	protected static final int _minConnections = 1;

	/**
	 * Indicator if there are connections being used by other programs. It is
	 * used by the class to controll the connections deletion. If _pool size is
	 * less then _cur the connection manager waits to have all connections
	 * returned to the pool.
	 */
	protected boolean _shutDown = false;

	/**
	 * Indicator if the new connection can be created in case the pool is empty.
	 * If the indicator is set to "fase" the connection manager will wait for
	 * the first connection to be returned to the pool. There will be no new
	 * connection created. If the indicator is set to "true" (defaut) the new
	 * conncetion up to _max number will be created any time the pool is empty.
	 */
	protected boolean _dynamicConnectionCreation = true;

	/**
	 * By defautl this connection pool is used. If the property file setup is
	 * APS=true then Application Server (Websphere) connection pool is used. The
	 * connection type supported for time being is JDBC only. CICS and MQ pool
	 * is not available in websphere 3.0. For CICS, MQ, AS400 the this pool can
	 * be used.
	 */
	protected boolean _APS_POOL = false;

	/**
	 * APS pool name (e.g.JDBC/DB2E).
	 */
	protected String _APS_POOL_NAME;

	/**
	 * No connection pool. Use single
	 */
	protected boolean _USE_POOL = true;

	/**
	 * The interval to ping the data source connection to keep it alive.
	 */
	protected long _pingInterval = 0;

	/**
	 * Ping command ( e.g. For JDBC select * from sysibm.systables ).
	 */
	protected String _PING_COMMAND = null;

	/**
	 * The number of retry while creating connection.
	 */
	protected int _connectRetry = 0;

	/*
	 * Connect retry sleep in milliseconds.
	 */
	public static long CONNECT_RETRY_SLEEP;

	/*
	 * The number of miliseconds the connection manager will wait for a
	 * subsequent usage of the connection before closing the connection. Once a
	 * connection has been used, the timeout value specified by the
	 * _keepAliveTimeout directive applies.
	 */
	protected long _keepAliveTimeout;

	/**
	 * Create data source specific connection.
	 */
	protected abstract J2EEConnection createConnection() throws AS2ConnectionException;

	/**
	 * Create the pool of data source connections.
	 */
	protected void createPool()
	{
		if (_APS_POOL)
			return;
		int i = 0;
		try
		{
			for (; i < _min; i++)
			{
				J2EEConnection conn = createConnection();
				if (conn != null)
				{
					conn.setLastTimeUsed(System.currentTimeMillis());
					_pool.push(conn);
					AS2Trace.trace(AS2Trace.I, "Connection created. Pool Size = " + _pool.size());
				}
			}
		}
		catch (Exception e)
		{
			if (i > 0)
			{
				AS2Trace.trace(AS2Trace.E, e, "Only Pool Size = " + _pool.size()
						+ " connections available");
				_min = i;
			}
			else
			{
				AS2Trace.trace(AS2Trace.E, e, "No connections opened");
			}
		}
		_cur = _min;
	}

	/**
	 * Destroy the pool of data source connections.
	 */
	public synchronized void destroyPool() throws AS2ConnectionException
	{
		if (_APS_POOL)
			return;
		AS2Trace.trace(AS2Trace.I, "Destroy the connections pool.");
		_shutDown = true;
		while (_pool.size() < _cur)
		{
			try
			{
				AS2Trace.trace(AS2Trace.I, "Waiting to Destroy the connections pool.");
				wait(AS2Context.getInstance().TXNTIMEOUT);
			}
			catch (Exception e)
			{
				AS2ConnectionException ex = new AS2ConnectionException("251");
				ex.addCauseException(e);
				ex._host = _HOST;
				ex._password = _PASSWORD;
				ex._user = _USER;
				throw ex;
			}
		}
		try
		{
			while (!_pool.empty())
			{
				J2EEConnection conn = (J2EEConnection) _pool.pop();
				conn.disconnect();
			}
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e.toString());
			AS2ConnectionException ex = new AS2ConnectionException("251");
			ex.addCauseException(e);
			ex._host = _HOST;
			ex._password = _PASSWORD;
			ex._user = _USER;
			throw ex;
		}
	}

	/**
	 * Destroy the pool when J2EEConnectionManager is "garbage collected".
	 */
	public void finalize()
	{
		if (_APS_POOL)
			return;
		try
		{
			destroyPool();
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e, "J2EEConnectionMnager.finalize");
		}
	}

	/**
	 * Retrieve data source connection from pool or create new connection if
	 * maximun number of conneciton is not reached and
	 * _dynamicConnectionCreation is set to "true".
	 */
	public synchronized J2EEConnection getConnection() throws AS2ConnectionException
	{

		if (_APS_POOL)
			return getConnectionAPS();

		AS2Trace
				.trace(AS2Trace.I, "ASKING <<< a connection. Current POOL size = " + _pool.size());

		J2EEConnection conn = null;

		if (!_USE_POOL)
		{
			AS2Trace.trace(AS2Trace.I, "Connection asked no Pool used");
			try
			{
				conn = createConnection();
				return conn;
			}
			catch (Exception e)
			{
				AS2Trace.trace(AS2Trace.E, e, "Connection asked no Pool used");
			}
		}

		if (_shutDown)
		{
			AS2ConnectionException ex = new AS2ConnectionException("255");
			ex._host = _HOST;
			ex._password = _PASSWORD;
			ex._user = _USER;
			throw ex;
		}

		if (!_pool.empty())
		{
			conn = (J2EEConnection) _pool.pop();
		}
		else if (_cur < _max)
		{
			if (_dynamicConnectionCreation)
			{
				conn = createConnection();
				_cur++;
			}
			else
			{
				AS2Trace.trace(AS2Trace.W, "No option to create dynamic connection now Waiting.");
				//throw new J2EEConnectionException("252");
			}
		}
		else
		{
			AS2Trace.trace(AS2Trace.W, "No connections available now Waiting. Pool Size = "
					+ _pool.size());
			// throw new J2EEConnectionException("253");
		}
		if (conn == null)
		{
			while (_pool.empty())
			{
				try
				{
					AS2Trace.trace(AS2Trace.W,
							"No connections available now started Waiting. Pool Size = "
									+ _pool.size());
					wait();
				}
				catch (Exception e)
				{
					AS2ConnectionException ex = new AS2ConnectionException("253");
					ex.addCauseException(e);
					ex._host = _HOST;
					ex._password = _PASSWORD;
					ex._user = _USER;
					throw ex;
				}
				AS2Trace.trace(AS2Trace.W, "End of Waiting. Pool Size = " + _pool.size());
			}
			conn = (J2EEConnection) _pool.pop();
		}
		if (conn != null)
		{

			boolean valid = conn.ping();

			if (!valid)
			{ //ping failed create new connection

				AS2Trace.trace(AS2Trace.W, "Connection invalid creating new one: " + conn);
				//cur--;
				conn = null;
				//getConnection();
				conn = createConnection();
			}

			AS2Trace.trace(AS2Trace.I, "RESERVED <<< a connection. Current POOL size = "
					+ _pool.size());
		}
		if (conn != null)
			conn.setLastTimeUsed(System.currentTimeMillis());
		return conn;
	}

	/**
	 * Retrieve data source connection from APS pool.
	 */
	public abstract J2EEConnection getConnectionAPS() throws AS2ConnectionException;

	/**
	 * Retrieve data source connection by name from pool or create new
	 * connection if maximun number of conneciton is not reached and
	 * _dynamicConnectionCreation is set to "true".
	 */
	public synchronized J2EEConnection getConnectionBySessionId(String session)
			throws AS2ConnectionException
	{
		return getConnectionByName(session);
	}

	public synchronized J2EEConnection getConnectionByName(String name)
			throws AS2ConnectionException
	{
		if (_APS_POOL)
			return getConnectionByNameAPS(name);
		AS2Trace.trace(AS2Trace.I, "ASKING by name <<< a connection. Current POOL size = "
				+ _pool.size());
		if (_shutDown)
		{
			AS2ConnectionException ex = new AS2ConnectionException("255");
			ex._host = _HOST;
			ex._password = _PASSWORD;
			ex._user = _USER;
			throw ex;
		}
		J2EEConnection conn = null;
		if (!_pool.empty())
		{
			Enumeration E = _pool.elements();
			while (E.hasMoreElements())
			{
				conn = (J2EEConnection) E.nextElement();
				//if (conn._NAME.equals(name)) { //TODO uraditi i NAME i
				// SESSION_NAME
				if (name.equals(conn._SESSION_NAME))
				{
					_pool.removeElement(conn);
					break;
				}
			}
		}
		else if (_cur < _max)
		{
			if (_dynamicConnectionCreation)
			{
				conn = createConnection();
				//conn.setName(name);//TODO ispraviti kao gore
				conn._SESSION_NAME = name;
				_cur++;
			}
			else
			{
				AS2Trace.trace(AS2Trace.W, "No option to create dynamic connection");
				AS2ConnectionException ex = new AS2ConnectionException("252");
				ex._host = _HOST;
				ex._password = _PASSWORD;
				ex._user = _USER;
				throw ex;
			}
		}
		else
		{
			AS2Trace.trace(AS2Trace.W, "No connections available. Pool Size = " + _pool.size());
			AS2ConnectionException ex = new AS2ConnectionException("253");
			ex._host = _HOST;
			ex._password = _PASSWORD;
			ex._user = _USER;
			throw ex;
		}
		if (conn != null)
			AS2Trace.trace(AS2Trace.I, "RESERVED by name <<< a connection. Current POOL size = "
					+ _pool.size());
		conn.setLastTimeUsed(System.currentTimeMillis());
		return conn;
	}

	/**
	 * Retrieve data source connection by name from APS pool.
	 */
	public abstract J2EEConnection getConnectionByNameAPS(String name)
			throws AS2ConnectionException;

	public String getPingCommand()
	{
		if (_PING_COMMAND.equalsIgnoreCase("null"))
			return null;
		return _PING_COMMAND;
	}

	protected Stack getPool()
	{
		return _pool;
	}

	/**
	 * Sets the interval to check the connection status.
	 */
	public void initConnectionWatcher(long newPing, long newKeep)
	{
		AS2Trace.trace(AS2Trace.I, "J2EEConnectionManager.initConnectionWatcher begin.");
		long refreshInterval = 0;

		if (_APS_POOL)
			return;
		if (_USE_POOL)
		{
			_pingInterval = newPing;
			_keepAliveTimeout = newKeep;
			if (_pingInterval > 0 && _keepAliveTimeout > 0)
				refreshInterval = (_pingInterval < _keepAliveTimeout) ? _pingInterval : _keepAliveTimeout;
			else
				refreshInterval = (_pingInterval > _keepAliveTimeout) ? _pingInterval : _keepAliveTimeout;

			//refresh connections
			if (refreshInterval > 0)
			{
				J2EEConnectionWatcher aPinger = new J2EEConnectionWatcher(this, refreshInterval,
						_keepAliveTimeout);
				Thread aThread = new Thread(aPinger);
				aThread.setPriority(Thread.MIN_PRIORITY);
				aThread.start();
			}
		}
		else
		{
			AS2Trace.trace(AS2Trace.W,
					"J2EEConnectionManager.setPingInterval not set, Pool not being used.");
		}

		AS2Trace.trace(AS2Trace.I, "J2EEConnectionManager.initConnectionWatcher end.");
	}

	/**
	 * Initialize from properties. This method has to be called form subclass
	 * conection managers.
	 */
	protected void initialize(Properties p) throws AS2ConnectionException, IOException
	{
		_USER = p.getProperty(_class + ".USER", "");
		_PASSWORD = p.getProperty(_class + ".PASSWORD", "");
		String temp = p.getProperty(_class + ".ENCODE_BASE64", "FALSE");
		_ENCODE_BASE64 = new Boolean(temp).booleanValue();
		if (_ENCODE_BASE64)
			_PASSWORD = AS2Base64.decode(_PASSWORD);
		try
		{
			_HOST = p.getProperty(_class + ".HOST", InetAddress.getLocalHost().getHostAddress());
		}
		catch (Exception e)
		{
			_HOST = "127.0.0.1";
		}
		_PORT = AS2Helper.getIntProperty(p, _class + ".PORT", 8079);
		_max = AS2Helper.getIntProperty(p, _class + ".MAX_CONNECTIONS", _maxConnections);
		temp = p.getProperty(_class + ".APS_POOL", "FALSE");
		_APS_POOL = new Boolean(temp).booleanValue();
		temp = p.getProperty(_class + ".USE_POOL", "TRUE");
		_USE_POOL = new Boolean(temp).booleanValue();
		_APS_POOL_NAME = p.getProperty(_class + ".APS_POOL_NAME", "DB2G");
		_pingInterval = AS2Helper.getLongProperty(p, _class + ".PING_INTERVAL", 0);
		CONNECT_RETRY_SLEEP = AS2Helper.getIntProperty(p, _class + ".CONNECT_RETRY_SLEEP", 5000);//5 sekundi
		_connectRetry = AS2Helper.getIntProperty(p, _class + ".CONNECT_RETRY", 2); //dva dodatna pokusaja
		_PING_COMMAND = p.getProperty(_class + ".PING_COMMAND", "null");
		_keepAliveTimeout = AS2Helper.getLongProperty(p, _class + ".KEEP_ALIVE_TIMEOUT", 300000);
		_min = AS2Helper.getIntProperty(p, _class + ".MIN_CONNECTIONS", _minConnections);
		_min = (_max > _min) ? _min : _max;
		if (!_dynamicConnectionCreation)
		{
			_min = _max;
		}
		if (_USE_POOL)
		{
			//create the connection pool
			createPool();
			initConnectionWatcher(_pingInterval, _keepAliveTimeout);
		}
	}

	/**
	 * Return connection to the connection pool, so it can be used by other
	 * program execution.
	 */
	public synchronized void returnConnectionToPool(J2EEConnection conn)
	{

		if (conn != null)
		{
			conn.setAutoCommit(true);
			if (_APS_POOL)
			{
				returnConnectionToPoolAPS(conn);
			}
			else
			{
				if (!_USE_POOL)
				{
					AS2Trace.trace(AS2Trace.I, "Connection returned no Pool used");
					try
					{
						conn.disconnect();
						conn = null;
					}
					catch (Exception e)
					{
						AS2Trace.trace(AS2Trace.E, e, "Connection returned no Pool used");
					}
				}
				else
				{
					conn.setLastTimeUsed(System.currentTimeMillis());
					_pool.push(conn);
					notifyAll();
					AS2Trace.trace(AS2Trace.I,
							"+++ RELEASED >>> a connection. Current Pool Size = " + _pool.size());
				}
			}
		}
	}

	/**
	 * Return connection to the APS connection pool.
	 */
	public abstract void returnConnectionToPoolAPS(J2EEConnection conn);

	public void setAPSPoolFlag(boolean value)
	{
		_APS_POOL = value;
	}
}
