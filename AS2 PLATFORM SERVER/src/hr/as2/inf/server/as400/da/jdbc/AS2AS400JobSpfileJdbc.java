package hr.as2.inf.server.as400.da.jdbc;

import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.data.as400.AS2AS400SpoolFileVo;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.jdbc.AS2ResultSetUtilityJdbc;
import hr.as2.inf.server.da.jdbc.J2EEJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class AS2AS400JobSpfileJdbc extends J2EEJdbc{//J2EEDataAccessObjectJdbc {
    public AS2AS400JobSpfileJdbc() {
        setTableName("as400_job_spfile");
    }
    public AS2AS400SpoolFileVo daoLoad(AS2AS400SpoolFileVo value) throws AS2DataAccessException {
        AS2Trace.trace(AS2Trace.I, getTableName() + " daoLoad begin");
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = "SELECT * FROM as400_job_spfile ";
			sql = sql + "where job_number = ? and job_name = ? and spfile_name = ? and spfile_num = ? ";
			PreparedStatement pstmt = jco.prepareStatement(sql);
			pstmt.setObject(1,value.get("job_number"));
			pstmt.setObject(2,value.get("job_name"));
			pstmt.setObject(3,value.get("spfile_name"));
			pstmt.setObject(4,value.get("spfile_num"));
			pstmt.setMaxRows(0);
			ResultSet rs = pstmt.executeQuery();
			j2eers = AS2ResultSetUtilityJdbc.transformResultSetOneRow(rs);
			pstmt.close();
            AS2Trace.trace(AS2Trace.I, getTableName()+ " daoFindIfExists end");
        } catch (AS2DataAccessException e) {
		    throw e;
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e);
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
        return new AS2AS400SpoolFileVo(j2eers);
    }
    public boolean daoFindIfExists(AS2AS400SpoolFileVo value) throws AS2DataAccessException {
        AS2Trace.trace(AS2Trace.I, getTableName() + " daoFindIfExists begin");
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = "SELECT 1 FROM as400_job_spfile ";
			sql = sql + " where job_number = ? and job_name = ? and spfile_name = ? and spfile_num = ? ";
			PreparedStatement pstmt = jco.prepareStatement(sql);
			pstmt.setObject(1,value.get("job_number"));
			pstmt.setObject(2,value.get("job_name"));
			pstmt.setObject(3,value.get("spfile_name"));
			pstmt.setObject(4,value.get("spfile_num"));
			pstmt.setMaxRows(0);
			ResultSet rs = pstmt.executeQuery();
			j2eers = AS2ResultSetUtilityJdbc.transformResultSet(rs);
			pstmt.close();
            AS2Trace.trace(AS2Trace.I, getTableName()+ " daoFindIfExists end");
            int broj_slogova = j2eers.size();
            if(broj_slogova > 0)
                return true;
            else
                return false;
        } catch (AS2DataAccessException e) {
		    throw e;
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e);
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public String daoFindLastInserted(AS2AS400SpoolFileVo value) throws AS2DataAccessException {
        AS2Trace.trace(AS2Trace.I, getTableName() + " daoFindLastInserted begin");
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
			Connection jco = co.getJdbcConnection();
			String sql = "SELECT max(spfile_id) as spfile_id FROM as400_job_spfile ";
			sql = sql + " where job_number = ? and job_name = ? and spfile_name = ? and spfile_num = ? ";
			PreparedStatement pstmt = jco.prepareStatement(sql);
			pstmt.setObject(1,value.get("job_number"));
			pstmt.setObject(2,value.get("job_name"));
			pstmt.setObject(3,value.get("spfile_name"));
			pstmt.setObject(4,value.get("spfile_num"));
			pstmt.setMaxRows(0);
			ResultSet rs = pstmt.executeQuery();
			j2eers = AS2ResultSetUtilityJdbc.transformResultSetOneRow(rs);
			pstmt.close();
            AS2Trace.trace(AS2Trace.I, getTableName()+ " daoFindLastInserted end");
            String spfile_id = j2eers.get("spfile_id");
            return spfile_id;
        } catch (AS2DataAccessException e) {
		    throw e;
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e);
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
 }