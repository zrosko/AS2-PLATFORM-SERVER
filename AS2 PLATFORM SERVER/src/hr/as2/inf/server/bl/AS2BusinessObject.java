package hr.as2.inf.server.bl;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2BusinessLogicException;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.metrics.AS2ArmService;
import hr.as2.inf.server.da.J2EEDataAccessObject;
import hr.as2.inf.server.da.J2EEDataAccessObjectFactory;

import java.util.Vector;
/**
 * The AS2BusinessObject defines common base class for Business
 * Object to inherit from. The subclassed object should handle 
 * business logic only. All data access logic (SQL, MQ) should
 * be delegated to the Data Access Objects.
 * NOTE: Business objects are "pass-through" handlers. There is no
 * object caching at the server. Explicit caching is to be done 
 * through the AS2BusinessObjectCacheManager (future) which will supports READ, ADD, 
 * DELETE but not the UPDATE of the business object.
 * The concept used by business objects is taken from EJB Spec 1.0.
 * Future (optional) move to EJB is supported by design.
 */
public abstract class AS2BusinessObject extends AS2Record {
	private static final long serialVersionUID = 1L;
	/**
	 * Data Access Object to access a data source (JDBC, MQ, ...).
	 * Future release (optional) will contain a Vector of data access object
	 * in case a business object need to persist in more then one
	 * data source. mDao is set through property file 
	 * (AS2CommandFactory.properties). 
	 */
	protected J2EEDataAccessObject _dao;
	protected J2EEDataAccessObject _second_dao;
	
	public static final String BO_CREATE_RESULT = "BO_CREATE_RESULT";
	public static final String BO_STORE_RESULT = "BO_STORE_RESULT";
	
