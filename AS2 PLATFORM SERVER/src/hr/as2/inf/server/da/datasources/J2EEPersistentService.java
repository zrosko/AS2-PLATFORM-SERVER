package hr.as2.inf.server.da.datasources;

import hr.as2.inf.common.cache.AS2CacheManager;
import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.connections.J2EEConnection;
import hr.as2.inf.server.connections.J2EEConnectionManager;
import hr.as2.inf.server.connections.as400.AS2ConnectionManagerAS400;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionManagerJDBC;
import hr.as2.inf.server.da.metadata.J2EEMetaDataService;
import hr.as2.inf.server.transaction.AS2Transaction;
import hr.as2.inf.server.transaction.AS2TransactionFactory;

import java.io.File;
import java.util.Hashtable;
import java.util.Properties;
/**
 * Holds a query results from a data source. 
 * Data Access Objects may cache the query results (J2EEResultSet)
 * here and use the results later instead of going to the data source
 * all the time, even though the data has not been changed since the last query.
 * There is the assumption than data always get changed trhough the 
 * Data Access Objects (no other applications change data in the data source). 
 */
public abstract class J2EEPersistentService {
	public J2EEConnectionManager _CONNECTION_MANAGER;
	public Properties _serverConfiguration;
	protected Hashtable<String, Object> _OLD_CACHE = new Hashtable<String, Object>(1000);
	protected String _DEFAULT_RESOURCE_BUNDLE;
	private AS2CacheManager _cache = null;
  	/* Property file location. */
  	protected String _propertyFilePath = null;
  	protected J2EEMetaDataService _metaData = null;
  	protected String _METADATA_FILE=null;
public J2EEPersistentService() {
	initialize();
}
public J2EEMetaDataService getMetaData(){
	return _metaData;
}
public abstract void addToCache(String aBoFinder, AS2RecordList aRs) throws AS2Exception;
public void disconnectAll() {
	_CONNECTION_MANAGER.finalize();
}
public synchronized AS2CacheManager getCacheManager() {
  if(_cache==null || !_cache.isCacheActive()) {
    _cache = new AS2CacheManager();
  }
  return _cache;
}
public int getCacheSize() {
	return _OLD_CACHE.size();
}
/** 
 * Use persistence service class name concatinated with the 
 * thread name as a connection name.
 * During the same transaction the same connection to the same data 
 * source is required. Otherwise data base can get locked if using
 * two separate connection to the same data source during the same
 * transaction.
 */
public final J2EEConnection getConnection() throws AS2Exception {
	J2EEConnection conn = null;
	
	try {
		if(AS2TransactionFactory.getInstance().currentTransaction()!=null){
			conn = AS2Transaction.getConnectionByName(this.getClass().getName() + Thread.currentThread().getName());
			if(conn!=null)
				AS2Trace.trace(AS2Trace.I, conn, "Retrieved a connection from the Transaction");
		}	
	
		if (conn == null) {
			conn = _CONNECTION_MANAGER.getConnection(); 
			conn.setName(this.getClass().getName() + Thread.currentThread().getName());
			if (conn != null){
				if(AS2TransactionFactory.getInstance().currentTransaction()!=null){
					conn.setAutoCommit(false);
					AS2Transaction.addConnection(conn);
					AS2Trace.trace(AS2Trace.I, conn, "Added a connection to the Transaction");
				}	
			}	
		}
	} catch (AS2Exception e) {
		AS2Exception ex = new AS2Exception("302");
		ex.addCauseException(e);
		throw ex;
	}
	
	return conn;
}
protected abstract String getConnectionManagerType();	
// JDBC
// MQ
// CICS
// AS400
public AS2RecordList getFromCache(String aFinder) {
	
	if (AS2Context.getInstance().SERVER_CACHING_IND) {
		
		AS2Trace.trace(AS2Trace.I, "J2EEDomainPersistentService.getFromCache On - " + aFinder);
		
		AS2RecordList rs = (AS2RecordList) _OLD_CACHE.get(aFinder);
		
		if (rs != null){
		
			if (rs.hasChanged()){
				AS2Trace.trace(AS2Trace.I, "J2EEDomainPersistentService.getFromCache - Has Changed");
				return null;
			}else{
				AS2Trace.trace(AS2Trace.I, "J2EEDomainPersistentService.getFromCache - Found");
				return (AS2RecordList)rs.clone();
			}
		}
		AS2Trace.trace(AS2Trace.I, "J2EEDomainPersistentService.getFromCache - Not Found");		
		return rs;
	} else 
		return null;
}
public String getPropertyFilePath() throws AS2Exception {
	if(_propertyFilePath == null)
		_propertyFilePath =  AS2Helper.findPropertyFilePath(AS2Context.getPropertiesPath()+"/server/" + getServerConfigurationFile("properties").getName());
	if(_propertyFilePath == null)
			throw new AS2Exception("293");
	return _propertyFilePath;
}
public String getResourceBoundle(){
	return 	_DEFAULT_RESOURCE_BUNDLE;
}	
protected File getServerConfigurationFile(String aType){
	return new File(getClass().getName() + "." +aType);
}		
private void initialize(){

	try{		
		if(_serverConfiguration == null)
			readPropertyFile();
			
		String cmType = getConnectionManagerType();
		
		if(cmType.equals(AS2Constants.JDBC)){
			_CONNECTION_MANAGER = new J2EEConnectionManagerJDBC(_serverConfiguration);
		//}else if(cmType.equals(J2EEConstants.MQ)){
			//mCONNECTION_MANAGER = new J2EEConnectionManagerMQ(mServerConfiguration);	
		//}else if(cmType.equals(J2EEConstants.CICS)){
			//mCONNECTION_MANAGER = new J2EEConnectionManagerCICS(mServerConfiguration);
		}else if(cmType.equals(AS2Constants.AS400)){
			_CONNECTION_MANAGER = new AS2ConnectionManagerAS400(_serverConfiguration);
//			//metaDataInitialization
//			_METADATA_FILE = _serverConfiguration .getProperty("METADATAFILE", "hr/banksoft/resources/bsa/server/pcml/bsa.pcml");
//			if(!_METADATA_FILE.equals(""))
//				_metaData = new J2EEMetaDataService(_METADATA_FILE);
//			/* CB dodano start */
//			_METADATA_FILE = _serverConfiguration .getProperty("METADATAFILE", "hr/banksoft/resources/bsa/server/pcml/absa.pcml");
//			if(!_METADATA_FILE.equals(""))
//				_metaData.readResourceFile(_METADATA_FILE);
//			_METADATA_FILE = _serverConfiguration .getProperty("METADATAFILE", "hr/banksoft/resources/bsa/server/pcml/Cbsa.pcml");
//			if(!_METADATA_FILE.equals(""))
//			_metaData.readResourceFile(_METADATA_FILE);
//			_METADATA_FILE = _serverConfiguration .getProperty("METADATAFILE", "hr/banksoft/resources/bsa/server/pcml/Dbsa.pcml");
//			if(!_METADATA_FILE.equals(""))
//			_metaData.readResourceFile(_METADATA_FILE);
//			_METADATA_FILE = _serverConfiguration .getProperty("METADATAFILE", "hr/banksoft/resources/bsa/server/pcml/Zbsa.pcml");
//			if(!_METADATA_FILE.equals(""))
//			_metaData.readResourceFile(_METADATA_FILE);
		}else{
			//throw new J2EEDataAccessException("301");
		}
		/* CB dodano end */
	}catch(AS2Exception e){
		AS2Trace.trace(AS2Trace.E, e, "Eror during persistenece service connections initialization");
	}							
}
protected void readPropertyFile(){
	_serverConfiguration =  AS2Helper.readPropertyFileAsURL(AS2Context.getPropertiesPath() +"/server/"+ getServerConfigurationFile("properties").getName());	
}	
public void setResourceBoundle(String value){
	_DEFAULT_RESOURCE_BUNDLE = value;
}	
public void setResults(Hashtable<String, Object> value) {
	AS2Trace.trace(AS2Trace.W, "Removing the current cache; size was: " + _OLD_CACHE.size());
	
	synchronized (_OLD_CACHE) {
		_OLD_CACHE = value;			
	}
}
}
