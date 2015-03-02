package hr.as2.inf.server.security.authorization.da.jdbc;

import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.common.types.AS2Date;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.jdbc.J2EEJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class J2EEPrijavaKorisnikaJdbc extends J2EEJdbc {
    public static final String SESSION_VIEW = "SELECT * FROM rbac_prijava_korisnika where id_korisnika = ?";
    public static final String SQL_FIND_LAST = "SELECT MAX(id_prijave_korisnika) AS id_prijave_korisnika FROM rbac_prijava_korisnika WHERE (isnull(ispravno,'1') = '1') ";
    
    public J2EEPrijavaKorisnikaJdbc() {
        setTableName("rbac_prijava_korisnika");
    }

    public void daoLogIn(AS2User value) throws AS2DataAccessException {
        try {  
            value.set("vrijeme_prijave",AS2Date.getCurrentTime().toString());
            daoCreate(value);
            value.set("id_prijave_korisnika",daoFindLast());
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public String daoFindLast() throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND_LAST);
            pstmt.setMaxRows(1);
            ResultSet rs = pstmt.executeQuery();
            j2eers = transformResultSetOneRow(rs);
            pstmt.close();
            return j2eers.get("id_prijave_korisnika"); 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public AS2User daoLogOut(AS2User value) throws AS2DataAccessException {
//        J2EEConnectionJDBC co = null;
//        StringBuffer sql = new StringBuffer();
        return value;
        /*try {            
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            //TODO id_prijave_korisnika
            sql.append("update rbac_prijava_korisnika set ispravno = 0, vrijeme_odjave = ? where id_korisnika = ? AND id_prijave_korisnika = ?");
            PreparedStatement pstmt = jco.prepareStatement(sql.toString());
            pstmt.setTimestamp(1, AS2Date.getCurrentTime());
            pstmt.setString(2, value.get("id_korisnika"));
            pstmt.setString(3, value.get("id_prijave_korisnika"));//session id
            pstmt.setMaxRows(0);
            pstmt.executeUpdate();
            pstmt.close();
            return value;
        } catch (Exception e) {
            J2EEDataAccessException ex = new J2EEDataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }*/
    }
}