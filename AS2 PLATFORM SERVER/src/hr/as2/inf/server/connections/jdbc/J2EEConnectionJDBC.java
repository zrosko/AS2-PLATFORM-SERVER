package hr.as2.inf.server.connections.jdbc;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2ConnectionException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.types.AS2Date;
import hr.as2.inf.server.connections.J2EEConnection;
import hr.as2.inf.server.connections.J2EEConnectionManager;
import hr.as2.inf.server.da.jdbc.AS2ResultSetUtilityJdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

public final class J2EEConnectionJDBC extends J2EEConnection {
	protected Connection _connection;
	protected CallableStatement _callablestatement;
	protected PreparedStatement _preparedstatement;
	private String _DBUSED = null;
	private String _SCHEMA = null;
	private String _STORED_PROCEDURE_SCHEMA = null;

	/**
	 * JDBC SQL Generator (Wilfredo DeCastro's) Stored Procedure name.
	 */
	private String _SQLGEN_STORED_PROCEDURE_NAME = null;
	private String _DBNAME = null;
	private String _DRIVER = null;
	private String _DBURL = null;
	private int _MAXROWS = 0;
	protected LinkedHashMap<String, Object> _metaDataCache = new LinkedHashMap<String, Object>(100);

	public J2EEConnectionJDBC(J2EEConnectionManager connectionManager, String dbUsed,
			String dbName, String dbSchema, String spSchema, String driver,
			String dbUrl, int maxRows, String user, String password)
			throws AS2ConnectionException {
		_connectionManager = connectionManager;
		_DBUSED = dbUsed;
		_DBNAME = dbName;
		_SCHEMA = dbSchema;
		_STORED_PROCEDURE_SCHEMA = spSchema;
		_DRIVER = driver;
		_DBURL = dbUrl;
		_MAXROWS = maxRows;
		_USER = user;
		_PASSWORD = password;

		if (AS2Context.getInstance().EXTERNAL_TXN_COMMIT_IND) {
			AS2ConnectionException e = new AS2ConnectionException("264");
			throw e;
		}
		connect();
	}

	public J2EEConnectionJDBC(J2EEConnectionManager connectionManager,
			java.sql.Connection conn, String dbUsed, String dbName,
			String dbSchema, String spSchema, String driver, String dbUrl,
			int maxRows, String user, String password)
			throws AS2ConnectionException {
		_connectionManager = connectionManager;
		_DBUSED = dbUsed;
		_DBNAME = dbName;
		_SCHEMA = dbSchema;
		_STORED_PROCEDURE_SCHEMA = spSchema;
		_DRIVER = driver;
		_DBURL = dbUrl;
		_MAXROWS = maxRows;
		_USER = user;
		_PASSWORD = password;
		_connection = conn;
		_APS_POOL = true;

		/*
		 * Use autocommit just for default, but not for JTA and EJB application
		 * controller.
		 */
		if (AS2Context.getInstance().EXTERNAL_TXN_COMMIT_IND) {
			_AUTO_COMMIT_IND = false;
		}

		if (conn == null) {
			AS2ConnectionException ex = new AS2ConnectionException("224");
			ex.setHost(_HOST);
			ex.setUser(_USER);
			throw ex;
		} else {
			try { // here we have to avoid setting autocommit for JTA or EJB
					// application controller
				if (_AUTO_COMMIT_IND)
					_connection.setAutoCommit(_autoCommit);
				// mConnection.setTransactionIsolation(mTxnIsolationLevel);
			} catch (Exception e) {
				AS2ConnectionException ex = new AS2ConnectionException("261");
				ex.addCauseException(e);
				ex.setHost(_HOST);
				ex.setPassword(_PASSWORD);
				ex.setUser(_USER);
				throw ex;
			}
		}
	}

	public void commit() throws AS2ConnectionException {
		try {
			if (_connection != null){
				//in case dao did not close them
				try {
					if(_callablestatement !=null)
						_callablestatement.close();
					if(_preparedstatement !=null)
						_preparedstatement.close();
				} finally {
					_connection.commit();
				}
			}
		} catch (Exception e) {
			AS2ConnectionException ex = new AS2ConnectionException("260");
			ex.addCauseException(e);
			ex.setHost(_HOST);
			ex.setPassword(_PASSWORD);
			ex.setUser(_USER);
			throw ex;
		}
	}

