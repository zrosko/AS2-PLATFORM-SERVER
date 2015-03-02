package hr.as2.inf.server.security.authorization.facade;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2BusinessLogicException;
import hr.as2.inf.common.security.authorization.facade.AS2AuthorizationFacade;
import hr.as2.inf.common.security.dto.AS2KomponentaRs;
import hr.as2.inf.common.security.dto.AS2KomponentaVo;
import hr.as2.inf.common.security.dto.AS2KorisnikRs;
import hr.as2.inf.common.security.dto.AS2KorisnikVo;
import hr.as2.inf.common.security.dto.AS2OsobaRs;
import hr.as2.inf.common.security.dto.AS2OsobaVo;
import hr.as2.inf.common.security.dto.AS2UlogaKomponentaRs;
import hr.as2.inf.common.security.dto.AS2UlogaKomponentaVo;
import hr.as2.inf.common.security.dto.AS2UlogaRs;
import hr.as2.inf.common.security.dto.AS2UlogaVo;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.security.authorization.da.jdbc.J2EERBACKomponentaJdbc;
import hr.as2.inf.server.security.authorization.da.jdbc.J2EERBACKorisnikJdbc;
import hr.as2.inf.server.security.authorization.da.jdbc.J2EERBACOsobaJdbc;
import hr.as2.inf.server.security.authorization.da.jdbc.J2EERBACUlogaJdbc;
import hr.as2.inf.server.security.authorization.da.jdbc.J2EERBACUlogaKomponentaJdbc;

public final class AS2AuthorizationFacadeServer implements AS2AuthorizationFacade {

    private static AS2AuthorizationFacadeServer _instance = null;

    public static AS2AuthorizationFacadeServer getInstance() {
        if (_instance == null) {
            _instance = new AS2AuthorizationFacadeServer();
        }
        return _instance;
    }
    
    private String getUserName(AS2Record value){
		AS2User user = (AS2User) value.getAsObject(AS2Constants.USER_OBJ);
		return user.getUserName();
	}

    private AS2AuthorizationFacadeServer() {
    }

    public void azurirajKomponentu(AS2KomponentaVo value) throws Exception {
        J2EERBACKomponentaJdbc dao = new J2EERBACKomponentaJdbc();
        dao.daoStore(value);        
    }

    public AS2KomponentaRs procitajSveKomponente(AS2KomponentaVo value) throws Exception {
        J2EERBACKomponentaJdbc dao = new J2EERBACKomponentaJdbc();
        return dao.daoFind(value); 
    }
    
    public AS2KomponentaRs procitajKomponenteKorisnika(AS2KomponentaVo value) throws Exception {
        J2EERBACKomponentaJdbc dao = new J2EERBACKomponentaJdbc();
        value.set("korisnik", getUserName(value));
        return dao.daoFindKomponenteKorisnika(value); 
    }

    public void dodajKomponentu(AS2KomponentaVo value) throws Exception {
        AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
        value.setAplikacija(_user.getApplication());
        J2EERBACKomponentaJdbc dao = new J2EERBACKomponentaJdbc();
        dao.daoCreate(value); 
    }

    public void brisiKomponentu(AS2KomponentaVo value) throws Exception {
        J2EERBACKomponentaJdbc dao = new J2EERBACKomponentaJdbc();
        //value.setIspravno("0");
        dao.daoRemove(value);
        J2EERBACUlogaKomponentaJdbc dao_uk = new J2EERBACUlogaKomponentaJdbc();
        dao_uk.daoRemoveKomponentu(value);
    }
    public AS2KomponentaVo citajSlijedeciIdKomponente(AS2KomponentaVo value) throws Exception {
        AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
        value.setAplikacija(_user.getApplication());
        J2EERBACKomponentaJdbc dao = new J2EERBACKomponentaJdbc();
        value.setIdKomponente(dao.daoFindLast(value));
        return value;
    }
    public void azurirajUlogu(AS2UlogaVo value) throws Exception {
        J2EERBACUlogaJdbc dao = new J2EERBACUlogaJdbc();
        dao.daoStore(value); 
    }

    public AS2UlogaRs procitajSveUloge(AS2UlogaVo value) throws Exception {
        J2EERBACUlogaJdbc dao = new J2EERBACUlogaJdbc();
        return dao.daoFind(value); 
    }

    public void dodajUlogu(AS2UlogaVo value) throws Exception {
        AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
        value.setAplikacija(_user.getApplication());
        J2EERBACUlogaJdbc dao = new J2EERBACUlogaJdbc();
        dao.daoCreate(value);
        AS2UlogaKomponentaVo vo_uk = new AS2UlogaKomponentaVo();
        vo_uk.setIdUloge(value.getIdUloge());
        vo_uk.setIdKomponente(value.getZadanaKomponenta());
        vo_uk.setAplikacija(value.getAplikacija());
        J2EERBACUlogaKomponentaJdbc dao_komponenta = new J2EERBACUlogaKomponentaJdbc();
        dao_komponenta.daoCreate(vo_uk);
    }
    public AS2UlogaVo citajSlijedeciIdUloge(AS2UlogaVo value) throws Exception {
        AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
        value.setAplikacija(_user.getApplication());
        J2EERBACUlogaJdbc dao = new J2EERBACUlogaJdbc();
        value.setIdUloge(dao.daoFindLast(value));
        return value;
    }

    public void brisiUlogu(AS2UlogaVo value) throws Exception {
        J2EERBACUlogaJdbc dao = new J2EERBACUlogaJdbc();
        //value.setIspravno("0");
        dao.daoRemove(value);
        J2EERBACUlogaKomponentaJdbc dao_uk = new J2EERBACUlogaKomponentaJdbc();
        dao_uk.daoRemoveUlogu(value);
    }

