package hr.as2.inf.server.security.authorization.da.jdbc;

import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.security.dto.AS2OsobaRs;
import hr.as2.inf.common.security.dto.AS2OsobaVo;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.jdbc.J2EEJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class J2EERBACOsobaJdbc extends J2EEJdbc {
    private String SQL_FIND = "select convert(decimal(15,0),id_osobe) as jmbg, ime, prezime, convert(decimal(15,0),id_osobe) as id_osobe, ispravno, ime+' '+prezime as ime_prezime from rbac_osoba where (isnull(ispravno,'1') = '1') order by ime";
    private String SQL_FIND_IF_EXIST = "SELECT id_osobe FROM rbac_osoba WHERE (id_osobe = ?) ";
   
    public J2EERBACOsobaJdbc() {
        setTableName("rbac_osoba");
    }
    public AS2OsobaRs daoFind(AS2OsobaVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2OsobaRs j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND);
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2OsobaRs(transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public boolean daoFindIfExists(AS2OsobaVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND_IF_EXIST);
            pstmt.setString(1, value.getIdOsobe());
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
 }