package hr.as2.inf.server.da.key;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;

public interface J2EEKeyFactory {
	public static final String INCREMENT="INCREMENT";
	public static final String TABLE_NAME="TABLE_NAME";
	public static final String COLUMN_NAME="COLUMN_NAME";
public Object generateKey(AS2Record aFields) throws AS2Exception;
public Object generateKey(String tableName, String columnName)
    throws AS2Exception;
}
