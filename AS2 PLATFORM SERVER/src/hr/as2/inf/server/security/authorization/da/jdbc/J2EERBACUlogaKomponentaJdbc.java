package hr.as2.inf.server.security.authorization.da.jdbc;

import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.security.dto.AS2KomponentaVo;
import hr.as2.inf.common.security.dto.AS2UlogaKomponentaRs;
import hr.as2.inf.common.security.dto.AS2UlogaKomponentaVo;
import hr.as2.inf.common.security.dto.AS2UlogaVo;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.jdbc.J2EEJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class J2EERBACUlogaKomponentaJdbc extends J2EEJdbc{//J2EEDataAccessObjectJdbc {
    private String SQL_FIND = "SELECT K.komponenta, UK.akcije, UK.izvjesca, UK.id_uloge, UK.id_komponente, UK.aplikacija, UK.razina "+
    " FROM rbac_uloga_komponenta UK LEFT OUTER JOIN  rbac_komponenta K ON K.id_komponente = UK.id_komponente and K.aplikacija = ? "+
    " WHERE UK.id_uloge = ? AND UK.aplikacija = ? ";
    private String SQL_FIND_IF_EXIST = "SELECT * FROM rbac_uloga_komponenta WHERE aplikacija = ? and (id_uloge = ? and id_komponente = ?) ";

    public J2EERBACUlogaKomponentaJdbc() {
        setTableName("rbac_uloga_komponenta");
    }
    public AS2UlogaKomponentaRs daoFind(AS2UlogaKomponentaVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2UlogaKomponentaRs j2eers = null;
        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND);
            pstmt.setString(1,value.getAplikacija());
            pstmt.setObject(2,value.getIdUloge());
            pstmt.setString(3,value.getAplikacija());
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2UlogaKomponentaRs(transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public boolean daoFindIfExists(AS2UlogaKomponentaVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND_IF_EXIST);
            pstmt.setString(1, value.getAplikacija());
            pstmt.setString(2, value.getIdUloge());
            pstmt.setString(3, value.getIdKomponente());
            pstmt.setMaxRows(1);
            ResultSet rs = pstmt.executeQuery();
            j2eers = transformResultSet(rs);
            pstmt.close();
            if (j2eers.size()>0)
                return true;
            return false; 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public void daoRemoveUlogu(AS2UlogaVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement("delete from rbac_uloga_komponenta where aplikacija = ? and id_uloge =? ");
            pstmt.setString(1, value.getAplikacija());
            pstmt.setString(2, value.getIdUloge());
            pstmt.setMaxRows(0);
            pstmt.execute();
            pstmt.close();
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public void daoRemoveKomponentu(AS2KomponentaVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement("delete from rbac_uloga_komponenta where aplikacija = ? and id_komponente =? ");
            pstmt.setString(1, value.getAplikacija());
            pstmt.setString(2, value.getIdKomponente());
            pstmt.setMaxRows(0);
            pstmt.execute();
            pstmt.close();
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
 }