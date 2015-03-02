package hr.as2.inf.server.da.key;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;

public class J2EEKeyGenerator {
	public final static String KEY_FACTORY="KEY_FACTORY";
	protected J2EEKeyFactory _keyFactory;
	private static J2EEKeyGenerator _keyGenerator=null;
protected J2EEKeyGenerator() {
	super();
	AS2Context.setSingletonReference(this);	
}
public Object generateKey(AS2Record data) throws AS2Exception {
	try {
		_keyFactory = (J2EEKeyFactory) data.getAsObject(KEY_FACTORY);
	} catch (Exception e) {
	}
	if (_keyFactory == null)
		throw new AS2Exception("169");
	return _keyFactory.generateKey(data);
}
public Object generateKey(J2EEKeyFactory keyFactory, String tableName, String columnName)  throws AS2Exception{
	if (keyFactory==null)
		throw new AS2Exception("169");
	return keyFactory.generateKey(tableName,columnName);
}
public Object generateKey(String tableName, String columnName)  throws AS2Exception {
	return generateKey(_keyFactory,tableName,columnName);
}
public static J2EEKeyGenerator getInstance() {
    if (_keyGenerator == null)
        _keyGenerator = new J2EEKeyGenerator();
    return _keyGenerator;
}
public J2EEKeyFactory getKeyFactory() {
	return _keyFactory;
}
public void setKeyFactory(J2EEKeyFactory keyFactory) {
	_keyFactory = keyFactory;
}
}
