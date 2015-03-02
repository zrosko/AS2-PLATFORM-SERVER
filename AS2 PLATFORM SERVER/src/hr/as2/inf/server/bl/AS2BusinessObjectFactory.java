package hr.as2.inf.server.bl;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.exceptions.AS2BusinessLogicException;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.logging.AS2Trace;

import java.util.Hashtable;
/**
 * This class is a Business Object Factory. It creates 
 * the Business Objects and keeps them to be used by the 
 * applications as needed.
 * Business Objects are stateless and can be reused by multiple clients. 
 */
public final class AS2BusinessObjectFactory {
	/**
	 * Singleton reference.
	 */
	private static AS2BusinessObjectFactory _instance = null;
	/**
	 * Collection of already created BusinessObjects.
	 */
	private Hashtable<String, AS2BusinessObject> _businessObjects;
/**
 * Prevents the instantiation by the other objects.
 */
private AS2BusinessObjectFactory () {
	_businessObjects = new Hashtable<String, AS2BusinessObject>(50);
	AS2Context.setSingletonReference(this);	
}
/**
 * Instantiate the business object, store it in the collectio
 * and return the reference to a caller.
 */
private AS2BusinessObject create(String bo) throws AS2BusinessLogicException {
	Object ob = null;
	try {
		if (bo != null) {
			ob = Class.forName(bo).newInstance();
		}
	} catch (Exception e) {
		AS2Trace.trace(AS2Trace.E, e, "BO can not be created by Factory: " + bo);
		AS2BusinessLogicException ble = new AS2BusinessLogicException("102");
		ble.addCauseException(e);
		throw ble;
	}
	// BO created. Store the object for usage by other clients.
	_businessObjects.put(bo,(AS2BusinessObject)ob);
	return (AS2BusinessObject) ob;
}
/**
 * Delete the business object.
 */
public synchronized void destroy(String bo) {
	_businessObjects.remove (bo);
}
/**
 * Delete all the business objects.
 */
public synchronized void destroyAll() {
	_businessObjects = null;
	_businessObjects = new Hashtable<String, AS2BusinessObject>();
}
/**
 * Returns the singleton instance.
 */
public static AS2BusinessObjectFactory getInstance() {
	if(_instance == null)
		_instance = new AS2BusinessObjectFactory();
	return _instance;
}

/**
 * Get the business object from the collection
 * and return the reference to a caller.
 * If business object does not exist create one.
 */
public AS2BusinessObject getBusinessObject (String bo) throws AS2BusinessLogicException, AS2DataAccessException {
	AS2BusinessObject aBo = _businessObjects.get(bo);
	if(aBo == null)
		aBo = create(bo);
	return aBo;
}
}
