package hr.as2.inf.server.admin.facade;

import hr.as2.inf.common.admin.dto.AS2ConfigurationVo;
import hr.as2.inf.common.admin.dto.AS2ConnectionJdbcVo;
import hr.as2.inf.common.admin.dto.AS2ConnectionVo;
import hr.as2.inf.common.admin.dto.AS2EmailSetupVo;
import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.common.data.AS2MetaData;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.metrics.AS2ArmService;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionManagerJDBC;
import hr.as2.inf.server.da.datasources.J2EEDefaultJDBCService;
import hr.as2.inf.server.da.jdbc.J2EEDataAccessObjectJdbc;
import hr.as2.inf.server.exceptions.AS2ExceptionService;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;


public class AS2ServerAdministrationFacadeServer {
    private static AS2ServerAdministrationFacadeServer _instance = null;
private AS2ServerAdministrationFacadeServer() {
}
public static AS2ServerAdministrationFacadeServer getInstance() {
	if (_instance == null)
		_instance = new AS2ServerAdministrationFacadeServer();
	return _instance;
}
public AS2Record setConfiguration(AS2Record req) throws AS2Exception {
  synchronized (AS2Context.getInstance()) {
    AS2Context.getInstance().CLIENT_CACHING_IND = req.getAsBooleanOrFalse(AS2ConfigurationVo.CONFIGURATION__CLIENT_CACHING_IND);
    AS2Context.getInstance().SERVER_CACHING_IND = req.getAsBooleanOrFalse(AS2ConfigurationVo.CONFIGURATION__SERVER_CACHING_IND);
    AS2Context.getInstance().CACHE_CLEANER_REPEAT_TIME = req.getAsInt(AS2ConfigurationVo.CONFIGURATION__CACHE_CLEANER_REPEAT_TIME);
    AS2Context.getInstance().ABC_CACHE_TIME = req.getAsInt(AS2ConfigurationVo.CONFIGURATION__ONE_CACHE_TIME);
    AS2Context.getInstance().XYZ_CACHE_TIME = req.getAsInt(AS2ConfigurationVo.CONFIGURATION__TWO_CACHE_TIME);
  }
	String aTraceLevel = req.getAsStringOrBlank(AS2ConfigurationVo.CONFIGURATION__TRACE_LEVEL);
	if (aTraceLevel.equals("NONE"))
		AS2Trace.setTraceLevel(AS2Trace.N);
	else if (aTraceLevel.equals("ERROR"))
			AS2Trace.setTraceLevel(AS2Trace.E);
	else if (aTraceLevel.equals("WARNING"))
			AS2Trace.setTraceLevel(AS2Trace.W);
	else if (aTraceLevel.equals("INFO"))
			AS2Trace.setTraceLevel(AS2Trace.I);
	else if (aTraceLevel.equals("ALL"))
			AS2Trace.setTraceLevel(AS2Trace.A);
	boolean turnTraceOn = req.getAsBooleanOrFalse(AS2ConfigurationVo.CONFIGURATION__TRACE_ON);
	if (turnTraceOn)
		AS2Trace.turnTracingOn();
	else
		AS2Trace.turnTracingOff();	

   if(AS2Context.getInstance().SERVER_CACHING_IND == false) {
       J2EEDefaultJDBCService.getInstance().getCacheManager().deactivateCache();
   }
  //update u properties i upis u file
   return req;
}
public AS2RecordList getStatistics(AS2Record req) throws AS2Exception {
  AS2RecordList j2eers = new AS2RecordList();
  Hashtable ht;
  AS2Record vo = AS2ArmService.printStatistics(req);
  ht = (Hashtable)vo.getAsObject("@@statistics");  
  Enumeration E = ht.elements(); 
  while(E.hasMoreElements()){
	  LinkedHashMap<String, Object> stat_row = (LinkedHashMap<String, Object>)E.nextElement();
      AS2Record row = new AS2Record();
      row.setProperties(stat_row);
      j2eers.addRow(row);
  }
  ArrayList<String> col_names = new ArrayList<String>();
  col_names.add("service_name");
  col_names.add("service_count");
  col_names.add("business_logic_time");
  j2eers.setColumnNames(col_names);
  LinkedHashMap<String, AS2MetaData> md_ht = new LinkedHashMap<String, AS2MetaData>();
  AS2MetaData md = new AS2MetaData();
  md.setColumnLabel("service_name");
  md.setSequence(1);
  md_ht.put("service_name", md);
  md = new AS2MetaData();
  md.setColumnLabel("service_count");
  md.setSequence(2);
  md_ht.put("service_count", md);
  md = new AS2MetaData();
  md.setColumnLabel("business_logic_time");
  md.setSequence(3);
  md_ht.put("business_logic_time", md);
  j2eers.setMetaData(md_ht);
  return j2eers;
}



public AS2RecordList getExceptions(AS2Record req) throws AS2Exception {
  AS2RecordList result = new AS2RecordList();
  //uzmi messageId koji je upisan na konzoli i izdvoji sve exception-e vezane na taj broj poruke
  //najčešće se radi o samo jednom exception objectu koji će se pojaviti
  String messageId = req.getAsStringOrBlank("messageId");
  String scope = req.getAsStringOrBlank("scope");
  AS2RecordList exceptionLog = AS2ExceptionService.getInstance().getExceptionLog();

  ArrayList<AS2Record> exceptions = exceptionLog.getRows();
  for(int i=0;i<exceptions.size();i++) {
	  AS2Record _currentException = exceptions.get(i);
    String _exceptionMessageId = (String) _currentException.get("MessageId");
    if(scope.equals("All") || messageId.equals(_exceptionMessageId)) {
      result.addRow(new AS2Record(_currentException));
    }
  }
  return result;
}

private boolean writeDataToFile(Properties prop, String fileName) {
  boolean done;
  try {
      done = AS2Helper.writePropertyFile(prop, fileName);
  } catch(Exception e) {
      System.out.println("Greška pri pisanju podataka u settings file: "+fileName);
      done = false;
  }
  return done;
}

public AS2Record editDefaultJdbcConfiguration(AS2Record req) throws AS2Exception {
	try{
		boolean restartConnections = true;//req.getAsBooleanOrFalse("@@restart");
		J2EEDefaultJDBCService service = J2EEDefaultJDBCService.getInstance();
		synchronized (service) {
			/*Get new values for connections. Will not update property files at /settings directory.*/
			Set<String> E = req.keys();
			for(String name: E){
				String value = req.get(name);
				service._serverConfiguration.setProperty(name, value);
			}
      // upisivanje podataka u settings file iz kojeg podaci potjecu
      //String fileToWrite = "C:/DIS/DisServer/classes/settings/hr.ht.oss.dis.services.DefaultAdmDataSource.properties"; //uzmi ime file iz J2EEContexta
      //writeDataToFile(adm._serverConfiguration, fileToWrite);
			/* Close all old connections and open new ones.*/
			if(restartConnections){
				service.disconnectAll();
				service._CONNECTION_MANAGER = new J2EEConnectionManagerJDBC(service._serverConfiguration);
			}
		}
	}catch(Exception e){
		System.out.println("Greska kod editDefaultJdbcConfiguration");
	}
	return req;
}

public AS2ConfigurationVo getConfiguration(AS2Record  req) throws AS2Exception {
    AS2ConfigurationVo res = new AS2ConfigurationVo();
	res.set(AS2ConfigurationVo.CONFIGURATION__CACHE_CLEANER_REPEAT_TIME,AS2Context.getInstance().CACHE_CLEANER_REPEAT_TIME+"");
	res.set(AS2ConfigurationVo.CONFIGURATION__ONE_CACHE_TIME, AS2Context.getInstance().ABC_CACHE_TIME+"");
  	res.set(AS2ConfigurationVo.CONFIGURATION__TWO_CACHE_TIME, AS2Context.getInstance().XYZ_CACHE_TIME+"");

	res.set(AS2ConfigurationVo.CONFIGURATION__TRACE_ON,AS2Trace.isTraceOn()+"");
  	String aTraceLevel ="";	
	if(AS2Trace.getTraceLevel() == AS2Trace.I)
		aTraceLevel = "INFO";
	else if(AS2Trace.getTraceLevel() == AS2Trace.W)
		aTraceLevel = "WARNING";
	else if(AS2Trace.getTraceLevel() == AS2Trace.E)
		aTraceLevel = "ERROR";
	else if(AS2Trace.getTraceLevel() == AS2Trace.A)
		aTraceLevel = "ALL";
	else if(AS2Trace.getTraceLevel() == AS2Trace.N)
		aTraceLevel = "NONE";
	//log4j retrieve
	res.set(AS2ConfigurationVo.CONFIGURATION__TRACE_LEVEL, aTraceLevel);
	res.set(AS2ConfigurationVo.CONFIGURATION__SERVER_CACHING_IND,AS2Context.getInstance().SERVER_CACHING_IND+"");	
	res.set(AS2ConfigurationVo.CONFIGURATION__CLIENT_CACHING_IND,AS2Context.getInstance().CLIENT_CACHING_IND+"");	

	return res;
}
public AS2ConnectionJdbcVo getDefaultJdbcConfiguration(AS2Record req) throws AS2Exception {
    AS2ConnectionJdbcVo event = new AS2ConnectionJdbcVo();
    Properties jdbcProp = J2EEDefaultJDBCService.getInstance()._serverConfiguration;
    String key;
    Enumeration<Object> E = jdbcProp.keys();
    while (E.hasMoreElements()) {
        key = (String) E.nextElement();
        event.set(key, jdbcProp.getProperty(key));
    }
    event.setRemoteMethod("editDefaultJdbcConfiguration");
    return event;
}
public AS2RecordList getServerExceptions(AS2Record req) throws AS2Exception {
   return AS2ExceptionService.getInstance().getExceptionLog();
}
public AS2EmailSetupVo getMailProperties(AS2Record req) throws AS2Exception {
    return new AS2EmailSetupVo();
}
public AS2Record setMailProperties(AS2EmailSetupVo req) throws AS2Exception {
    System.out.println(req);
    return req;
}
public AS2ConnectionVo getDefaultAs400Configuration(AS2Record req) throws AS2Exception {
    /* 
     **/
    return null;
}
public AS2Record setDefaultJdbcConfiguration(AS2ConnectionJdbcVo req) throws AS2Exception {
	try{
	    System.out.println(req);
		boolean restartConnections = true;
		J2EEDefaultJDBCService service = J2EEDefaultJDBCService.getInstance();
		synchronized (service) {
			/*Get new values for connections. Will not update property files at /settings directory.*/
			Set<String> E = req.keys();
			for(String name: E){
				String value = req.get(name);
				service._serverConfiguration.setProperty(name,value);
			}
			//TODO  upisivanje podataka u settings file iz kojeg podaci potjecu
//			Properties p = J2EEContext.getInstance().getProperties();
//			String settingsPath = p.getProperty("SETTINGS_PATH");
//			String file = settingsPath + J2EEDefaultJDBCService.class.getName() + ".properties";
//			writeDataToFile(service._serverConfiguration, file);

			/* Close all old connections and open new ones.*/
			if(restartConnections){
				service.disconnectAll();
				service._CONNECTION_MANAGER = new J2EEConnectionManagerJDBC(service._serverConfiguration);
			}
		}
	}catch(Exception e){
		System.out.println("Greska kod editDefaultJdbcConfiguration");
	}
	return req;
}
public AS2Record setDefaultAs400Configuration(AS2ConnectionVo req) throws AS2Exception {
    return null;
}
public AS2RecordList getExceptionDetailsForId(AS2Record req) throws AS2Exception {
    return new AS2RecordList();//getMessageDetails
}
public AS2RecordList getExceptionsForId(AS2Record req) throws AS2Exception {
    //return new J2EEResultSet();//getExceptions
    return getServerExceptions(req);
}
public AS2RecordList getSQLQueryResults(AS2Record req) throws Exception {
    J2EEDataAccessObjectJdbc dao = new J2EEDataAccessObjectJdbc();
    AS2Trace.traceStringOut(AS2Trace.W, "SQL = "+req.get("sql"));
    return dao.daoExecuteQuery(req.get("sql"));
}
public AS2Record startSQLJob(AS2Record req) throws Exception {
	AS2Record ret = new AS2Record();
    J2EEDataAccessObjectJdbc dao = new J2EEDataAccessObjectJdbc();
    boolean res = dao.daoStartSQLJob(req.get("job_name"));
    ret.set("result",res);
    return ret;
}
}
