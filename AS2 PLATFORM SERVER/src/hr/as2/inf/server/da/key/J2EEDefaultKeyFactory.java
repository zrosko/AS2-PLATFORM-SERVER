package hr.as2.inf.server.da.key;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;

import java.util.Random;

public class J2EEDefaultKeyFactory implements J2EEKeyFactory {
	private static J2EEDefaultKeyFactory _keyFactory=null;
public J2EEDefaultKeyFactory() {
	super();
}
public Object generateKey(AS2Record aFields) throws AS2Exception{
	return generateKey(null,null);
}
public Object generateKey(String tableName, String columnName)  throws AS2Exception{
	Random rand=new Random(System.currentTimeMillis());
	return new Long(rand.nextLong());
}
public static J2EEDefaultKeyFactory getInstance() {
	if (_keyFactory==null)
		return new J2EEDefaultKeyFactory();
	return _keyFactory;
}
}
