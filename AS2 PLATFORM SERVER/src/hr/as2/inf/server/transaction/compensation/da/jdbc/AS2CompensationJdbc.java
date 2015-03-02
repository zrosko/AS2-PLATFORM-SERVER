package hr.as2.inf.server.transaction.compensation.da.jdbc;

import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.transaction.compensation.dto.AS2CompensationRs;
import hr.as2.inf.common.transaction.compensation.dto.AS2CompensationVo;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.jdbc.AS2ResultSetUtilityJdbc;
import hr.as2.inf.server.da.jdbc.J2EEJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class AS2CompensationJdbc extends J2EEJdbc {
    private String SQL_FIND = "SELECT * FROM j2ee_compensation ORDER BY transaction_id DESC";
    private String SQL_FIND_IF_EXIST = "SELECT * FROM j2ee_compensation WHERE (transaction_id = ?) ";
    
    public AS2CompensationJdbc() {
        setTableName("j2ee_compensation");
    }
    public AS2CompensationRs daoReadAll(AS2CompensationVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2CompensationRs j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND);
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2CompensationRs(AS2ResultSetUtilityJdbc.transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e);
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public AS2CompensationRs daoFind(AS2CompensationVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2CompensationRs j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND);
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2CompensationRs(AS2ResultSetUtilityJdbc.transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e);
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public boolean daoFindIfExists(AS2CompensationVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND_IF_EXIST);
            /* aplikacija */
            //AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
            pstmt.setString(1, value.getTransactionId());
            pstmt.setMaxRows(1);
            ResultSet rs = pstmt.executeQuery();
            j2eers = AS2ResultSetUtilityJdbc.transformResultSet(rs);
            pstmt.close();
            if (j2eers.size()>0)
                return true;
            return false; 
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e);
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
  }