	public static final String BO_SERVICE = "BO_SERVICE";
	public static final String BO_STORE_SERVICE = "BO_STORE_SERVICE";
	public static final String BO_CREATE_SERVICE = "BO_CREATE_SERVICE";
	public static final String BO_REMOVE_SERVICE = "BO_REMOVE_SERVICE";
	public static final String BO_REMOVE_ALL_SERVICE = "BO_REMOVE_ALL_SERVICE";

public AS2BusinessObject() throws AS2DataAccessException{
	super();
	getDataAccessObjects();
}
public AS2Record boCreate(AS2Record aFields) throws Exception {
	AS2Record res = null;
	
	try {
		AS2ArmService.armStop(AS2ArmService.BUSINESS_LOGIC);
		AS2ArmService.armStart(AS2ArmService.DATA_ACCESS);
		
		if (_dao != null) {
			res = _dao.daoCreate(aFields);
		} else {
			AS2Trace.trace(AS2Trace.E, "No Data Access Object found for: " + getClass().getName());
			AS2DataAccessException e = new AS2DataAccessException("101");
			e.setErrorDescription(getClass().getName());
			throw e;
		}
		//for postCreate trigger
		aFields.set(BO_CREATE_RESULT, res);
		aFields.set(BO_SERVICE, BO_CREATE_SERVICE);
		boPostCreate(aFields);
		//notify result set cache.
		setChanged();
		notifyObservers(aFields);
	} finally {
		AS2ArmService.armStop(AS2ArmService.DATA_ACCESS);
		AS2ArmService.armStart(AS2ArmService.BUSINESS_LOGIC);
	}
	return res;
}
public AS2RecordList boFind(AS2Record aFields) throws Exception {
	AS2RecordList res = null;
	
	try {
		AS2ArmService.armStop(AS2ArmService.BUSINESS_LOGIC);
		AS2ArmService.armStart(AS2ArmService.DATA_ACCESS);
		
		if (_dao != null) {
			res = _dao.daoFind(aFields);
		} else {
			AS2Trace.trace(AS2Trace.E, "No Data Access Object found for: " + getClass().getName());
			AS2DataAccessException e = new AS2DataAccessException("101");
			e.setErrorDescription(getClass().getName());
			throw e;
		}
	} finally {
		AS2ArmService.armStop(AS2ArmService.DATA_ACCESS);
		AS2ArmService.armStart(AS2ArmService.BUSINESS_LOGIC);
	}
	return res;
}
public AS2RecordList boLoad(AS2Record aFields) throws Exception {
	AS2RecordList res = null;
	
	try {
		AS2ArmService.armStop(AS2ArmService.BUSINESS_LOGIC);
		AS2ArmService.armStart(AS2ArmService.DATA_ACCESS);
		
		if (_dao != null) {
			res = _dao.daoLoad(aFields);
		} else {
			AS2Trace.trace(AS2Trace.E, "No Data Access Object found for: " + getClass().getName());
			AS2DataAccessException e = new AS2DataAccessException("101");
			e.setErrorDescription(getClass().getName());
			throw e;
		}
	} finally {
		AS2ArmService.armStop(AS2ArmService.DATA_ACCESS);
		AS2ArmService.armStart(AS2ArmService.BUSINESS_LOGIC);
	}
	return res;
}
/**
 * The business object load next using the key.
 */
public AS2RecordList boLoadNext(AS2Record aFileds) throws Exception {
	AS2RecordList res = null;
	
	try {
		AS2ArmService.armStop(AS2ArmService.BUSINESS_LOGIC);
		AS2ArmService.armStart(AS2ArmService.DATA_ACCESS);
		
		if (_dao != null) {
			res = _dao.daoLoadNext(aFileds);
		} else {
			AS2Trace.trace(AS2Trace.E, "No Data Access Object found for: " + getClass().getName());
			AS2DataAccessException e = new AS2DataAccessException("101");
			e.setErrorDescription(getClass().getName());
			throw e;
		}
	} finally {
		AS2ArmService.armStop(AS2ArmService.DATA_ACCESS);
		AS2ArmService.armStart(AS2ArmService.BUSINESS_LOGIC);
	}
	return res;
}
public void boPostCreate (AS2Record aFields) throws AS2BusinessLogicException, AS2DataAccessException {
}
public void boPostRemove (AS2Record aFields) throws AS2BusinessLogicException, AS2DataAccessException {
}
public void boPostStore (AS2Record aFields) throws AS2BusinessLogicException, AS2DataAccessException {
}
public void boRemove(AS2Record aFields) throws Exception {
	try {
		
		AS2ArmService.armStop(AS2ArmService.BUSINESS_LOGIC);
		AS2ArmService.armStart(AS2ArmService.DATA_ACCESS);
		
		if (_dao != null) {
			_dao.daoRemove(aFields);
		} else {
			AS2Trace.trace(AS2Trace.E, "No Data Access Object found for: " + getClass().getName());
			AS2DataAccessException e = new AS2DataAccessException("101");
			e.setErrorDescription(getClass().getName());
			throw e;
		}
		//for postRemove trigger
		boPostRemove(aFields);
		aFields.set(BO_SERVICE, BO_REMOVE_SERVICE);
		//notify result set cache.
		setChanged();
		notifyObservers(aFields);
	} finally {
		AS2ArmService.armStop(AS2ArmService.DATA_ACCESS);
		AS2ArmService.armStart(AS2ArmService.BUSINESS_LOGIC);
	}
}
public void boRemoveAll(AS2Record aFields) throws Exception {
	try {
		AS2ArmService.armStop(AS2ArmService.BUSINESS_LOGIC);
		AS2ArmService.armStart(AS2ArmService.DATA_ACCESS);
		
		if (_dao != null) {
			_dao.daoRemoveAll(aFields);
		} else {
			AS2Trace.trace(AS2Trace.E, "No Data Access Object found for: " + getClass().getName());
			AS2DataAccessException e = new AS2DataAccessException("101");
			e.setErrorDescription(getClass().getName());
			throw e;
		}
		//notify result set cache.
		aFields.set(BO_SERVICE, BO_REMOVE_ALL_SERVICE);
		setChanged();
		notifyObservers(aFields);
	} finally {
		AS2ArmService.armStop(AS2ArmService.DATA_ACCESS);
		AS2ArmService.armStart(AS2ArmService.BUSINESS_LOGIC);
	}
}
public AS2Record boStore(AS2Record aFields) throws Exception {
	AS2Record res = null;
	try {
		
		AS2ArmService.armStop(AS2ArmService.BUSINESS_LOGIC);
		AS2ArmService.armStart(AS2ArmService.DATA_ACCESS);
		if (_dao != null) {
			res = _dao.daoStore(aFields);
		} else {
			AS2Trace.trace(AS2Trace.E, "No Data Access Object found for: " + getClass().getName());
			AS2DataAccessException e = new AS2DataAccessException("101");
			e.setErrorDescription(getClass().getName());
			throw e;
		}
		//for postStore trigger
		aFields.set(BO_STORE_RESULT, res);
		aFields.set(BO_SERVICE, BO_STORE_SERVICE);
		boPostStore(aFields);
		//notify result set cache.
		setChanged();
		notifyObservers(aFields);
	} finally {
		AS2ArmService.armStop(AS2ArmService.DATA_ACCESS);
		AS2ArmService.armStart(AS2ArmService.BUSINESS_LOGIC);
	}
	return res;
}
/**
 * Attach the Data Access Object.
 */
public void getDataAccessObjects() throws AS2DataAccessException {
    if (_dao == null){
        Vector<J2EEDataAccessObject> _daos = J2EEDataAccessObjectFactory.getInstance().getMultipleDataAccessObjects(this);
        if(_daos.size()>1){
            _dao = (J2EEDataAccessObject)_daos.elementAt(0);
            _second_dao = (J2EEDataAccessObject)_daos.elementAt(1);
            
        }else if(_daos.size()==1){
            _dao = (J2EEDataAccessObject)_daos.elementAt(0); 
        }
    }
//	if (_dao == null)
//		_dao = (J2EEDataAccessObject) AS2CommandFactory.getInstance().getDataAccessObject(this);
}
public String toString() {
	return super.toString();
}
}
