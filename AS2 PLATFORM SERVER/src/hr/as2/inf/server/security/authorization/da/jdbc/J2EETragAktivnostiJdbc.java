package hr.as2.inf.server.security.authorization.da.jdbc;

import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.jdbc.J2EEJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class J2EETragAktivnostiJdbc extends J2EEJdbc{//J2EEDataAccessObjectJdbc {
    public static final String LOG_VIEW = "SELECT * FROM rbac_trag_aktivnosti where id_prijave_korisnika = ?";
    
    public J2EETragAktivnostiJdbc() {
        setTableName("rbac_trag_aktivnosti");
    }
    //TODO ne koristi se jos
    public AS2RecordList daoFindLogForToday() throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;
        StringBuffer sql = new StringBuffer();

        try {
            sql.append("SELECT K.korisnik, P.aplikacija, T.server_komponenta, T.server_servis, T.sadrzaj, P.id_prijave_korisnika, ");
            sql.append("P.id_korisnika, P.vrijeme_prijave, P.vrijeme_odjave,P.timeout, P.ispravno, K.id_osobe, K.zaporka,");
            sql.append("K.zadana_uloga, K.vrijedi_od, K.vrijedi_do, K.opis, T.id_traga_aktivnosti, T.aktivnost,T.vrijeme_aktivnosti ");
            sql.append("FROM  rbac_trag_aktivnosti AS T LEFT OUTER JOIN ");
            sql.append("rbac_prijava_korisnika AS P ON P.id_prijave_korisnika = T.id_prijave_korisnika LEFT OUTER JOIN ");
            sql.append("rbac_korisnik AS K ON K.id_korisnika = P.id_korisnika ");
            sql.append("WHERE (DAY(T.vrijeme_aktivnosti) = DAY(GETDATE())) AND (MONTH(T.vrijeme_aktivnosti) = MONTH(GETDATE())) ");
            sql.append("ORDER BY T.vrijeme_aktivnosti");
 
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(sql.toString());
            pstmt.setMaxRows(2000);
            ResultSet rs = pstmt.executeQuery();
            j2eers = transformResultSetOneRow(rs);
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
}