/* (c) Adriacom Software d.o.o.
 * 22211 Vodice, Croatia
 * Created by Z.Rosko (zrosko@yahoo.com)
 * Date Dec 14, 2010 
 * Time: 12:16:23 PM
 */
package hr.as2.inf.server.security;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.common.types.AS2Date;
import hr.as2.inf.server.security.authorization.da.jdbc.J2EETragAktivnostiJdbc;

import java.lang.reflect.Method;
import java.util.Properties;


public final class AS2SecurityService {
    private static AS2SecurityService _instance = null;
    private static final String PROPERTIES_FILE = "/server/J2EESecureFacadeFactory.properties";
    private static Properties _facadeClasses;
    protected boolean _log_activity = true;//NOVO
	private AS2SecurityService() throws AS2Exception{
        initialize();
		AS2Context.setSingletonReference(this);
	}
	public static AS2SecurityService getInstance() {
		if (_instance == null)
			try {
				_instance = new AS2SecurityService();
			} catch (AS2Exception e) {
				AS2Trace.trace(AS2Trace.E, e, "J2EESecurityService.Constructor failed?");
			}
		return _instance;
	}
    protected void initialize() {
        _facadeClasses = AS2Helper.readPropertyFileAsURL(AS2Context.getPropertiesPath() + PROPERTIES_FILE);
    }
	public void checkServiceSecurity(AS2Record value) throws Exception {
	    AS2Record _value = new AS2Record();
	    _value = (AS2Record)value.clone();
	    if(_facadeClasses!=null &&_facadeClasses.getProperty(value.getRemoteObject())!=null){
	        Class facade_class = null;
	        Method get_instance_method = null;
	        Method regular_facade_method = null;
	        Class regular_parameters[] = null;
	        Class get_instance_parameters[] = new Class[0];
	      
	        Object target_object = null;
	        Object[] arguments = null;
	        Object[] get_instance_arguments = new Object[0];
	        regular_parameters = new Class[] { _value.getClass() };
	        arguments = new Object[] { _value };
	
	        facade_class = Class.forName(value.getRemoteObject());
	        get_instance_method = facade_class.getMethod("getInstance", get_instance_parameters);
	        target_object = get_instance_method.invoke(target_object, get_instance_arguments);
	        Object _returned = null;
	        regular_facade_method = facade_class.getMethod("checkServiceSecurity", regular_parameters);
	        regular_facade_method.invoke(target_object, arguments); 
	    }
        //if passed security check
    }
	public AS2Record prepareLogData(AS2Record value){
		AS2Record vo_log = new AS2Record();;		
		if(_log_activity){
            AS2User _user = (AS2User)value.getAsObject(AS2Constants.USER_OBJ);
            if(_user==null)
            	return vo_log; 
            
            vo_log = new AS2Record();            
            vo_log.set("id_prijave_korisnika", _user.getAsString("id_prijave_korisnika","0"));
            vo_log.set("aplikacija", _user.getApplication());
            vo_log.set("ip_address", _user.get("ip_address"));
            vo_log.set("korisnik", _user.get("korisnik"));
            vo_log.set("id_uloge", _user.get("role_id"));
            vo_log.set("id_komponente", _user.get("function_id"));
            vo_log.set("id_osobe", _user.get("id_osobe"));
            vo_log.set("vrijeme_aktivnosti", AS2Date.getCurrentTime().toString());
            vo_log.set("server_komponenta", value.getRemoteObject());
            vo_log.set("server_servis", value.getRemoteMethod());
            vo_log.set("sadrzaj", value.toString());
            String _service = value.getRemoteMethod();
            if(_service.startsWith("dodaj")||
                _service.startsWith("azuriraj")||
                _service.startsWith("brisi")||
                _service.startsWith("dupliciraj")||
                _service.startsWith("job")||
                _service.startsWith("ukljuci")||
                _service.startsWith("iskljuci")||
                _service.startsWith("izracunaj")||
                _service.startsWith("upisi")||
                _service.startsWith("izradi")||
                _service.startsWith("ponavljanje")||
                _service.startsWith("obrada")||
                _service.endsWith("POVIJEST")||
                _service.startsWith("obradi")){
                 	vo_log.set("aktivnost", "TXN");
             }else{
             		vo_log.set("aktivnost", "QUERY");
             }            
		}
		return vo_log;
	}
    public void daoLogActivity(AS2Record value){
        if(_log_activity){            
            J2EETragAktivnostiJdbc dao_log = new J2EETragAktivnostiJdbc();
            value.set("vrijeme_aktivnosti", AS2Date.getCurrentTime().toString());
            try{
            	dao_log.daoCreate(value);
	        }catch(Exception e){
	            System.out.println(e);
	        }
        }
    }
}