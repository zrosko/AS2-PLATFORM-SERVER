package hr.as2.inf.server.da.jdbc;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.email.AS2EmailConstants;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.format.AS2Format;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.J2EEDataAccessObject;
import hr.as2.inf.server.da.datasources.J2EEDefaultJDBCService;
import hr.as2.inf.server.da.key.J2EEDefaultKeyFactory;
import hr.as2.inf.server.da.key.J2EEKeyFactory;
import hr.as2.inf.server.da.key.J2EEKeyGenerator;
import hr.as2.inf.server.da.valuelisthandler.AS2ValueListHandler;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class J2EEDataAccessObjectJdbc extends J2EEDataAccessObject {
	public static final String CREATE = "CREATE";
	public static final String FIND = "FIND";
	public static final String LOAD = "LOAD";
	public static final String STORE = "STORE";
	public static final String REMOVE = "REMOVE";
	public static final String REMOVE_ALL = "REMOVE_ALL";
	public static final String FIND_ALL_KEYS = "FIND_ALL_KEYS";
	public static final String SCHEMA = "SCHEMA.";
	public static final String QUERY = "QUERY";
	private String _tableName;
	private LinkedHashMap<String, Object> _sqlStatementCache = new LinkedHashMap<String, Object>();
	protected boolean _keysUpdateable = false;

	/* BULK operations */
	public int daoCreateMany(AS2RecordList rs) throws AS2Exception {
		for (AS2Record vo : rs.getRows()) {
			daoCreate(vo);
		}
		return rs.size();
	}
	
	public int daoRemoveMany(AS2RecordList valueList) throws AS2Exception {
		for (AS2Record vo : valueList.getRows()) {
			daoRemove(vo);
		}
		return valueList.size();
	}
	
	public void daoRemoveAll(AS2Record data) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public int daoStoreMany(AS2RecordList valueList) throws AS2Exception {
		for (AS2Record vo : valueList.getRows()) {
			daoStore(vo);
		}
		return valueList.size();
	}
	
	/* SINGLE operations */
	public AS2Record daoCreate(AS2Record data) throws AS2Exception {
		AS2Record res = new AS2Record();
		J2EEConnectionJDBC co = null;
		try {
			prepareData(data, getTableName());
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = prepareSqlCreate(data, getTableName());
			PreparedStatement pstmt = jco.prepareStatement(sql);
			AS2RecordList columnsInfo = co.getColumnsInfo(getTableName());
			ArrayList<AS2Record> columns = columnsInfo.getRows();
			// validate length
			validateFieldLength(columnsInfo, data);
			// start prepare insert
			LinkedHashMap<String, Object> hashKeys = co.getPrimaryKeysInHashtable(getTableName());
			String keyName = "";
			int i = 0;
			for (AS2Record model : columns) {
				LinkedHashMap<String, Object> row = model.getProperties();
				if (co.isIdentityColumn(row)) {
					keyName = co.getColumnName(row);
					continue; // identity column, do nothing
				}
				String _table_col = co.getColumnName(row);
				String _data_col = _table_col.toLowerCase();
				Object _data_value = data.getAsObject(_data_col);
				if (_data_value != null) {
					int _type = co.getColumnDataType(row);
					if (_data_value.toString().equals("")){
						pstmt.setNull(++i, _type);
					}else{//not null begin					
						switch (_type) {
						case Types.DECIMAL:
							pstmt.setBigDecimal(++i, data.getAsBigDecimal(_data_col));
							break;
						case Types.TIMESTAMP:
							pstmt.setTimestamp(++i, data.getAsSqlTimestamp(_data_col));
							break;
						case Types.DATE:
							pstmt.setDate(++i, data.getAsSqlDate(_data_col));
							break;
						case Types.INTEGER:
							pstmt.setInt(++i, data.getAsInt(_data_col));
							break;
						case Types.SMALLINT:
							pstmt.setShort(++i, data.getAsShort(_data_col));
							break;
						case Types.CHAR:
							pstmt.setString(++i, data.getAsStringOrBlank(_data_col));
							break;
						default:
							pstmt.setObject(++i, _data_value);						
						}//not null
					}
					res.set(_data_col, _data_value);
				} else {
					if (hashKeys.get(_table_col) != null) {
						try {
							Object newKey = J2EEKeyGenerator.getInstance()
									.generateKey(getKeyFactory(),
											getTableName(), _table_col);

							int _type = co.getColumnDataType(row);
							switch (_type) {
							case Types.DECIMAL:
								pstmt.setBigDecimal(++i, new BigDecimal(newKey.toString()));
								break;
							case Types.TIMESTAMP:
								pstmt.setTimestamp(++i,	Timestamp.valueOf(newKey.toString()));
								break;
							case Types.DATE:
								pstmt.setDate(++i, java.sql.Date.valueOf(newKey.toString()));
								break;
							case Types.INTEGER:
								pstmt.setInt(++i,Integer.valueOf(newKey.toString()).intValue());
								break;
							case Types.SMALLINT:
								pstmt.setShort(++i,Short.valueOf(newKey.toString()).shortValue());
								break;
							case Types.CHAR:
								pstmt.setString(++i, newKey.toString());
								break;
							default:
								pstmt.setObject(++i, newKey);
							}
							res.set(_data_col, newKey);
						} catch (AS2Exception ke) {
							int columnSqlType = co.getColumnDataType(row);
							switch (columnSqlType) {
							case Types.DECIMAL:
								pstmt.setBigDecimal(++i,
										new java.math.BigDecimal(co
												.getColumnDefaultValue(row)
												.toString()));
								break;
							case Types.TIMESTAMP:
								pstmt.setTimestamp(++i, Timestamp.valueOf(co
										.getColumnDefaultValue(row).toString()));
								break;
							case Types.DATE:
								pstmt.setDate(++i, java.sql.Date.valueOf(co
										.getColumnDefaultValue(row).toString()));
								break;
							case Types.INTEGER:
								pstmt.setInt(++i, Integer.valueOf(
												co.getColumnDefaultValue(row)
														.toString()).intValue());
								break;
							case Types.SMALLINT:
								pstmt.setShort(++i,	Short.valueOf(
												co.getColumnDefaultValue(row)
														.toString()).shortValue());
								break;
							case Types.CHAR:
								pstmt.setString(++i,
										co.getColumnDefaultValue(row)
												.toString());
								break;
							default:
								pstmt.setObject(++i,co.getColumnDefaultValue(row));
							}
							res.set(_data_col, co.getColumnDefaultValue(row));
						}
					} else {
						int columnSqlType = co.getColumnDataType(row);
						Object defValue = co.getColumnDefaultValue(row).toString();
						if ("".equals(defValue)) {
							pstmt.setNull(++i, columnSqlType);
						} else {
							switch (columnSqlType) {
							case Types.DECIMAL:
								pstmt.setBigDecimal(++i,new BigDecimal((String) defValue));
								break;
							case Types.TIMESTAMP:
								pstmt.setTimestamp(++i,Timestamp.valueOf((String) defValue));
								break;
							case Types.DATE:
								pstmt.setDate(++i,Date.valueOf((String) defValue));
								break;
							case Types.INTEGER:
								pstmt.setInt(++i,Integer.valueOf((String) defValue).intValue());
								break;
							case Types.SMALLINT:
								pstmt.setShort(++i,Short.valueOf((String) defValue).shortValue());
								break;
							case Types.CHAR:
								pstmt.setString(++i, (String) defValue);
								break;
							default:
								pstmt.setObject(++i, defValue);
							}
						}
						res.set(_data_col, co.getColumnDefaultValue(row));
					}
				}
			}
			/* int count =*/ pstmt.executeUpdate();
			pstmt.close();
			if (!getTableName().contains("trag_aktivnosti") && keyName != null
					&& keyName.length() > 0) {
				pstmt = jco.prepareStatement("SELECT MAX(" + keyName
						+ ") FROM " + getTableName() + " AS " + keyName);
				ResultSet rs = pstmt.executeQuery();
				AS2RecordList loc_rs = transformResultSetOneRow(rs);
				pstmt.close();
				res.set(keyName, new AS2Record(loc_rs).get(""));
				// res =daoLoad(new J2EEValueObject(loc_rs));
			}

		} catch (AS2Exception e) {
			throw e;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
		return res;
	}
	
	public AS2RecordList daoLoad(AS2Record data) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		AS2RecordList as2_rs = null;
		try {
			prepareData(data, getTableName());
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = prepareSqlLoad(data, getTableName());
			PreparedStatement pstmt = jco.prepareStatement(sql);

			AS2RecordList keysInfo = co.getPrimaryKeysInfo(getTableName());
			ArrayList<AS2Record> keys = keysInfo.getRows();
			int i = 0;
			for (AS2Record model : keys) {
				LinkedHashMap<String, Object> row = model.getProperties();

				String key = co.getColumnName(row);
				Object value = data.getAsObject(key);
				if (value != null) {
					pstmt.setObject(++i, value);
				} else {
					if (co.getColumnIsNullable(row)) {
						int sqlType = co.getColumnDataType(row);
						pstmt.setNull(++i, sqlType);
					} else {
						pstmt.setObject(++i, co.getColumnDefaultValue(row));
					}
				}
			}
			pstmt.setMaxRows(1);
			ResultSet rs = pstmt.executeQuery();
			as2_rs = transformResultSetOneRow(rs);
			pstmt.close();
			return as2_rs;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public AS2RecordList daoLoadNext(AS2Record data) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public void daoRemove(AS2Record data) throws AS2Exception {
		J2EEConnectionJDBC co = null;

		try {
			prepareData(data, getTableName());
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = prepareSqlRemove(data, getTableName());
			PreparedStatement pstmt = jco.prepareStatement(sql);
			AS2RecordList keysInfo = co.getPrimaryKeysInfo(getTableName());
			ArrayList<AS2Record> keys = keysInfo.getRows();
			int i = 0;
			for (AS2Record model : keys) {
				LinkedHashMap<String, Object> row = model.getProperties();

				String key = co.getColumnName(row);
				Object value = data.getAsObject(key);
				if (value != null) {
					pstmt.setObject(++i, value);
				} else {
					if (co.getColumnIsNullable(row)) {
						int sqlType = co.getColumnDataType(row);
						pstmt.setNull(++i, sqlType);
					} else {
						pstmt.setObject(++i, co.getColumnDefaultValue(row));
					}
				}
			}
			int updateCounter = pstmt.executeUpdate();
			if (updateCounter != 1) {
				AS2Exception e = new AS2Exception("162");
				e.setSeverity(updateCounter);
				throw e;
			}
			pstmt.close();
		} catch (AS2Exception e) {
			throw e;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}
	
	public AS2Record daoStore(AS2Record data) throws AS2Exception {
		AS2Record res = new AS2Record();
		J2EEConnectionJDBC co = null;
        boolean hasChanged = false;
		try {
			prepareData(data, getTableName());
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = prepareSqlStore(data, getTableName());
			PreparedStatement pstmt = jco.prepareStatement(sql);
			AS2RecordList columnsInfo = co.getColumnsInfo(getTableName());
			ArrayList<AS2Record> columns = columnsInfo.getRows();
			// validate length
			validateFieldLength(columnsInfo, data);
			// start to do update
			LinkedHashMap<String, Object> hashKeys = null;
			if (!isKeysUpdateable())
				hashKeys = co.getPrimaryKeysInHashtable(getTableName());
			else
				hashKeys = null;
			int i = 0;
			for (AS2Record model : columns) {
				LinkedHashMap<String, Object> row = model.getProperties();
				String column = co.getColumnName(row);// .toLowerCase();
				// if key is updateable or if the column is not a key
				if ((hashKeys == null)
						|| ((hashKeys != null) && (hashKeys.get(column) == null))) {
					// if(!((String)row.get("type_name")).endsWith("identity")){
					Object value = data.getAsObject(column);
					// only updates those columns passed in data
					if (value != null) {
						hasChanged=true;
						int columnSqlType = co.getColumnDataType(row);
						switch (columnSqlType) {
						case Types.DECIMAL:
						case Types.LONGVARBINARY:
						case Types.INTEGER:
						case Types.SMALLINT:
						case Types.TIMESTAMP:
						case Types.DATE:
							if (value.toString().trim().length() == 0) {
								pstmt.setNull(++i, columnSqlType);
								break;
							}
						// case Types.TIMESTAMP:
						// if (value.toString().trim().length()==0){
						// pstmt.setNull(++i,Types.TIMESTAMP);
						// break;
						// }
						// case Types.DATE:
						// if (value.toString().trim().length()==0){
						// pstmt.setNull(++i, Types.DATE);
						// break;
						// }
						default:
							if (value.toString().trim().equals(""))
								pstmt.setNull(++i, columnSqlType);
							else
								pstmt.setObject(++i, value);
						}
						// ********************
						// pstmt.setObject(++i, value);
						res.set(column, value);
					}
					// }
				}
			}
			AS2RecordList keysInfo = co.getPrimaryKeysInfo(getTableName());
			ArrayList<AS2Record> keys = keysInfo.getRows();
			for (AS2Record model : keys) {
				LinkedHashMap<String, Object> row = model.getProperties();

				String key = co.getColumnName(row);
				Object value = data.getAsObject(key);
				if (value != null) {
					pstmt.setObject(++i, value);
					res.set(key, value);
				} else {
					throw new Exception("Not all key values are specified.");
				}
			}
			if(hasChanged){
				int updateCounter = pstmt.executeUpdate();
				if (updateCounter > 1)
					throw new AS2Exception(" 162");
			}
			pstmt.close();
		} catch (AS2Exception e) {
			throw e;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
		return res;
	}
	
	/* FIND operations */
	public AS2RecordList daoFind(AS2Record data) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		AS2RecordList as2_rs = null;

		try {//
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = prepareSqlFind(data, getTableName());
			PreparedStatement pstmt = jco.prepareStatement(sql);
			pstmt.setMaxRows(co.getMaxRows());
			ResultSet rs = pstmt.executeQuery();
			as2_rs = transformResultSet(rs);
			pstmt.close();
			return as2_rs;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public AS2RecordList daoFindFromView(AS2Record data, String view) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		AS2RecordList as2_rs = null;

		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = prepareSqlFind(data, view);
			PreparedStatement pstmt = jco.prepareStatement(sql);
			pstmt.setMaxRows(co.getMaxRows());
			ResultSet rs = pstmt.executeQuery();
			as2_rs = transformResultSet(rs);
			pstmt.close();
			return as2_rs;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	/* EXECUTE operations */
	public AS2RecordList daoCallStoredProcedureQuery(AS2Record data, String sp_name) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		AS2RecordList as2_rs = null;
		StringBuffer sp = new StringBuffer();
		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			sp.append("{call ");
			sp.append(sp_name);
			sp.append("}");
			CallableStatement cs = jco.prepareCall(sp.toString());
			ResultSet rs = cs.executeQuery();
			as2_rs = transformResultSet(rs);
			cs.close();
			return as2_rs;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public AS2RecordList daoCallStoredProcedure(AS2Record data,	String sp_name) throws AS2Exception {
		AS2RecordList as2_rs = null;
		J2EEConnectionJDBC co = null;
		StringBuffer sp = new StringBuffer();

		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			sp.append("{call ");
			sp.append(sp_name);
			sp.append("}");
			CallableStatement cs = jco.prepareCall(sp.toString());
			// here to use input parameters (data)
			boolean res = cs.execute();
			if (res) {
				ResultSet rs = cs.getResultSet();
				as2_rs = transformResultSet(rs);
				int rs_count = 1;
				while (((cs.getMoreResults() == true) || (cs.getUpdateCount() != -1))) {
					rs = cs.getResultSet();
					AS2RecordList rs_tmp = transformResultSet(rs);
					as2_rs.addResultSet("@@" + rs_count++, rs_tmp);
				}
			} else {
				// find out the
				// getUpdateCount
			}
			cs.close();
			return as2_rs;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public void daoTruncate() throws AS2Exception {
		J2EEConnectionJDBC co = null;

		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			PreparedStatement pstmt = jco.prepareStatement("truncate table " + getTableName());
			pstmt.setMaxRows(co.getMaxRows());
			pstmt.execute();
			pstmt.close();
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}
	public void daoExecuteSQL(String sql) throws AS2Exception {
		J2EEConnectionJDBC co = null;

		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			Statement stmt = jco.createStatement();
			/* boolean rs =*/ stmt.execute(sql);
			stmt.close();
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public AS2RecordList daoExecuteSimpleSQL(String sql) throws AS2Exception {
			J2EEConnectionJDBC co = null;
		AS2RecordList as2_rs = new AS2RecordList();

		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			PreparedStatement pstmt = jco.prepareStatement(sql);
			pstmt.setMaxRows(co.getMaxRows());
			ResultSet rs = pstmt.executeQuery();
			as2_rs = transformResultSetOneRow(rs);
			pstmt.close();
			return as2_rs;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public AS2RecordList daoExecuteQuery(String sql) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		AS2RecordList as2_rs = new AS2RecordList();

		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			PreparedStatement pstmt = jco.prepareStatement(sql);
			pstmt.setMaxRows(co.getMaxRows());
			ResultSet rs = pstmt.executeQuery();
			as2_rs = transformResultSet(rs);
			pstmt.close();
			return as2_rs;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public AS2RecordList daoExecuteQuery(AS2Record data) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		AS2RecordList as2_rs = null;

		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = data.getAsStringOrBlank(QUERY);
			sql = AS2Format.stringReplace(sql, SCHEMA, co.getDbSchema(), true);
			sql = AS2Format.stringReplace(sql, SCHEMA.toLowerCase(),co.getDbSchema(), true);
			PreparedStatement pstmt = jco.prepareStatement(sql);
			pstmt.setMaxRows(co.getMaxRows());
			ResultSet rs = pstmt.executeQuery();
			as2_rs = transformResultSet(rs);
			pstmt.close();
			return as2_rs;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	/* DAO operations */
	public J2EEConnectionJDBC getConnection() {
		try {
			return (J2EEConnectionJDBC) J2EEDefaultJDBCService.getInstance().getConnection();
		} catch (Exception e) {
			//TODO J2EETrace.trace(J2EETrace.E, e);
		}
		return null;
	}
	
	public J2EEKeyFactory getKeyFactory() {
		return J2EEDefaultKeyFactory.getInstance();
	}

	public java.lang.String getTableName() {
		return _tableName;
	}

	public boolean isKeysUpdateable() {
		return _keysUpdateable;
	}
	/* SET and GET operations */
	public void setKeysUpdateable(boolean value) {
		_keysUpdateable = value;
	}

	public void setTableName(java.lang.String newTableName) {
		_tableName = newTableName;
	}

	/* JDBC operations */
	/**
     * Used to convert java.sql.ResulSet to AS2RecordList.  
     * It does not support table with composite key yet. 
     * Future release will have [] instead of int.
     */
    public static AS2RecordList transformResultSet(ResultSet rs) { 
    	AS2RecordList as2_rs = AS2ValueListHandler.getInstance().handleResultSet(rs);
        if(as2_rs!=null)
            return as2_rs;
        return AS2ResultSetUtilityJdbc.transformResultSet(rs);
    }
    /**
     * Used to convert java.sql.ResulSet to J2EEResultSet. It is used for single
     * row result queries.
     */
    public static AS2RecordList transformResultSetOneRow(ResultSet rs) {
    	return AS2ResultSetUtilityJdbc.transformResultSetOneRow(rs);
    }
	public ArrayList<String> daoFindColumnNames() throws AS2Exception {
		J2EEConnectionJDBC co = null;
		try {
			co = getConnection();
			return co.getColumns(getTableName());
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}
	/* PREPARE JDBC operations */
	protected AS2Record prepareData(AS2Record data, String tableName) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		try {
			// data.clearEmptyFields();
			co = getConnection();
			AS2RecordList columnsInfo = co.getColumnsInfo(tableName);
			ArrayList<AS2Record> columns = columnsInfo.getRows();
			for (AS2Record model : columns) {
				try {
					LinkedHashMap<String, Object> row = model.getProperties();
					String column = co.getColumnName(row);
					Object value = data.getAsObject(column);
					int columnSqlType = co.getColumnDataType(row);
					switch (columnSqlType) {
					case Types.DATE:
						if (!(value instanceof java.sql.Date)) {
							data.set(column, data.getAsSqlDate(column));
						}
						break;
					case Types.TIMESTAMP:
						//TODO test
						//if (!(value instanceof java.sql.Timestamp)) {
						//	 data.set(column, data.getAsSqlTimestamp(column));
						//}
						break;
					default:
						break;
					}
				} catch (Exception ex) {
					System.out.println(ex);
				}
			}
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
		return data;
	}
	public String prepareSqlCreate(AS2Record data, String table) throws  AS2Exception {
		// get from cache
		String cacheKey = CREATE + table;
		//String sql = (String) sqlStatementCache.get(cacheKey);
		//TODO ispod
		// if (sql != null)
		// return sql;

		StringBuffer sqlBuffer = new StringBuffer();

		try {

			J2EEConnectionJDBC mConnection = getConnection();
			LinkedHashMap<String, Object> identiyColumns = mConnection
					.getIdentiyColumns(table);
			int identityCounter = identiyColumns.size();
			sqlBuffer.append("insert into ");
			sqlBuffer.append(mConnection.getDbName());
			sqlBuffer.append(mConnection.getDbSchema());
			sqlBuffer.append(table);
			sqlBuffer.append(" ( ");
			ArrayList<String> columns = mConnection.getColumns(table);
			int i = 1;
			for (String aName : columns) {
				if (identiyColumns.get(aName) != null)
					continue;
				if (i == 1) {
					sqlBuffer.append(aName);
				} else {
					sqlBuffer.append(",");
					sqlBuffer.append(aName);
				}
				i++;
			}
			sqlBuffer.append(") values (");

			int j = 1;
			for (String aName : columns) {
				if (identiyColumns.get(aName) != null)
					continue;
				if (j == columns.size() - identityCounter)
					sqlBuffer.append("?)");
				else
					sqlBuffer.append("?,");
				j++;
			}
			// add to cache
			_sqlStatementCache.put(cacheKey, sqlBuffer.toString());
			return sqlBuffer.toString();
		} catch (Exception e) {
			throw new AS2DataAccessException("4001");
		}
	}

	public String prepareSqlFind(AS2Record data, String table) throws AS2Exception {
		// get from cache
		String orderByClause = data.getAsStringOrNull(AS2Constants.ORDER_BY_CLAUSE);
		String selectClause = data.getAsStringOrNull(AS2Constants.SELECT_CLAUSE);
		AS2Record criteria = data.getAsJ2EEValueObjectOrNull(AS2Constants.FIND_CRITERIA);
		StringBuffer cacheKey = new StringBuffer();
		// use cache only if criteria is null
		if (criteria == null) {
			cacheKey.append(FIND);
			cacheKey.append(table);
			if (selectClause != null)
				cacheKey.append(selectClause);
			if (orderByClause != null)
				cacheKey.append(orderByClause);

			String sql = (String) _sqlStatementCache.get(cacheKey.toString());

			if (sql != null)
				return sql;
		}

		StringBuffer sqlBuffer = new StringBuffer();
		try {

			J2EEConnectionJDBC mConnection = getConnection();
			sqlBuffer.append("select ");
			if (selectClause == null)
				sqlBuffer.append("*");
			else
				sqlBuffer.append(selectClause);
			sqlBuffer.append(" from ");
			sqlBuffer.append(mConnection.getDbName());
			sqlBuffer.append(mConnection.getDbSchema());
			sqlBuffer.append(table);

			if (criteria != null) {
				sqlBuffer.append(" where ");

				boolean firstTimeInLoop = true;
				for (String aName : criteria.keys()) {
					Object aValue = criteria.get(aName);
					if (aValue instanceof String) {
						if (firstTimeInLoop) {
							firstTimeInLoop = false;
						} else {
							sqlBuffer.append(" and ");
						}
						sqlBuffer.append(aName);
						sqlBuffer.append(" = '");
						sqlBuffer.append(aValue);
						sqlBuffer.append("'");
					}
				}
			}
			if (orderByClause != null) {
				sqlBuffer.append(" ");
				sqlBuffer.append(orderByClause);
			}

			if (criteria == null) {
				// add to cache
				_sqlStatementCache.put(cacheKey.toString(), sqlBuffer.toString());
			}
			return sqlBuffer.toString();
		} catch (Exception e) {
			throw new AS2DataAccessException("4001");
		}
	}

	public String prepareSqlFindAllKeys(AS2Record data, String table) throws AS2Exception {
		// get from cache
		String cacheKey = FIND_ALL_KEYS + table;
		String sql = (String) _sqlStatementCache.get(cacheKey);

		if (sql != null)
			return sql;

		StringBuffer sqlBuffer = new StringBuffer();
		try {
			sqlBuffer.append("select ");
			J2EEConnectionJDBC mConnection = getConnection();

			ArrayList<String> keys = mConnection.getPrimaryKeys(table);
			int i = 1;
			for (String aName : keys) {
				if (i == 1) {
					sqlBuffer.append(aName);
				} else {
					sqlBuffer.append(", ");
					sqlBuffer.append(aName);
				}
				i++;
			}

			sqlBuffer.append(" from ");
			sqlBuffer.append(mConnection.getDbName());
			sqlBuffer.append(mConnection.getDbSchema());
			sqlBuffer.append(table);
			sqlBuffer.append(" ");

			// add to cache
			_sqlStatementCache.put(cacheKey, sqlBuffer.toString());
			return sqlBuffer.toString();
		} catch (Exception e) {
			throw new AS2DataAccessException("4001");
		}
	}

	public String prepareSqlLoad(AS2Record data, String table)	throws AS2Exception {
		// get from cache
		String selectClause = data.getAsStringOrNull(AS2Constants.SELECT_CLAUSE);
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append(LOAD);
		cacheKey.append(table);
		if (selectClause != null)
			cacheKey.append(selectClause);

		String sql = (String) _sqlStatementCache.get(cacheKey.toString());

		if (sql != null)
			return sql;

		StringBuffer sqlBuffer = new StringBuffer();
		try {

			J2EEConnectionJDBC mConnection = getConnection();
			sqlBuffer.append("select ");
			if (selectClause == null)
				sqlBuffer.append("*");
			else
				sqlBuffer.append(selectClause);
			sqlBuffer.append(" from ");
			sqlBuffer.append(mConnection.getDbName());
			sqlBuffer.append(mConnection.getDbSchema());
			sqlBuffer.append(table);
			sqlBuffer.append(" where ");
			ArrayList<String> keys = mConnection.getPrimaryKeys(table);
			int i = 1;
			for (String aName : keys) {
				if (i == 1) {
					sqlBuffer.append(aName);
					sqlBuffer.append(" = ? ");
				} else {
					sqlBuffer.append(" and ");
					sqlBuffer.append(aName);
					sqlBuffer.append(" = ? ");
				}
				i++;
			}
			// add to cache
			_sqlStatementCache.put(cacheKey.toString(), sqlBuffer.toString());
			return sqlBuffer.toString();
		} catch (Exception e) {
			throw new AS2DataAccessException("4001");
		}
	}

	public String prepareSqlRemove(AS2Record data, String table) throws AS2Exception {
		// get from cache
		String cacheKey = REMOVE + table;
		String sql = (String) _sqlStatementCache.get(cacheKey);

		if (sql != null)
			return sql;

		StringBuffer sqlBuffer = new StringBuffer();
		try {

			J2EEConnectionJDBC mConnection = getConnection();
			sqlBuffer.append("delete from ");
			sqlBuffer.append(mConnection.getDbName());
			sqlBuffer.append(mConnection.getDbSchema());
			sqlBuffer.append(table);
			sqlBuffer.append(" where ");
			ArrayList<String> keys = mConnection.getPrimaryKeys(table);
			int i = 1;
			for (String aName : keys) {
				if (i == 1) {
					sqlBuffer.append(aName);
					sqlBuffer.append(" = ? ");
				} else {
					sqlBuffer.append(" and ");
					sqlBuffer.append(aName);
					sqlBuffer.append(" = ? ");
				}
				i++;
			}

			// add to cache
			_sqlStatementCache.put(cacheKey, sqlBuffer.toString());
			return sqlBuffer.toString();
		} catch (Exception e) {
			throw new AS2DataAccessException("4001");
		}
	}

	public String prepareSqlStore(AS2Record data, String table) throws AS2Exception {
		// get from cache
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append(STORE);
		cacheKey.append(table);
		cacheKey.append(data.getProperties());
		String sql = (String) _sqlStatementCache.get(cacheKey.toString());

		if (sql != null)
			return sql;

		StringBuffer sqlBuffer = new StringBuffer();
		try {

			J2EEConnectionJDBC mConnection = getConnection();
			sqlBuffer.append("update ");
			sqlBuffer.append(mConnection.getDbName());
			sqlBuffer.append(mConnection.getDbSchema());
			sqlBuffer.append(table);
			sqlBuffer.append(" set ");
			ArrayList<String> columns = mConnection.getColumns(table);
			LinkedHashMap<String, Object> hashKeys = null;
			if (!isKeysUpdateable())
				hashKeys = mConnection.getPrimaryKeysInHashtable(table);
			else
				hashKeys = null;
			int i = 1;
			for (String column : columns) {
				// if key is updateable or if the column is not a key
				if ((hashKeys == null)
						|| ((hashKeys != null) && (hashKeys.get(column) == null))) {
					Object value = data.getAsObject(column);
					// only updates those columns passed in data
					if (value != null) {
						if (i == 1) {
							sqlBuffer.append(column);
							sqlBuffer.append(" = ? ");
						} else {
							sqlBuffer.append(", ");
							sqlBuffer.append(column);
							sqlBuffer.append(" = ? ");
						}
						i++;
					}
				}
			}
			sqlBuffer.append(" where ( ");

			ArrayList<String> keys = mConnection.getPrimaryKeys(table);
			int j = 1;
			for (String aName : keys) {
				if (j == 1) {
					sqlBuffer.append(aName);
					sqlBuffer.append(" = ? ");
				} else {
					sqlBuffer.append(" and ");
					sqlBuffer.append(aName);
					sqlBuffer.append(" = ? ");
				}
				j++;
			}
			sqlBuffer.append(")");

			// add to cache
			_sqlStatementCache.put(cacheKey.toString(), sqlBuffer.toString());
			return sqlBuffer.toString();
		} catch (Exception e) {
			throw new AS2DataAccessException("4001");
		}
	}

	/* VALIDATION operations */
	private void validateFieldLength(AS2RecordList meta_data, AS2Record data) {
		J2EEConnectionJDBC co = null;
		String column_name;
		LinkedHashMap<String, Object> column_row;
		Object value;

		co = getConnection();
		if (meta_data == null)
			meta_data = co.getColumnsInfo(getTableName());
		ArrayList<AS2Record> columns = meta_data.getRows();

		for (AS2Record original_row : columns) {
			column_row = original_row.getProperties();
			column_name = co.getColumnName(column_row).toLowerCase();
			value = data.getAsObject(column_name);
			if (value != null) {
				if(value.toString().equals(""))
					continue;//ignore null from client
				int size = co.isColumnSizeValid(column_row, value.toString());
				if (size > 0) {
					AS2DataAccessException ex = new AS2DataAccessException("900");
					ex.setFieldOne(column_name);
					ex.setFieldTwo(size + "");
					ex.setReadyForClient(true); // go back to client
					throw ex;
				}
			}
		}
	}
	
	/* JOBS operations */
	/**
	 * One option is to receive ID to fined email address.
	 */
	public void daoSendEmail(AS2Record value) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		StringBuffer sp = new StringBuffer();

		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			// sp.append("exec msdb.dbo.sp_send_dbmail ");
			sp.append("exec cmdb_prod.dbo.sp_j2ee_mail ");
			if (value.get(AS2EmailConstants.IZBOR).matches("1"))
				sp.append(" @recipients = '"+ value.get(AS2EmailConstants.RECIPIENTS) + "',");
			sp.append(" @body = '" + value.get(AS2EmailConstants.BODY) + "'");
			sp.append(", @subject = '" + value.get(AS2EmailConstants.SUBJECT)+ "'");
			sp.append(", @profile_name = '"	+ value.get(AS2EmailConstants.PROFILE_NAME) + "'");
			if (value.get(AS2EmailConstants.IZBOR).matches("2"))
				sp.append(", @org_jedinica = '"	+ value.get(AS2EmailConstants.ORG_JEDINICA_RADA) + "'");
			if (value.get(AS2EmailConstants.IZBOR).matches("3"))
				sp.append(", @grupa = '" + value.get(AS2EmailConstants.GRUPA)+ "'");
			if (value.get(AS2EmailConstants.IZBOR).matches("4"))															
				sp.append(", @to_jmbg = '" + value.get("@@to_jmbg") + "'");
			// ako se promijeni naziv grupe obavezno provjeriti 'store
			// proceduru'
			CallableStatement cs = jco.prepareCall(sp.toString());
			/* boolean res =*/  cs.execute();
			cs.close();
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	// TODO passing sql in value objects
	public int[] daoBatch(AS2Record value) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			Statement stmt = jco.createStatement();
			stmt.addBatch("UPDATE ABC SET name = 111");
			stmt.addBatch("UPDATE ABC SET name = 222");
			stmt.addBatch("UPDATE ABC SET name = 333");
			stmt.addBatch("UPDATE ABC SET name = 444");
			int[] ret = stmt.executeBatch();
			stmt.close();
			return ret;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public boolean daoStartSQLJob(String job_name) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		StringBuffer sp = new StringBuffer();
		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			sp.append("{call ");
			sp.append(" dbo.stp_j2ee_start_job (?,?) ");
			sp.append(" }");
			CallableStatement cs = jco.prepareCall(sp.toString());
			cs.setString(1, job_name);
			cs.setInt(2, 1);// @maxwaitmins je 1
			cs.executeQuery();
			cs.close();
			return true;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public boolean daoStopSQLJob(String job_name) throws AS2Exception {
		J2EEConnectionJDBC co = null;
		StringBuffer sp = new StringBuffer();
		try {
			co = getConnection();
			Connection jco = co.getJdbcConnection();
			sp.append("{call ");
			sp.append(" msdb.dbo.sp_stop_job (?) ");
			sp.append(" }");
			CallableStatement cs = jco.prepareCall(sp.toString());
			cs.setString(1, job_name);
			boolean ret = cs.execute();
			cs.close();
			return ret;
		} catch (Exception e) {
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}
}