	public void connect() throws AS2ConnectionException {

		try {
			/*
			 * SQLServer problem retrieving prmary key meta data FIX: This error
			 * occurs when you try to execute multiple statements against a SQL
			 * Server database with the JDBC driver while in manual transaction
			 * mode (AutoCommit=false) and while using the direct
			 * (SelectMethod=direct) mode. Direct mode is the default mode for
			 * the driver. When you use manual transaction mode, you must set
			 * the SelectMethod property of the driver to Cursor, or make sure
			 * that you use only one active statement on each connection as
			 * specified in the "More Information" section of this article.
			 * http:
			 * //support.microsoft.com/default.aspx?scid=kb;en-us;313181#kb3
			 */
			if (_DBUSED.equalsIgnoreCase("sql")) // sql 2005 is not the same
				_DBURL = _DBURL + ";SelectMethod=Cursor;";
			_connection = DriverManager.getConnection(_DBURL, _USER, _PASSWORD);
			_connection.setAutoCommit(_autoCommit);
			// _onnection.setTransactionIsolation(?);

			if (_connection.getWarnings() != null) {
				SQLWarning warn = _connection.getWarnings();

				while (warn != null) {
					AS2Trace.trace(AS2Trace.I, this, "*WARNING* SQL State ="
							+ warn.getSQLState());
					AS2Trace.trace(AS2Trace.I, this, "*WARNING* Message   ="
							+ warn.getMessage());
					AS2Trace.trace(AS2Trace.I, this, "*WARNING* Vendor ="
							+ warn.getErrorCode());
					warn = warn.getNextWarning();
				}
			}
			// use database from the settings
			if (_DBUSED.startsWith("sql")) {
				//if(_DBUSED.equalsIgnoreCase("sql")) {//$NON-NLS-1$
				Statement stmt = _connection.createStatement();
				stmt.execute("use " + _DBNAME);
				stmt.close();
			}
		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, this, e.toString());
			AS2ConnectionException ex = new AS2ConnectionException("261");
			ex.addCauseException(e);
			ex.setHost(_HOST);
			ex.setPassword(_PASSWORD);
			ex.setUser(_USER);
			throw ex;
		}
	}

	public void disconnect() throws AS2ConnectionException {

		try {
			if (_connection != null) {
				_connection.close();
			}
		} catch (SQLException exc) {
			/*
			 * If a transaction is in process, first rollback then disconnect
			 */
			if (exc.getSQLState().equals("25000")) {
				AS2Trace.trace(AS2Trace.W, exc.toString());
				try {
					rollback();
					_connection.close();
				} catch (Exception e) {
					AS2Trace.trace(AS2Trace.E, e.toString());
					AS2ConnectionException ex = new AS2ConnectionException(
							"262");
					ex.addCauseException(e);
					ex.setHost(_HOST);
					ex.setPassword(_PASSWORD);
					ex.setUser(_USER);
					throw ex;
				}
			}
		}
		_connection = null;
		_valid = false;
	}

	public int isColumnSizeValid(LinkedHashMap<String, Object> row, String value) {
		// could be needed to extra check INT, DOUBLE, etc.
		int valid = 0;
		if (getColumnSize(row) < value.length())
			return getColumnSize(row);
		return valid;
	}

	public int getColumnSize(LinkedHashMap<String, Object> row) {
		int ct = -1;
		try {
			ct = Integer.valueOf(row.get("column_size").toString()).intValue();
		} catch (Exception e) {
			ct = -1;
		}
		return ct;
	}

	public int getColumnDataType(LinkedHashMap<String, Object> row) {
		int ct = -1;
		try {
			ct = Integer.valueOf(row.get("data_type").toString()).intValue();
		} catch (Exception e) {
			ct = -1;
		}
		return ct;
	}

	public boolean isIdentityColumn(LinkedHashMap<String, Object> row) {
		boolean identity = false;
		try {
			String type_name = row.get("type_name").toString();
			identity = type_name.endsWith("identity");
		} catch (Exception e) {
			identity = false;
		}
		return identity;
	}

	public Object getColumnDefaultValue(LinkedHashMap<String, Object> row) {
		Object o = null;
		// try {
		// o = row.get("column_def");
		// } catch (Exception e) {
		// o = null;
		// }
		if (o == null) {
			try {
				int columnSqlType = getColumnDataType(row);
				switch (columnSqlType) {
				case Types.BIGINT:
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.INTEGER:
				case Types.NUMERIC:
				case Types.REAL:
				case Types.SMALLINT:
				case Types.TINYINT:
				case Types.DATE:
				case Types.TIMESTAMP:
				default:
					return "";
				}
			} catch (Exception ex) {
				return "";
			}
		} else
			return o;
	}

	public boolean getColumnIsNullable(LinkedHashMap<String, Object> row) {
		boolean isNullable = false;
		try {
			String s = row.get("is_nullable").toString();
			if (s.equalsIgnoreCase("no"))
				isNullable = false;
			else
				isNullable = true;
		} catch (Exception e) {
			isNullable = false;
		}
		return isNullable;
	}

	public String getColumnName(LinkedHashMap<String, Object> row) {
		String cn = null;
		try {
			cn = row.get("column_name").toString();
		} catch (Exception e1) {
			cn = null;
		}
		return cn;
	}

	public ArrayList<String> getColumns(String table) {
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append("getColumns");
		cacheKey.append(_SCHEMA);
		cacheKey.append(table);
		cacheKey.append(AS2Date.getCurrentDate());
		Object o = _metaDataCache.get(cacheKey.toString());
		if ((o != null) && (o instanceof ArrayList))
			return (ArrayList) o;

		ArrayList<String> columns = new ArrayList<String>();
		try {
			AS2RecordList columnsInfo = getColumnsInfo(table);
			if (columnsInfo != null) {
				Iterator<AS2Record> it = columnsInfo.getRows().iterator();
				while (it.hasNext()) {
					AS2Record model = it.next();
					LinkedHashMap<String, Object> row = model.getProperties();
					String cn = null;
					try {
						cn = row.get("column_name").toString();
					} catch (Exception e1) {
					}
					if (cn != null) {
						columns.add(cn);
					}
				}
			}
			_metaDataCache.put(cacheKey.toString(), columns);
		} catch (Exception e2) {
			AS2Trace.trace(AS2Trace.I, e2, "getColumns");
		}
		return columns;
	}

	public AS2RecordList getColumnsInfo(String table) {
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append("getColumnsInfo");
		cacheKey.append(_SCHEMA);
		cacheKey.append(table);
		cacheKey.append(AS2Date.getCurrentDate());
		Object o = _metaDataCache.get(cacheKey.toString());
		if ((o != null) && (o instanceof AS2RecordList))
			return (AS2RecordList) o;

		AS2RecordList columns = null;

		try {
			DatabaseMetaData _rsmd = _connection.getMetaData();
			ResultSet rs;
			// if(_DBUSED.equalsIgnoreCase("IBM")){
			rs = _rsmd.getColumns(_DBNAME, _SCHEMA.toUpperCase(), table, null);
			// }else{//SQL, FIREBIRD?
			// rs = md.getColumns(null, null,table, null);
			// }
			columns = AS2ResultSetUtilityJdbc.transformResultSet(rs);
			_metaDataCache.put(cacheKey.toString(), columns);
			rs.close();
		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.I, e, "getColumnsInfo");
		}
		return columns;
	}

	public LinkedHashMap<String, Object> getColumnsInHashtable(String table) {
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append("getColumnsInHashtable");
		cacheKey.append(table);
		cacheKey.append(AS2Date.getCurrentDate());
		Object o = _metaDataCache.get(cacheKey.toString());
		if ((o != null) && (o instanceof LinkedHashMap))
			return (LinkedHashMap) o;

		LinkedHashMap<String, Object> columns = new LinkedHashMap<String, Object>();

		try {
			AS2RecordList columnsInfo = getColumnsInfo(table);
			if (columnsInfo != null) {
				ArrayList<AS2Record> v = columnsInfo.getRows();
				for (AS2Record e : v) {
					LinkedHashMap<String, Object> row = e.getProperties();
					String cn = null;
					try {
						cn = row.get("column_name").toString();
					} catch (Exception e1) {
						System.out.println(e1);
					}
					if (cn != null) {
						columns.put(cn, row);
					}
				}
			}
			_metaDataCache.put(cacheKey.toString(), columns);
		} catch (Exception e2) {
			AS2Trace.trace(AS2Trace.I, e2, "getColumns");
		}
		return columns;
	}

	/**
	 * Holds column names for identity columns.
	 */
	public LinkedHashMap<String, Object> getIdentiyColumns(String table) {
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append("getIdentiyColumns");
		cacheKey.append(table);
		cacheKey.append(AS2Date.getCurrentDate());
		Object o = _metaDataCache.get(cacheKey.toString());
		if ((o != null) && (o instanceof LinkedHashMap))
			return (LinkedHashMap) o;

		LinkedHashMap<String, Object> identityColumns = new LinkedHashMap<String, Object>();

		try {
			AS2RecordList columnsInfo = getColumnsInfo(table);
			if (columnsInfo != null) {
				ArrayList<AS2Record> v = columnsInfo.getRows();
				for (AS2Record e : v) {
					LinkedHashMap<String, Object> row = e.getProperties();
					if (isIdentityColumn(row)) {
						String cn = null;
						cn = row.get("column_name").toString();
						if (cn != null) {
							identityColumns.put(cn, row);
						}
					}
				}
			}
			_metaDataCache.put(cacheKey.toString(), identityColumns);
		} catch (Exception e2) {
			AS2Trace.trace(AS2Trace.I, e2, "getIdentiyColumns");
		}
		return identityColumns;
	}

	public String getDbSchema() {
		if (_SCHEMA.equalsIgnoreCase("") || _SCHEMA.equalsIgnoreCase(" "))
			return "";
		else
			return _SCHEMA + ".";
	}

	public String getDbSchemaName() {
		return _SCHEMA;
	}

	public Connection getJdbcConnection() {
		return _connection;
	}
	public PreparedStatement getPreparedStatement(String sql) throws Exception {
		_preparedstatement = _connection.prepareStatement(sql);
		return _preparedstatement;
	}
	public CallableStatement getCallableStatement(String sql) throws Exception {
		_callablestatement = _connection.prepareCall(sql);
		return _callablestatement;
	}
	public int getMaxRows() {
		return _MAXROWS;
	}

	public void setMaxRows(int value) {
		_MAXROWS = value;
	}

	public ArrayList<String> getPrimaryKeys(String table) {
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append("getPrimaryKeys");
		cacheKey.append(_SCHEMA);
		cacheKey.append(table);
		cacheKey.append(AS2Date.getCurrentDate());
		Object o = _metaDataCache.get(cacheKey.toString());
		if ((o != null) && (o instanceof Vector))
			return (ArrayList) o;

		ArrayList<String> keys = new ArrayList<String>();

		try {
			AS2RecordList keysInfo = getPrimaryKeysInfo(table);
			if (keysInfo != null) {
				ArrayList<AS2Record> v = keysInfo.getRows();
				for (AS2Record e : v) {
					AS2Record vo = e;
					String cn = null;
					try {
						cn = vo.get("column_name").toString();
					} catch (Exception e1) {
						System.out.println(e1);
					}
					if (cn != null) {
						keys.add(cn);
					}
				}
			}
			_metaDataCache.put(cacheKey.toString(), keys);
		} catch (Exception e2) {
			AS2Trace.trace(AS2Trace.I, e2, "getPrimaryKeys");
		}
		return keys;
	}

	public AS2RecordList getPrimaryKeysInfo(String table) {
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append("getPrimaryKeysInfo");
		cacheKey.append(_SCHEMA);
		cacheKey.append(table);
		cacheKey.append(AS2Date.getCurrentDate());
		Object o = _metaDataCache.get(cacheKey.toString());
		if ((o != null) && (o instanceof AS2RecordList))
			return (AS2RecordList) o;

		AS2RecordList keys = null;

		try {
			DatabaseMetaData _md = _connection.getMetaData();
			// //
			ResultSet rs;
			// if(_DBUSED.equalsIgnoreCase("db2")){
			// rs = md.getPrimaryKeys(null, _SCHEMA.toUpperCase(), table);
			// }else{//SQL, FIREBIRD?
			rs = _md.getPrimaryKeys(_DBNAME, _SCHEMA.toUpperCase(), table);
			// catalog je prvi parametar (ime baze)
			// }
			// //
			// ResultSet rs = md.getPrimaryKeys(null, _SCHEMA.toUpperCase(),
			// table);
			keys = AS2ResultSetUtilityJdbc.transformResultSet(rs);
			_metaDataCache.put(cacheKey.toString(), keys);
			rs.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return keys;
	}

	public LinkedHashMap<String, Object> getPrimaryKeysInHashtable(String table) {
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append("getPrimaryKeysInHashtable");
		cacheKey.append(table);
		cacheKey.append(AS2Date.getCurrentDate());
		Object o = _metaDataCache.get(cacheKey.toString());
		if ((o != null) && (o instanceof LinkedHashMap))
			return (LinkedHashMap) o;

		LinkedHashMap<String, Object> keys = new LinkedHashMap<String, Object>();

		try {
			AS2RecordList keysInfo = getPrimaryKeysInfo(table);
			if (keysInfo != null) {
				ArrayList<AS2Record> v = keysInfo.getRows();
				for (AS2Record vo : v) {
					LinkedHashMap<String, Object> row = vo.getProperties();
					String cn = null;
					try {
						cn = row.get("column_name").toString();
					} catch (Exception e) {
						System.out.println(e);
					}
					if (cn != null) {
						keys.put(cn, row);
					}
				}
			}
			_metaDataCache.put(cacheKey.toString(), keys);
		} catch (Exception e2) {
			AS2Trace.trace(AS2Trace.I, e2, "getPrimaryKeysInHashtable");
		}
		return keys;
	}

	public String getSqlGenStoredProcedureName() {
		return _SQLGEN_STORED_PROCEDURE_NAME;
	}

	public String getStoredProcedureSchema() {
		return " " + _STORED_PROCEDURE_SCHEMA + ".";
	}

	public String getStoredProcedureSchemaName() {
		return _STORED_PROCEDURE_SCHEMA;
	}

	/**
	 * Get meta data for passed table name. Not sure if there is better way of
	 * getting the meta data. ( ? DatabseMetaData class or ?).
	 */
	public LinkedHashMap<String, Object> getTableMetaData(String aTableName) {

		try {
			LinkedHashMap<String, Object> md = new LinkedHashMap<String, Object>();
			String sql;
			// if(_DBUSED.equalsIgnoreCase("IBM")){
			sql = "select * from " + _SCHEMA + "." + aTableName;
			// }else{//SQL, FIREBIRD?
			// sql = "select * from " + aTableName;
			// }
			PreparedStatement pstmt = _connection.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int numcols = rsmd.getColumnCount();
			for (int i = 1; i <= numcols; i++) {
				md.put(rsmd.getColumnLabel(i).toLowerCase(), rsmd
						.getColumnTypeName(i).toUpperCase());
			}
			rs.close();
			return md;
		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, e.toString());
		}
		return new LinkedHashMap<String, Object>();
	}

	/**
	 * Ping the data source. Just to make the connection not time out.
	 */
	public boolean ping() {
		String ping = _connectionManager.getPingCommand();

		if (ping == null)
			return true;

		if (_connection != null) {
			try {
				Statement stmt = _connection.createStatement();
				AS2Trace.trace(AS2Trace.I, "Ping command = " + ping);
				stmt.execute(ping);
				_valid = true;
			} catch (Exception e) {
				_valid = false;
				AS2Trace.trace(AS2Trace.W, e, "Ping failed.");
			}
		}
		return _valid;
	}

	public void rollback() throws AS2ConnectionException {
		try {
			//in case dao did not close them
			try {
				if(_callablestatement !=null)
					_callablestatement.close();
				if(_preparedstatement !=null)
					_preparedstatement.close();
			} finally {
				_connection.rollback();
			}
		} catch (Exception e) {
			AS2ConnectionException ex = new AS2ConnectionException("263");
			ex.addCauseException(e);
			ex.setHost(_HOST);
			ex.setPassword(_PASSWORD);
			ex.setUser(_USER);
			throw ex;
		}
	}

	public void setAutoCommit(boolean value) {
		try {
			if (_AUTO_COMMIT_IND) {
				super.setAutoCommit(value);
				_connection.setAutoCommit(value);
			}
		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, e.toString());
		}
	}

	public void setSqlGenStoredProcedureName(String value) {
		_SQLGEN_STORED_PROCEDURE_NAME = value;
	}

	public void setTransactionIsolation(int value) {
		try {
			super.setTransactionIsolation(value);
			_connection.setTransactionIsolation(_txnIsolationLevel);
		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, e.toString());
		}
	}
	public void setDBUSED(String value){
		_DBUSED = value;
	}
	public void setDBNAME(String value){
		_DBNAME = value;
	}
	public void setSCHEMA(String value){
		_SCHEMA = value;
	}
	public String getDbName() {
		if (_DBNAME.equalsIgnoreCase("") || _DBNAME.equalsIgnoreCase(" "))
			return "";
		else
			return " " + _DBNAME + ".";
	}
	public String toString() {
		return (super.toString() + "\n Connection	 	" + _connection
				+ "\n DBUSED		" + _DBUSED + "\n DBNAME			" + _DBNAME
				+ "\n SCHEMA  	 	" + _SCHEMA + "\n DRIVER 			" + _DRIVER
				+ "\n DBURL			" + _DBURL + "\n");
	}
}
