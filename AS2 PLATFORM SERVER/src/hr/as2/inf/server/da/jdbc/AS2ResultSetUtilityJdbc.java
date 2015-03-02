package hr.as2.inf.server.da.jdbc;

import hr.as2.inf.common.data.AS2MetaData;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.types.AS2Date;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * The JDBC utility class. Used to service common JDBC functionality such as
 * conversion from java.sql.ResultSet to AS2RecordList.
 * The class is used by connection and data access class. 
 */
public final class AS2ResultSetUtilityJdbc {
    /**
     * Used to check if upate timestamp from DB is changed while one client was
     * retrieved the data from DB.
     */
    public static void checkSynhronization(ResultSet rs, String b)
            throws AS2DataAccessException {

        if (b == null)
            throw new AS2DataAccessException("164");
        try {
            if (rs.next()) {
                Timestamp a = rs.getTimestamp(1);
                if (a != null) {
                    if (AS2Date.isTimestampUpdated(a, b))
                        throw new AS2DataAccessException("166");
                } else
                    throw new AS2DataAccessException("153");
            }
        } catch (SQLException e) {
            throw new AS2DataAccessException("153");
        }
    }

    /**
     * Used to check if update time stamp from DB is changed while one client was
     * retrieved the data from DB.
     */
    public static void checkSynhronization(java.sql.Timestamp a, String b)
            throws AS2DataAccessException {

        if (b == null)
            throw new AS2DataAccessException("164");
        if (a != null) {
            if (AS2Date.isTimestampUpdated(a, b))
                throw new AS2DataAccessException("166");
        } else {
            throw new AS2DataAccessException("168");
        }
    }
    /**
     * Used to convert java.sql.ResulSet to AS2RecordList.  
     * It does not support table with composite key yet. 
     * Future release will have [] instead of int.
     */
    public static AS2RecordList transformResultSet(ResultSet rs) {
        AS2RecordList as2_rs = new AS2RecordList();
        try {
            if (rs != null) {//BEGIN result set
                ResultSetMetaData rsmd = rs.getMetaData();
                int numcols = rsmd.getColumnCount();
                for (int i = 1; i <= numcols; i++) {
                    AS2MetaData meta_data = new AS2MetaData(
                            rsmd.getColumnLabel(i).toLowerCase(),
                            rsmd.getColumnTypeName(i),
                            rsmd.getColumnType(i),
                            rsmd.getColumnDisplaySize(i),
                            rsmd.getPrecision(i),
                            rsmd.getScale(i),
                            i); //sequence
                    as2_rs.addMetaData(rsmd.getColumnLabel(i).toLowerCase(),meta_data);
                    as2_rs.addColumnName(rsmd.getColumnLabel(i).toLowerCase());
                    as2_rs.setColumnSize(i, rsmd.getColumnDisplaySize(i));
                }
                while (rs.next()) {                    
                    AS2Record row = new AS2Record(numcols);
                    //row.setMetaData(as2_rs.getMetaData());//copy metadata to each row
                    for (int i = 1; i <= numcols; i++) {
                        try {
                            Object obj = rs.getObject(i);

                            if (obj != null) {
                                int _type = rsmd.getColumnType(i);

                                if (_type == java.sql.Types.CLOB) {
                                    Clob _clob = (Clob) obj;
                                    String _s_clob = _clob.getSubString(1,(int) _clob.length());
                                    row.set(rsmd.getColumnLabel(i).toLowerCase(), _s_clob);
                                    //}else if(_type == java.sql.Types.TIMESTAMP){
                                    //row.put(rsmd.getColumnLabel(i).toLowerCase(),
                                    //AS2Date.formatStringDateToDDMMYYY(obj.toString().trim());
                                } else {
                                    row.set(rsmd.getColumnLabel(i).toLowerCase(), obj.toString().trim());
                                }
                            }
                        } catch (Exception e) {
                        	e.printStackTrace();
                        }
                    }
                   as2_rs.addRow(row);
                }
            }//END result set
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return as2_rs;
    }
    /**
     * Used to convert java.sql.ResulSet to J2EEResultSet. It is used for single
     * row result queries.
     */
    public static AS2RecordList transformResultSetOneRow(ResultSet rs) {
        AS2RecordList as2_rs = new AS2RecordList();
        try {
           	if (rs != null) {//BEGIN result set
                ResultSetMetaData rsmd = rs.getMetaData();
                int numcols = rsmd.getColumnCount();
                for (int i = 1; i <= numcols; i++) {
                    AS2MetaData meta_data = new AS2MetaData(
                            rsmd.getColumnLabel(i).toLowerCase(),
                            rsmd.getColumnTypeName(i),
                            rsmd.getColumnType(i),
                            rsmd.getColumnDisplaySize(i),
                            rsmd.getPrecision(i),
                            rsmd.getScale(i),
                            i); //sequence
                    as2_rs.addMetaData(rsmd.getColumnLabel(i).toLowerCase(),meta_data);
                    as2_rs.addColumnName(rsmd.getColumnLabel(i).toLowerCase());
                    as2_rs.setColumnSize(i, rsmd.getColumnDisplaySize(i));
                }

                if (rs.next()) {
                    for (int i = 1; i <= numcols; i++) {
                        try {
                            Object obj = rs.getObject(i);
                            
                            if (obj != null) {
                                int _type = rsmd.getColumnType(i);

                                if(_type == java.sql.Types.LONGVARBINARY){
                                    byte[] image_column = (byte[])obj;
                                    as2_rs.set(rsmd.getColumnLabel(i).toLowerCase(), image_column); 
                                }else if (_type == java.sql.Types.CLOB) {
                                    Clob _clob = (Clob) obj;
                                    String _s_clob = _clob.getSubString(1,(int) _clob.length());
                                    as2_rs.set(rsmd.getColumnLabel(i).toLowerCase(), _s_clob);
                                } else {
                                    as2_rs.set(rsmd.getColumnLabel(i).toLowerCase(), obj.toString().trim());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                as2_rs.setOneRowOnly(true);
            }//END result set
        } catch (Exception e) {
            e.printStackTrace();
        }
        return as2_rs;
    }
 }