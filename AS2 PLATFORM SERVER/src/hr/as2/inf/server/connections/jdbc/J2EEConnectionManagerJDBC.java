package hr.as2.inf.server.connections.jdbc;

//import com.ibm.ejs.sm.beans.*;
import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.common.exceptions.AS2ConnectionException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.connections.J2EEConnection;
import hr.as2.inf.server.connections.J2EEConnectionManager;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
/**
 * The J2EEConnectionManagerJDBC pre-allocates a pool of JDBC connections.
 *
 * @version 1.0 
 * @author 	Zdravko Rosko
 */
public final class J2EEConnectionManagerJDBC extends J2EEConnectionManager {
	public static String _class = "hr.adriacomsoftware.inf.server.connection.jdbc.J2EEConnectionManagerJDBC";
	/**
	 * JDBC URL Subprotocol.
	 */
	private String _DBUSED = null;
	/**
	 * JDBC Database name.
	 */
	private String _DBNAME = null;
	/**
	 * JDBC Database schema name.
	 */
	private String _SCHEMA = null;
	/**
	 * JDBC Stored Procedures schema name.
	 */
	private String _STORED_PROCEDURE_SCHEMA = null;
	/**
	 * JDBC SQL Generator (Wilfredo DeCastro's) Stored Procedure name.
	 */
	private String _SQLGEN_STORED_PROCEDURE_NAME = null;
	/**
	 * JDBC Database name.
	 */
	//private String _DBNAME = null;
	/**
	 * JDBC Driver.
	 */
	private String _DRIVER = null;
	/**
	 * JDBC URL.
	 */
	private String _DBURL = null;
	/**
	 * JDBC maximum rows(optional) retrived from data base.
	 */
	private int _MAXROWS;
	