    public void azurirajUloguKomponentu(AS2UlogaKomponentaVo value) throws Exception {
        J2EERBACUlogaKomponentaJdbc dao = new J2EERBACUlogaKomponentaJdbc();
        dao.daoStore(value); 
    }

    public AS2UlogaKomponentaRs procitajSveUlogeKomponente(AS2UlogaKomponentaVo value) throws Exception {
        //za jednu ulogu
        J2EERBACUlogaKomponentaJdbc dao = new J2EERBACUlogaKomponentaJdbc();
        return dao.daoFind(value); 
    }

    public void dodajUloguKomponentu(AS2UlogaKomponentaVo value) throws Exception {
        J2EERBACUlogaKomponentaJdbc dao = new J2EERBACUlogaKomponentaJdbc();
        if (dao.daoFindIfExists(value))
	        throw new AS2BusinessLogicException("2149");
        dao.daoCreate(value);
    }

    public void brisiUloguKomponentu(AS2UlogaKomponentaVo value) throws Exception {
        J2EERBACUlogaKomponentaJdbc dao = new J2EERBACUlogaKomponentaJdbc();
        dao.daoRemove(value);
    }

    public void azurirajKorisnika(AS2KorisnikVo value) throws Exception {
        J2EERBACKorisnikJdbc dao = new J2EERBACKorisnikJdbc();
        AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
        value.setAplikacija(_user.getApplication());
        dao.daoStore(value);        
    }

    public AS2KorisnikRs procitajSveKorisnike(AS2KorisnikVo value) throws Exception {
        J2EERBACKorisnikJdbc dao = new J2EERBACKorisnikJdbc();
        AS2KorisnikRs rs = dao.daoFind(value); 
        return rs;
    }
    public AS2KorisnikRs procitajSveAktivneKorisnike(AS2KorisnikVo value) throws Exception {
        J2EERBACKorisnikJdbc dao = new J2EERBACKorisnikJdbc();
        AS2KorisnikRs rs = dao.daoFindActiveUsers(value); 
        return rs;
    }
    public AS2KorisnikRs procitajSveKorisnikeAplikacije(AS2KorisnikVo value) throws Exception {
        J2EERBACKorisnikJdbc dao = new J2EERBACKorisnikJdbc();
        AS2KorisnikRs rs = dao.daoFindAplicationUsers(value); 
        return rs;
    }
    public void dodajKorisnika(AS2KorisnikVo value) throws Exception {
        J2EERBACKorisnikJdbc dao = new J2EERBACKorisnikJdbc();
	    if (dao.daoFindIfExists(value))
	        throw new AS2BusinessLogicException("1000");
	    AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
        value.setAplikacija(_user.getApplication());
        dao.daoCreate(value);
    }

    public void brisiKorisnika(AS2KorisnikVo value) throws Exception {
        J2EERBACKorisnikJdbc dao = new J2EERBACKorisnikJdbc();
        AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
        value.setAplikacija(_user.getApplication());
        value.setIspravno("0");
        dao.daoStore(value);
    }

    public void azurirajOsobu(AS2OsobaVo value) throws Exception {
        J2EERBACOsobaJdbc dao = new J2EERBACOsobaJdbc();
        dao.daoStore(value);         
    }

    public AS2OsobaRs procitajSveOsobe(AS2OsobaVo value) throws Exception {
        J2EERBACOsobaJdbc dao = new J2EERBACOsobaJdbc();
        return dao.daoFind(value);
    }

    public void dodajOsobu(AS2OsobaVo value) throws Exception {
        J2EERBACOsobaJdbc dao = new J2EERBACOsobaJdbc();
	    if (dao.daoFindIfExists(value))
	        throw new AS2BusinessLogicException("1000");
        dao.daoCreate(value);
    }

    public void brisiOsobu(AS2OsobaVo value) throws Exception {
        J2EERBACOsobaJdbc dao = new J2EERBACOsobaJdbc();
        value.setIspravno("0");
        dao.daoStore(value); 
    }
    public AS2RecordList autorizirajKorisnika(AS2User value) throws Exception {
    	J2EERBACKorisnikJdbc _dao = new J2EERBACKorisnikJdbc();
    	return _dao.daoFindUserApplication(value);
    }
    public AS2RecordList procitajOvlastiKorisnikaZaAplikaciju(AS2User value) throws Exception {
    	J2EERBACKorisnikJdbc _dao = new J2EERBACKorisnikJdbc();
    	return _dao.daoFindAppAuthorizations(value);
    }
    //************************************************************************************
    /* NOVA SpredSheet logika samo TEST TODO move to some generic DATA SOURCE komponente */
    public AS2RecordList fetchTable(AS2Record value) throws Exception {
    	String tableName = value.get("tableName");
    	String whereSql = value.get("whereSql");
    	String sql = "select * from "+tableName+" "+whereSql;
    	J2EERBACKorisnikJdbc _dao = new J2EERBACKorisnikJdbc();
    	AS2RecordList rs = _dao.daoExecuteQuery(sql);
    	if(value.exists("@meta")){
    		AS2RecordList rs_meta = new AS2RecordList();
    		for(String column:rs.getMetaData().keySet()){
				rs_meta.addRow( rs.getMetaData(column).prepareRecord());				
			}
    		return rs_meta;
    	}
    	return rs;
    }
    public AS2RecordList addTableRow(AS2Record value) throws Exception {
    	return new AS2RecordList();
    }
    public AS2RecordList updateTableRow(AS2Record value) throws Exception {
    	return new AS2RecordList();
    }
    public AS2RecordList deleteTableRow(AS2Record value) throws Exception {
    	return new AS2RecordList();
    }
}