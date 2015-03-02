package hr.as2.inf.server.transaction.compensation.da.jdbc;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.transaction.compensation.dto.AS2TransactionRs;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.jdbc.AS2ResultSetUtilityJdbc;
import hr.as2.inf.server.da.jdbc.J2EEJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class AS2TransactionStepJdbc extends J2EEJdbc {
    public AS2TransactionStepJdbc() {
        setTableName("j2ee_transaction_step");
    }
    public AS2RecordList daoFindForRollback(AS2Record value) throws AS2DataAccessException {
        String SQL_FIND_FOR_ROLLBACK = "SELECT * FROM j2ee_transaction_step WHERE (transaction_id = ?) ORDER BY step_id DESC";
        J2EEConnectionJDBC co = null;
        AS2TransactionRs j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND_FOR_ROLLBACK);
            pstmt.setString(1, value.get("transaction_id"));
            pstmt.setMaxRows(0);
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2TransactionRs(AS2ResultSetUtilityJdbc.transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e);
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public AS2RecordList daoFindForCommit(AS2Record value) throws AS2DataAccessException {
        String SQL_FIND_FOR_COMMIT = "SELECT * FROM j2ee_transaction_step WHERE (transaction_id = ?) ORDER BY step_id"; //ASC
        J2EEConnectionJDBC co = null;
        AS2TransactionRs j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND_FOR_COMMIT);
            pstmt.setString(1, value.get("transaction_id"));
            pstmt.setMaxRows(0);
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2TransactionRs(AS2ResultSetUtilityJdbc.transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e);
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public boolean daoFindIfExists(AS2Record value) throws AS2DataAccessException {
        String SQL_FIND_IF_EXIST = "SELECT * FROM j2ee_transaction_step WHERE (transaction_id = ?) ";
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND_IF_EXIST);
            /* aplikacija */
            //AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
            pstmt.setString(1, value.get("transaction_id"));
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