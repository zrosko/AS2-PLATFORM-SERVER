package hr.as2.inf.server.da.facade;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.server.da.jdbc.J2EEDataAccessObjectJdbc;

/**
 * Generic server facade to access and manipulate data on a generic way.
 *
 */
public final class AS2DataAccessFacadeServer {

    private static AS2DataAccessFacadeServer _instance = null;

    public static AS2DataAccessFacadeServer getInstance() {
        if (_instance == null) {
            _instance = new AS2DataAccessFacadeServer();
        }
        return _instance;
    }
    
    private AS2DataAccessFacadeServer() {
    }
    private String getTableNameFull(AS2Record value){
    	String databaseName = value.get("databaseName");
    	String schemaName = value.get("schemaName");
    	String tableName = value.get("tableName");
    	return databaseName+"."+schemaName+"."+tableName;
    }
    public AS2RecordList fetchTable(AS2Record value) throws Exception {

    	String whereSql = value.get("whereSql");
    	String sql = "select * from "+getTableNameFull(value)+" "+whereSql;
    	J2EEDataAccessObjectJdbc _dao = new J2EEDataAccessObjectJdbc();
    	AS2RecordList rs = _dao.daoExecuteQuery(sql);
    	if(value.exists("@meta")){
    		AS2RecordList rs_meta = new AS2RecordList();
    		for(String column:rs.getMetaData().keySet()){
				rs_meta.addRow( rs.getMetaData(column).prepareRecord());				
			}
    		return rs_meta;
    	}
    	return rs;
    }
    public AS2Record addTableRow(AS2Record value) throws Exception {
    	J2EEDataAccessObjectJdbc _dao = new J2EEDataAccessObjectJdbc();
    	_dao.getConnection().setDBNAME(value.get("databaseName"));
    	_dao.getConnection().setSCHEMA(value.get("schemaName"));
    	_dao.setTableName(value.get("tableName"));
    	return _dao.daoCreate(value);
    }
    public AS2Record updateTableRow(AS2Record value) throws Exception {
    	J2EEDataAccessObjectJdbc _dao = new J2EEDataAccessObjectJdbc();
    	_dao.getConnection().setDBNAME(value.get("databaseName"));
    	_dao.getConnection().setSCHEMA(value.get("schemaName"));
    	_dao.setTableName(value.get("tableName"));
    	return _dao.daoStore(value);
    }
    public AS2Record deleteTableRow(AS2Record value) throws Exception {
    	J2EEDataAccessObjectJdbc _dao = new J2EEDataAccessObjectJdbc();
    	_dao.getConnection().setDBNAME(value.get("databaseName"));
    	_dao.getConnection().setSCHEMA(value.get("schemaName"));
    	_dao.setTableName(value.get("tableName"));
    	_dao.daoRemove(value);
    	return value;
    }
}