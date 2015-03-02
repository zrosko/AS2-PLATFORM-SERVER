package hr.as2.inf.server.security.authorization.da.jdbc;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.security.dto.AS2UlogaRs;
import hr.as2.inf.common.security.dto.AS2UlogaVo;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.jdbc.J2EEJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class J2EERBACUlogaJdbc extends J2EEJdbc{//J2EEDataAccessObjectJdbc {
    private String SQL_FIND = "SELECT uloga,opis,zadana_komponenta, ispravno,aplikacija,id_uloge FROM rbac_uloga WHERE aplikacija = ? and (isnull(ispravno,'1') = '1') ";
    private String SQL_FIND_LAST = "SELECT MAX(id_uloge)+1 AS id_uloge FROM rbac_uloga WHERE aplikacija = ? ";//and (isnull(ispravno,'1') = '1') ";
    public J2EERBACUlogaJdbc() {
        setTableName("rbac_uloga");
    }
    public AS2UlogaRs daoFind(AS2UlogaVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2UlogaRs j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND);
            AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
            pstmt.setString(1, _user.getApplication());
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2UlogaRs(transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public String daoFindLast(AS2UlogaVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND_LAST);
            pstmt.setString(1, value.getAplikacija());
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = transformResultSetOneRow(rs);
            pstmt.close();
            return j2eers.get("id_uloge"); 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
 }