	Context _ctx;
	java.util.Hashtable _parms;
/**
 * Initialize from properties. Call base class "initialize".
 */
public J2EEConnectionManagerJDBC(Properties p) throws AS2ConnectionException {
	try {
		_DBUSED = p.getProperty(_class + ".DBUSED", "sql"); 
		_DBNAME = p.getProperty(_class + ".DBNAME", "test");
		_SCHEMA = p.getProperty(_class + ".SCHEMA", "dbo");
		_STORED_PROCEDURE_SCHEMA = p.getProperty(_class + ".STORED_PROCEDURE_SCHEMA", "dbo");
		_SQLGEN_STORED_PROCEDURE_NAME = p.getProperty(_class + ".SQLGEN_STORED_PROCEDURE_NAME", "dbo");
		_DRIVER = p.getProperty(_class + ".DRIVER", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		//COM.ibm.db2.jdbc.app.DB2Driver
		//com.microsoft.jdbc.sqlserver.SQLServerDriver
		_DBURL = p.getProperty(_class + ".DBURL", "jdbc:" + _DBUSED + ":" + _DBNAME);
		_MAXROWS = AS2Helper.getIntProperty(p, _class + ".MAXROWS", 0);
		Class.forName(_DRIVER).newInstance();
		
		AS2Trace.trace(AS2Trace.W, "Using DBURL: " + _DBURL);
		AS2Trace.trace(AS2Trace.W, "Using DBNAME: " + _DBNAME);
		
		super.initialize(p);
		
		if (_APS_POOL) {
			_parms = new java.util.Hashtable();
			_parms.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.ejs.ns.jndi.CNInitialContextFactory");
			_parms.put(Context.PROVIDER_URL, "iiop://" + _HOST + ":900/");
			_ctx = new InitialContext(_parms);
		}
	} catch (Exception e) {
		AS2Trace.trace(1, e.toString());
		AS2ConnectionException ex = new AS2ConnectionException("264");
		ex.addCauseException(e);
		ex.setHost(_HOST);
		ex.setPassword(_PASSWORD);
		ex.setUser(_USER);
		throw ex;
	}
}
/**
 * Create JDBC data source connection.
 */
protected J2EEConnection createConnection() throws AS2ConnectionException {

    J2EEConnectionJDBC conn = null;
    Exception e = null;

    for (int i = 0; i <= _connectRetry; i++) {
        try {
            conn = 
                new J2EEConnectionJDBC(
                    this, 
                    _DBUSED, 
                    _DBNAME,
                    _SCHEMA, 
                    _STORED_PROCEDURE_SCHEMA, 
                    _DRIVER, 
                    _DBURL, 
                    _MAXROWS, 
                    _USER, 
                    _PASSWORD); 
            conn.setSqlGenStoredProcedureName(_SQLGEN_STORED_PROCEDURE_NAME);
            return conn;
        } catch (Exception ex) {
            e = ex;
            AS2Trace.trace(
                AS2Trace.W, 
                "Can not create connection - now retry # "
                    + (i + 1)
                    + " reason: "
                    + ex.toString()); 
            try {
                Thread.sleep(CONNECT_RETRY_SLEEP);
            } catch (Exception sleepExc) {
                AS2Trace.trace(AS2Trace.W, "Problem while sleeping on connect retry");
            }
        }
    }

    if (e != null || conn == null) {
        AS2Trace.trace(
            AS2Trace.E, 
            "Can not create connection at all - reason: " + e.toString()); 
        AS2ConnectionException ex = new AS2ConnectionException("265");
        ex.addCauseException(e);
        ex.setHost(_HOST);
        ex.setPassword(_PASSWORD);
        ex.setUser(_USER);
        throw ex;
    }

    return conn;
}
public J2EEConnection getConnectionAPS() throws AS2ConnectionException {
	return getConnectionByNameAPS(_APS_POOL_NAME);
}
public J2EEConnection getConnectionByNameAPS(String name)
    throws AS2ConnectionException {
    J2EEConnectionJDBC conn = null;
//    try {
//        DataSource ds = (DataSource) _ctx.lookup(name);
//        java.sql.Connection apConn = ds.getConnection(_USER, _PASSWORD);
//        conn = 
//            new J2EEConnectionJDBC(
//                this, 
//                apConn, 
//                _DBUSED,
//    			  _DBNAME,
//                _SCHEMA, 
//                _STORED_PROCEDURE_SCHEMA, 
//                _DRIVER, 
//                _DBURL, 
//                _MAXROWS, 
//                _USER, 
//                _PASSWORD); 
//        conn.setSqlGenStoredProcedureName(_SQLGEN_STORED_PROCEDURE_NAME);
//    } catch (Exception e) {
//        J2EEConnectionException ex = new J2EEConnectionException("253");
//        ex.addCauseException(e);
//        ex.setHost(_HOST);
//        ex.setPassword(_PASSWORD);
//        ex.setUser(_USER);
//        throw ex;
//    }
    return conn;
}
/**
 * Get the DB Schema indicator.
 */
public String getDbSchema() {
	 return _SCHEMA;
}
/**
 * Get the maximum row number.
 */
public int getMaxRows() {
	 return _MAXROWS;
}
/**
 * Test.
 */
public static void main(String args[]) {
	AS2Trace.turnMethodTracingOn();
	AS2Trace.turnTracingOn();
	AS2Trace.setTraceLevel(6);
	AS2Trace.trace(AS2Trace.I, "main", "start");
	java.util.Properties p;
	
	p = AS2Helper.readPropertyFile("c:\\bsa\\settings\\hr.banksoft.inf.server.services.J2EEefaultJDBCService.properties");
	try {
		//p = new java.util.Properties();
		J2EEConnectionManagerJDBC cm = new J2EEConnectionManagerJDBC(p);
		
		J2EEConnectionJDBC co = (J2EEConnectionJDBC)cm.getConnection();
		co.ping();
		//ResultSet rs = co.getDBMetaData("ECP%");

		//while(rs.next()){
			//String table_name = rs.getString("TABLE_NAME");
			//System.out.println("T.name="+table_name); 
			//java.util.Vector in = co.getPrimaryKeys("test");
			//System.out.println(in);
			//while(in.next()){
				//System.out.println("Index Name:"+in.getString("INDEX_NAME"));
				//System.out.println("ColumnName:"+in.getString("COLUMN_NAME"));	
			//}
			//in.close();
			
		//}
		//rs.close();
		
		
		
	} catch (Exception e) {
		System.out.println(e);
	}
	AS2Trace.trace(AS2Trace.I, "main", "end");
}
public void returnConnectionToPoolAPS(J2EEConnection conn) {
	J2EEConnectionJDBC aConn = (J2EEConnectionJDBC)conn;
	try {
		aConn.disconnect();
	} catch (Exception e) {
		// Could not drop connection
		AS2Trace.trace(AS2Trace.W, "Could not drop connectionn for APS");
	}
	aConn._connection = null;
	conn = null;
}
}
