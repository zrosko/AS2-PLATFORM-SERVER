package hr.as2.inf.server.security.authorization.da.jdbc;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.security.dto.AS2KorisnikRs;
import hr.as2.inf.common.security.dto.AS2KorisnikVo;
import hr.as2.inf.common.security.encoding.AS2Base64;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.common.types.AS2Date;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.jdbc.J2EEJdbc;
import hr.as2.inf.server.da.jdbc.J2EESqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class J2EERBACKorisnikJdbc extends J2EEJdbc {
	public static final String SQL_FIND = "SELECT O.ime + ' ' + O.prezime AS ime_prezime, K.korisnik AS sifra_korisnika, U.uloga AS uloga, "+
    " K.id_korisnika, convert(decimal(15,0),K.id_osobe) as id_osobe, K.korisnik, K.zaporka, K.zadana_uloga, K.vrijedi_od, K.vrijedi_do, K.opis, K.ispravno, "+
    " K.aplikacija FROM rbac_korisnik K LEFT OUTER JOIN rbac_osoba O ON K.id_osobe = O.id_osobe LEFT OUTER JOIN"+
    " rbac_uloga U ON K.zadana_uloga = U.id_uloge AND U.aplikacija = ? WHERE (ISNULL(K.ispravno, '1') = '1') and K.aplikacija = ?";
    public static final String SQL_FIND_IF_EXIST = "SELECT korisnik FROM rbac_korisnik WHERE (korisnik = ? and aplikacija = ?) and (ISNULL(ispravno, 1) = 1)";
    public static final String LOGIN_VIEW = "SELECT * FROM view_rbac_login where korisnik = ";
    public static final String APP_USERS_SQL = "select convert(char(15),convert(decimal(15,0),jmbg)) AS id, rtrim(ime)+' '+rtrim(prezime)AS name from CMDB_PROD.dbo.view_cmdb_djelatnik_pogled "
            +" ORDER BY ime";
    
    public J2EERBACKorisnikJdbc() {
        setTableName("rbac_korisnik");
    }
    public AS2KorisnikRs daoFind(AS2KorisnikVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2KorisnikRs j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND);
            /* aplikacija */
            AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
            pstmt.setString(1,_user.getApplication());
            pstmt.setString(2,_user.getApplication());
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2KorisnikRs(transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public boolean daoFindIfExists(AS2KorisnikVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(SQL_FIND_IF_EXIST);
            /* aplikacija */
            AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
            pstmt.setString(1, value.getKorisnik());
            pstmt.setString(2,_user.getApplication());
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
    public AS2User daoLogIn(AS2User value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;
        String _ip_address;

        try {   
            _ip_address = value.get("ip_address");
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = null;
            if(value.getUserName().equals("guest"))
            	pstmt = jco.prepareStatement(LOGIN_VIEW+"'"+value.getUserName()+"' AND aplikacija ="+"'"+value.getApplication()+"'");
            else
            	pstmt = jco.prepareStatement(LOGIN_VIEW+"'"+value.getUserName()+"' AND password ="+"'"+value.get(AS2User.PASSWORD)+"'"+" AND aplikacija ="+"'"+value.getApplication()+"'");
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = transformResultSet(rs);
            value.setProperties(null);
            if(j2eers.getRows().size()>0){
                value.setProperties(j2eers.getRowAt(0).getProperties());
                //value.setPassword(value.get(J2EEUser.PASSWORD));//encrypt
                value.set(AS2User.FUNCTIONS, j2eers);
            }
            value.set("ip_address",_ip_address);
            pstmt.close();
            
            try{
                String _aplikacija = value.getApplication();
                String _component = value.getRemoteObject();
                String _service = value.getRemoteMethod();
                String _id_uloge = value.get("role_id");
                String _id_komponente = value.get("function_id");
                String _id_osobe = value.get("id_osobe");
                String _korisnik = value.get("korisnik");
                
				J2EETragAktivnostiJdbc dao_log = new J2EETragAktivnostiJdbc();
				AS2Record vo_log = new AS2Record();
				vo_log.set("id_prijave_korisnika", value.getAsString("id_prijave_korisnika","0"));
                vo_log.set("ip_address", _ip_address);
                vo_log.set("aplikacija", _aplikacija);
                vo_log.set("korisnik", _korisnik);
                vo_log.set("id_uloge", _id_uloge);
                vo_log.set("id_komponente", _id_komponente);
                vo_log.set("id_osobe", _id_osobe);
                vo_log.set("aktivnost", "LOGIN");
                vo_log.set("vrijeme_aktivnosti", AS2Date.getCurrentTime().toString());
                vo_log.set("server_komponenta", _component);
                vo_log.set("server_servis", _service);
                vo_log.set("sadrzaj", value.toString());
			    dao_log.daoCreate(vo_log);
			}catch(Exception e){
			    System.out.println(e);
			}
			
            return value;
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }

    public AS2User daoChangePassword(AS2User value)
            throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        J2EESqlBuilder sql = new J2EESqlBuilder();
        int counter = 1;

        try {            
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            sql.append("UPDATE rbac_korisnik SET zaporka = ? where korisnik = ");
            sql.append("'"+value.getUserName()+"'");
            //zr.4.10.2011. promjerna passworda za sve a ne samo za jednu aplikaciju
            //sql.append("'"+value.getUserName()+"' AND aplikacija ="+"'"+value.getApplication()+"'");
            sql.append(" AND zaporka ="+"'"+value.get(AS2User.PASSWORD)+"'");
            PreparedStatement pstmt = jco.prepareStatement(sql.toString());
            pstmt.setString(counter++, AS2Base64.encode(value.get(AS2User.NEW_PASSWORD)));
            pstmt.setMaxRows(0);
            value.set("@@counter",pstmt.executeUpdate());
            pstmt.close();
            return value;
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public AS2KorisnikRs daoFindActiveUsers(AS2KorisnikVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2KorisnikRs j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            J2EESqlBuilder sql = new J2EESqlBuilder();
            sql.append("select oznaka_radnika AS id, rtrim(ime)+' '+rtrim(prezime)AS name ");
            sql.append("from CMDB_PROD.dbo.view_cmdb_djelatnik_pogled where oznaka_radnika in (");
            sql.append("SELECT korisnik FROM CMDB_PROD.dbo.rbac_korisnik ");
            sql.append("where ISNULL(ispravno,1) = 1");
            sql.append("and isnull(vrijedi_do,GETDATE()) >= GETDATE()");
            sql.append("and aplikacija = ? ) ");
            PreparedStatement pstmt = jco.prepareStatement(sql.toString());
            /* aplikacija */
            AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
            pstmt.setString(1,_user.getApplication());
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2KorisnikRs(transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public AS2KorisnikRs daoFindAplicationUsers(AS2KorisnikVo value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2KorisnikRs j2eers = null;

        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            J2EESqlBuilder sql = new J2EESqlBuilder();
            sql.append("select convert(char(15),convert(decimal(15,0),jmbg)) AS id, rtrim(ime)+' '+rtrim(prezime)AS name ");
            sql.append("from CMDB_PROD.dbo.view_cmdb_djelatnik_pogled where oznaka_radnika in (");
            sql.append("SELECT korisnik FROM CMDB_PROD.dbo.rbac_korisnik ");
            sql.append("where ISNULL(ispravno,1) = 1");
            sql.append("and isnull(vrijedi_do,GETDATE()) >= GETDATE()");
            sql.append("and aplikacija = ? ) ORDER BY ime");
            PreparedStatement pstmt = jco.prepareStatement(sql.toString());
            /* aplikacija */
            AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
            pstmt.setString(1,_user.getApplication());
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = new AS2KorisnikRs(transformResultSet(rs));
            pstmt.close();
            return j2eers; 
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public AS2RecordList daoFindUserApplication(AS2User value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;
        PreparedStatement pstmt = null;
        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
	        pstmt = jco.prepareStatement("select * from view_rbac_login_aplikacije where username = ? and application = ? ");
	        pstmt.setObject(1,value.get("as2_username"));
	        pstmt.setObject(2,value.get("application"));
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = transformResultSet(rs);
            pstmt.close();
            return j2eers;
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public AS2RecordList daoFindApplications(AS2User value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;
        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement("select * from view_rbac_login_aplikacije where username = ? and application not in ('portal') order by application ");
            pstmt.setObject(1,value.get("as2_username"));
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = transformResultSet(rs);
            pstmt.close();
            return j2eers;
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
    public AS2RecordList daoFindAppAuthorizations(AS2User value) throws AS2DataAccessException {
        J2EEConnectionJDBC co = null;
        AS2RecordList j2eers = null;
        try {
            co = getConnection();
            Connection jco = co.getJdbcConnection();
            PreparedStatement pstmt = jco.prepareStatement(LOGIN_VIEW+"'"+value.getUserName()+"' AND aplikacija ="+"'"+value.getApplication()+"'");
            pstmt.setMaxRows(co.getMaxRows());
            ResultSet rs = pstmt.executeQuery();
            j2eers = transformResultSet(rs);
            pstmt.close();
            return j2eers;
        } catch (Exception e) {
            AS2DataAccessException ex = new AS2DataAccessException("151");
            ex.addCauseException(e);
            throw ex;
        }
    }
}