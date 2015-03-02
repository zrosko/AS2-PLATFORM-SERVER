package hr.as2.inf.server.session;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.data.valuelist.AS2ValueListInfo;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.common.session.AS2Session;
import hr.as2.inf.server.session.bl.AS2SessionBo;

import java.util.Hashtable;

public class AS2SessionFactory {
	//test
	final private static ThreadLocal<String> currentSession = new ThreadLocal<String>();	
	//test
	private Hashtable<String, AS2User> _transaction_users = new Hashtable<String, AS2User>();
	private Hashtable<String, AS2Record> _transaction_requests = new Hashtable<String, AS2Record>();
	
	public final static int AS2_SESSION_TYPE_MEMORY = 1;
	public final static int AS2_SESSION_TYPE_JDBC = 2;
	public final static int AS2_SESSION_TYPE = AS2_SESSION_TYPE_MEMORY;
	
	private static AS2SessionBo bo;	
	private static AS2SessionFactory instance = null;

	private AS2SessionFactory() {
	}

	public static AS2SessionFactory getInstance() {
		if (instance == null){
			instance = new AS2SessionFactory();
			bo = new AS2SessionBo(AS2_SESSION_TYPE);
		}
		return instance;
	}
	//test
	public String test(String value){
		currentSession.set(value);
		return currentSession.get();
	}
	//test
	public AS2Session createSession(AS2Record value) throws Exception {
		AS2User user = (AS2User) value.getAsObject(AS2Constants.USER_OBJ);
		_transaction_users.put(Thread.currentThread().getName(),user);
		_transaction_requests.put (Thread.currentThread().getName(), value);
		
		AS2Session session = new AS2Session();
		session.setSessionId(getCurrentSessionId(value));
		return bo.create(session);
	}
	
	public AS2Session getCurrentSession(AS2Record value) throws Exception {
		AS2Session session = new AS2Session();
		session.setSessionId(getCurrentSessionId(value));
		return bo.load(session);
	}

	public AS2Session getCurrentSession() throws Exception {
		AS2Session session = new AS2Session();
		session.setSessionId(Thread.currentThread().getName());
		return bo.load(session);
	}
	public void invalidateSession(AS2Record value, AS2Record res) throws Exception {
		AS2User user = getCurrentUser();
		AS2ValueListInfo info = (AS2ValueListInfo) user.getProperty(AS2User._VALUE_LIST_INFO);
		if(info!=null)
		    info.resetActions();
		res.set(AS2RecordList._VALUE_LIST_INFO, info);
		_transaction_users.remove(Thread.currentThread().getName());
	    _transaction_requests.remove(Thread.currentThread().getName());
	    //session
		AS2Session session = new AS2Session();
		session.setSessionId(getCurrentSessionId(value));
		bo.invalidate(session);
	}

	public void setSessionValue(String name, String value) throws Exception {
		AS2Session session = new AS2Session();
		session.setSessionId(getCurrentSessionId(null));
		bo.setValue(session, name, value);
	}

	public void removeSessionValue(String name) throws Exception {
		AS2Session session = new AS2Session();
		session.setSessionId(getCurrentSessionId(null));
		bo.removeValue(session, name);
	}
	
	/* private */
	public String getCurrentSessionId(AS2Record value){
		//TODO use thread, user object, session id from client etc.
		String session_key = Thread.currentThread().getName();
		return session_key;
	}
	
	/******** User *********/
	public AS2User getCurrentUser(){
		AS2User user = _transaction_users.get(Thread.currentThread().getName()); 
	    if(user!=null)
	        return user;
	    return new AS2User();
	}
	public String getCurrentUserId(){
		AS2User user = _transaction_users.get(Thread.currentThread().getName()); 
	    if(user!=null)
	        return user.getUserId();
	    return "";
	}
	public String getCurrentUserName(){
		AS2User user = (AS2User)_transaction_users.get(Thread.currentThread().getName()); 
	    if(user!=null)
	        return user.getUserName();
	    return "";
	}
	public String getCurrentUserDepartmentId(){
		AS2User user = (AS2User)_transaction_users.get(Thread.currentThread().getName()); 
	    if(user!=null)
	        return user.getUserDepartment();
	    return "";
	}
	public String getCurrentUserRoleId(){
		AS2User user = (AS2User)_transaction_users.get(Thread.currentThread().getName()); 
	    if(user!=null)
	        return user.getUserRole();
	    return "";	    
	}
}
