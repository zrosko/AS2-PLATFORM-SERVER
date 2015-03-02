package hr.as2.inf.server.da;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.logging.AS2Trace;

import java.util.Properties;
import java.util.Vector;
/**
 * The Data Access Object Factory holds a mapping that BO 
 * query to get the Data Access Object that handles
 * their persistence.
 * The map is read from a resource file named 
 * /bsa/settings/J2EEDataAccessObjectFactory.  
 * The file format is the format used by the java.util.Properties class.
 * @version 1.0 
 * @author 	Zdravko Rosko
 */
public final class J2EEDataAccessObjectFactory {
	public static String _class = "hr.as2.inf.server.da.AS2DataAccessObjectFactory";
	/** properties file name */
	private static final String PROPERTIES_FILE = "/server/AS2DataAccessObjectFactory.properties";

	/** singleton instance of this class */
	private static J2EEDataAccessObjectFactory _instance = null;

	/** map from BO class name to data access object */
	private static Properties _boToDataAccessObjectMap;

	/** maximum number of data access objects per business object */
	private static int MAXDAO = 1;

	/** default dao type */
	private static String DEFAULT_DAO = "As400";
	private static String SECOND_DAO = "Jdbc";
/**
 * Construct an data access object registry and
 * load the BO to data access object map from a properties file.
 * The constructor is private to allow only this class to create instances of itself.
 */
private J2EEDataAccessObjectFactory() {
	initialize();
	AS2Context.setSingletonReference(this);	
}
/**
 * Construct a data access object usign the map
 * and return it to the caller (BO).
 * @param Object bo (J2EEBusinessObject)
 */
public Object getDataAccessObject(Object bo) throws AS2DataAccessException {
	Object ob = null;
	String daoClassNameFromProperty = _boToDataAccessObjectMap.getProperty(bo.getClass().getName());
	StringBuffer daoClassName = new StringBuffer();
	String packageName="";
	String restName="";
	
	try {
		if (daoClassNameFromProperty == null) {
			// take the DEFAULT entered in the property file as a default
			daoClassNameFromProperty = bo.getClass().getName();
			int startBl = daoClassNameFromProperty.indexOf(".bo.");
			if(startBl == -1 )
				startBl = daoClassNameFromProperty.indexOf(".bl.");
			packageName = daoClassNameFromProperty.substring(0, startBl + 1);
			restName = daoClassNameFromProperty.substring(startBl + 3);
			startBl = restName.indexOf("BO");
			if(startBl>0)
				restName = restName.substring(0, startBl);
				
			daoClassName.append(packageName); 
			daoClassName.append("da."); 
			daoClassName.append(DEFAULT_DAO.toLowerCase());
			daoClassName.append(restName);
			daoClassName.append(DEFAULT_DAO);
		}else{
			daoClassName.append(daoClassNameFromProperty);
		}
		try{
			ob = Class.forName(daoClassName.toString()).newInstance();
		}catch(ClassNotFoundException e){
			daoClassName = new StringBuffer();
			daoClassName.append(packageName); 
			daoClassName.append("da."); 
			daoClassName.append(SECOND_DAO.toLowerCase());
			daoClassName.append(restName);
			daoClassName.append(SECOND_DAO);			
			ob = Class.forName(daoClassName.toString()).newInstance();
		}
	} catch (Exception e) {
		AS2Trace.trace(AS2Trace.E, e, "DAO for " + 
			bo.getClass().getName() + " can not be created: " + daoClassNameFromProperty);
		AS2DataAccessException dae = new AS2DataAccessException("155");
		dae.addCauseException(e);
		throw dae;
	}
	return ob;
}
/**
 * Singleton.
 */
public static J2EEDataAccessObjectFactory getInstance () {
	
	if (_instance == null)
		_instance = new J2EEDataAccessObjectFactory();
	return _instance;
}
/**
 * Construct a Vector of data access object usign the map
 * and return it to the caller (BO).
 * @param Object bo (J2EEBusinessObject)
 */
public Vector<J2EEDataAccessObject> getMultipleDataAccessObjects(Object bo) throws AS2DataAccessException {
	Vector<J2EEDataAccessObject> dao = new Vector<J2EEDataAccessObject>(MAXDAO);
	if (bo == null)
		return dao;
	Object o = getDataAccessObject(bo); 
	if (o != null)
		dao.addElement((J2EEDataAccessObject)o);
	String boName = bo.getClass().getName();
	for (int i = 1; i <= MAXDAO; i++) {
		String boNameI = boName + i;
		String daoName = _boToDataAccessObjectMap.getProperty(boNameI);
		try {
			if (daoName != null) {
				Object ob = Class.forName(daoName).newInstance();
				dao.addElement((J2EEDataAccessObject)ob);
			}
		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, e, "DataAccessObject for " + bo.getClass().getName() + " can not be created: " + daoName);
			AS2DataAccessException j2e = new AS2DataAccessException("155");
			j2e.addCauseException(e);
			throw j2e;
		}
	}
	return dao;
}
/**
 * Read the property file and initialize map.
 */
	protected void initialize() {
		_boToDataAccessObjectMap = AS2Helper.readPropertyFileAsURL(AS2Context
				.getPropertiesPath() + PROPERTIES_FILE);
		DEFAULT_DAO = _boToDataAccessObjectMap.getProperty(_class
				+ ".DEFAULT_DAO", "As400");
		SECOND_DAO = _boToDataAccessObjectMap.getProperty(_class
				+ ".SECOND_DAO", "Jdbc");
		MAXDAO = AS2Helper.getIntProperty(_boToDataAccessObjectMap, _class
				+ ".MAXDAO", 1);
	}
}
