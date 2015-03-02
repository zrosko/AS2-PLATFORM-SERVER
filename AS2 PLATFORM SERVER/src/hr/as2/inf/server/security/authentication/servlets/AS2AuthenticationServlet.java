package hr.as2.inf.server.security.authentication.servlets;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.requesthandlers.AS2ServerRequestHandler;
import hr.as2.inf.server.security.AS2SecurityConstants;
import hr.as2.inf.server.security.authentication.facade.AS2AuthenticationFacadeServer;
import hr.as2.inf.server.transaction.AS2Transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
 
/**
 * TODO http://stackoverflow.com/questions/19882447/java-servlet-redirect-to-static-page-do-not-work
<servlet>
		<servlet-name>AS2AuthenticationServlet</servlet-name>
		<servlet-class>
			hr.as2.inf.server.security.authentication.servlets.AS2AuthenticationServlet
		</servlet-class>
		<init-param>
			<param-name>application_link</param-name>
			<param-value>http://127.0.0.1:8888/portal.html?gwt.codesvr=127.0.0.1:9997</param-value>
			<!-- PROMJENITI NA PRODUKCIJI U /aplikacija-->
		</init-param>
		<init-param>
			<param-name>domain</param-name>
			<param-value>jaba</param-value>
		</init-param>
		<init-param>
			<param-name>active-directory-realm</param-name>
			<param-value>ldap://SRVDOM1:389/dc=jaba,dc=hr</param-value>
		</init-param>
		<init-param>
			<param-name>application</param-name>
			<param-value>portal</param-value>
		</init-param>
		<init-param>
			<param-name>admin-users</param-name>
			<param-value>as2admin,admin</param-value>
		</init-param>
		<init-param>
			<param-name>session_type</param-name>
			<param-value>1</param-value>
		</init-param>
		<init-param>
			<param-name>authentication_type</param-name>
			<param-value>1</param-value>
		</init-param>
	</servlet>
	<servlet-mapping>
		<servlet-name>AS2AuthenticationServlet</servlet-name>
		<url-pattern>/j_security_check_as2</url-pattern>
	</servlet-mapping>
 * http://www.journaldev.com/1907/java-servlet-session-management-tutorial-with-examples-of-cookies-httpsession-and-url-rewriting
 * http://www.informit.com/articles/article.aspx?p=26138&seqNum=7
 */
@WebServlet("/j_security_check_as2")
public class AS2AuthenticationServlet extends AS2ServerRequestHandler implements AS2SecurityConstants{
    private static final long serialVersionUID = 1L;
    //Authentitation type
    public static int AUTHENTICATION_NA = 0;
    public static int AUTHENTICATION_AD = 1;
    public static int AUTHENTICATION_LDAP = 2;
    public static int AUTHENTICATION_JDBC = 3;
    public static int AUTHENTICATION_TYPE = AUTHENTICATION_AD;
    //Session type
    public static int SESSION_NA = 0;
    public static int SESSION_SERVLET = 1;
    public static int SESSION_JDBC = 2;
    public static int SESSION_TYPE = SESSION_SERVLET;
    //Defaults
    private final String username = "admin";
    private final String password = "password";
    private String _application_url = "/portal";
    private String _login_url = "module/login/login.jsp";
	//web.xml parameter, here are defaults
	private static String _active_directory_realm = "ldap://SRVDOM1:389/dc=jaba,dc=hr";//default
	private static String _domain = "jaba";
	private static String _application = "jasperserver";//default
	private static ArrayList<String> admin_users = new ArrayList<String>();//jasperamdin, etc.

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		System.out.println("AS2AuthenticationServlet init() ");
		String parametar_value = null;
//		//login_link
//		parametar_value = config.getInitParameter(AS2_LOGIN_LINK);
//		if(parametar_value!=null && parametar_value.length()>0)
//			_login_link = parametar_value;
		//application_link
		parametar_value = config.getInitParameter(AS2_APPLICATION_LINK);
		if(parametar_value!=null && parametar_value.length()>0)
			_application_url = parametar_value;
		//domain
		parametar_value = config.getInitParameter(AS2_DOMAIN);
		if(parametar_value!=null && parametar_value.length()>0)
			_domain = parametar_value;
		//application
		parametar_value = config.getInitParameter(AS2_APPLICATION);
		if(parametar_value!=null && parametar_value.length()>0)
			_application = parametar_value;
		//active directory realm
		parametar_value = config.getInitParameter(AS2_ACTIVE_DIRECTORY_REALM);
		if(parametar_value!=null && parametar_value.length()>0)
			_active_directory_realm = parametar_value;
		//session_type
		parametar_value = config.getInitParameter(AS2_SESSION_TYPE);
		if(parametar_value!=null && parametar_value.length()>0)
			SESSION_TYPE = Integer.parseInt(parametar_value);
		//authentication_type
		parametar_value = config.getInitParameter(AS2_AUTHENTICATION_TYPE);
		if(parametar_value!=null && parametar_value.length()>0)
			AUTHENTICATION_TYPE = Integer.parseInt(parametar_value);
		//admins
		String admin_users_list = config.getInitParameter(AS2_ADMIN_USERS);
		StringTokenizer token = new StringTokenizer(admin_users_list, ",");

		while (token.hasMoreTokens()) {
			admin_users.add(token.nextToken());
		}
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
    //If we disable the cookies in browser, it won't work because server will not receive the JSESSIONID cookie from client. 
    //Servlet API provides support for URL rewriting that we can use to manage session in this case.
    //It is a fallback approach and it kicks in only if browser cookies are disabled.
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String client_action = "login";
        if(request.getParameter("action")!=null)
        	client_action = request.getParameter("action");
        if(client_action.equalsIgnoreCase("login"))
        	doPostLogIn(request, response);
        else if(client_action.equalsIgnoreCase("logout")){
        	doPostLogOut(request, response);
        } else if(client_action.equalsIgnoreCase("changepassword"))
        	doPostChangePassword(request, response);
    }

    protected void doPostLogIn(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String j_username = req.getParameter(AS2_J_USERNAME);
        String j_password = req.getParameter(AS2_J_PASSWORD);
    	String j_remote_address = req.getRemoteAddr();
    	String j_remote_port = req.getRemotePort()+"";
        
        AS2User request = new AS2User();
    	request.set(AS2_USERNAME, j_username);
    	request.set(AS2_PASSWORD, j_password);
    	request.set(AS2_J_USERNAME, j_username);
    	request.set(AS2_J_PASSWORD, j_password);
    	
        //int AUTHENTICATION_TYPE = AUTHENTICATION_AD;
        int SESSION_TYPE = SESSION_SERVLET;        
        boolean login_ok = false;
        //LOG IN TO 
        //Need a servlet parameter to decide the authentication type (AD,LDAP, DAO)
        if(AUTHENTICATION_TYPE==AUTHENTICATION_NA){
        	if(username.equals(j_username) && password.equals(j_password)){
        		login_ok = true;
        	}
        }else if(AUTHENTICATION_TYPE == AUTHENTICATION_JDBC){ 
        	login_ok = loginDAO(request);			        	
        }else  if(AUTHENTICATION_TYPE==AUTHENTICATION_AD){
        	login_ok = loginAD(request);  
        }
        
        if(login_ok){   
        	String as2_session = j_username + "_as2_" + j_remote_address+"_"+j_remote_port;
        	if(SESSION_TYPE==SESSION_SERVLET){
        		//TODO
				//HttpSession session = req.getSession();
				//session.setAttribute(AS2_USERNAME, j_username);
				//session.setAttribute(AS2_SESSION, as2_session);
				//session.setMaxInactiveInterval(AS2_MAX_AGE_INTERVAL);
        	}else if(SESSION_TYPE!=SESSION_SERVLET){
        		//strategy pattern
        		//as2_login_ok cookie.setValue(Base64.decodeString(cookie.getValue()));
        	}
        	Cookie ck_userName = new Cookie(AS2_USERNAME, j_username);
        	ck_userName.setMaxAge(AS2_MAX_AGE_INTERVAL);
        	ck_userName.setPath("/");
        	res.addCookie(ck_userName);
        	Cookie ck_as2_session = new Cookie(AS2_SESSION,as2_session);
        	ck_as2_session.setMaxAge(AS2_MAX_AGE_INTERVAL);
        	ck_as2_session.setPath("/");
        	res.addCookie(ck_as2_session);
        	//Get the encoded URL string for URL rewriting
        	String encodedURL = res.encodeRedirectURL(_application_url);
        	res.sendRedirect(encodedURL);
        }else{
        	doPostLogInError(req, res);
        } 
    }
   
    protected void doPostLogInError(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	res.setContentType("text/html; charset=UTF-8");
    	PrintWriter out= res.getWriter();
//    	out.println("<!DOCTYPE html>");
//    	out.println("<html>");
//    	out.println("<head>");
//    	out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
//    	out.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">");
//    	out.println("</head>");
//    	out.println("<body>");
    	out.print("<font color=\"red\">Korisničko ime ili lozinka su netočni.</font>");
    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/"+_login_url);
    	rd.include(req, res);
//    	out.println("</body>");
//    	out.println("</html>");
    }
    protected void doPostLogOut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	int SESSION_TYPE = SESSION_SERVLET;
		response.setContentType("text/html; charset=UTF-8");
		String as2_session = "";
		String username = "";
		Cookie[] cookies = request.getCookies();
		if(cookies != null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equalsIgnoreCase(AS2_USERNAME)){
					username = cookie.getValue();
					cookie.setPath("/");
	    		}else if(cookie.getName().equalsIgnoreCase(AS2_SESSION)){
	    			as2_session = cookie.getValue();
	    			cookie.setPath("/");
	    		}else if(cookie.getName().equalsIgnoreCase(AS2_J_SESSION_ID)){
	    			cookie.setPath("/"+_application);
	    		}
				System.out.println("AS2AuthenticationServer:logOut: deleting cookie: "+cookie.getName()+"="+cookie.getValue() );
				cookie.setValue("");
				cookie.setMaxAge(0);
				response.addCookie(cookie);
			}
		}
		//invalidate the session if exists
		if(SESSION_TYPE==SESSION_SERVLET){
	    	//HttpSession session = request.getSession(false);
	    	//if(session != null){
	    	//	session.invalidate();
	    	//}
		}else if(SESSION_TYPE!=SESSION_SERVLET){
   		//strategy pattern
		}
		System.out.println("AS2AuthenticationServer:logOut: OK - username: " + username + " as2_session: " + as2_session);
		response.sendRedirect(_login_url);
	}
    //TODO
    protected void doPostChangePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	//int SESSION_TYPE = SESSION_SERVLET;
//    	response.setContentType("text/html; charset=UTF-8");
        String j_user = request.getParameter(AS2_J_USERNAME);
        String j_password = request.getParameter(AS2_J_PASSWORD);
        AS2User as2_password = new AS2User();
        as2_password.set(AS2_USERNAME, j_user);
        as2_password.set(AS2_PASSWORD, j_password);
        
        @SuppressWarnings("unused")
		boolean as2_password_ok = false;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("JSESSIONID")) {
					System.out.println("LOG OUT JSESSIONID=" + cookie.getValue());
			    	try {//TODO Transakcija
			    		as2_password = AS2AuthenticationFacadeServer.getInstance().changePassword(as2_password);
			    		if(as2_password!=null && as2_password.get("valid").equals("ok"))
			    			as2_password_ok = true;
			    	}catch(Exception e){
			    		System.out.println("LOG OUT JSESSIONID=" + e);
			    		as2_password_ok = false;
			    	}
					break;
				}
			}
		}
		//TODO obavijest da je password promjenjen
		response.sendRedirect("password.html");
	}
	protected static boolean loginDAO(AS2Record vo) {
		boolean success = false;
		//option 2 
		//response = J2EEApplicationControllerFactory.getInstance().getApplicationController().executeRequest(request);	//Execute request
		//potrebno imati u path J2EEContext.property
		try{			
			AS2Transaction.begin();
			AS2User value = new AS2User(vo);
			value.setUserName(vo.get(AS2_J_USERNAME));
			value.setPassword(vo.get(AS2_J_PASSWORD));
			value.setApplication(_application);//from web.xml
			AS2User _user = AS2AuthenticationFacadeServer.getInstance().logIn(value);
			if(_user!=null && _user.get("valid").equals("ok"))
				success = true;
			AS2Transaction.commit();
		}catch(Exception e){
			AS2Transaction.rollback();
			System.out.println("AS2AuthenticationServer:loginDAO: FAILED: "+e+vo.get(AS2_J_USERNAME));
			return false;
		}
		return success;
	}
	/**
	 * Login to LDAP (Active Directory).
	 * @param vo
	 * @return
	 */
	protected boolean loginAD(AS2Record vo) {
		boolean success = false;
		String user = "";
		String password = "";
		UsernamePasswordToken token =  null;
		int step_count = 0;
		try{
			user = vo.get(AS2_J_USERNAME);
			password = vo.get(AS2_J_PASSWORD);
			if(user.length()==0 || password.length()==0){
				return false;
			}
			Ini ini = new Ini();
			ini.setSectionProperty("main", "activeDirectoryRealm","org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm");
			ini.setSectionProperty("main", "activeDirectoryRealm.url",_active_directory_realm);
			step_count++;
			Factory<?> factory = new IniSecurityManagerFactory(ini);
			step_count++;
			org.apache.shiro.mgt.SecurityManager securityManager = (org.apache.shiro.mgt.SecurityManager)factory.getInstance();
			step_count++;
			SecurityUtils.setSecurityManager(securityManager);
			step_count++;
			Subject subject = SecurityUtils.getSubject();
			step_count++;
			token = new UsernamePasswordToken(_domain + "\\" + user, password, false);
			Session session = subject.getSession(true);
			session.setTimeout(24*60*60*1000);//24h
			subject.login(token);
			step_count++;
			success = subject.isAuthenticated();
			System.out.println("AS2AuthenticationServer:loginAD: OK - username: "+user+ " AD SHIRO Session: "+ subject.getSession(false).getId());
		}catch(Exception e){
			System.out.println("AS2AuthenticationServer:loginAD: FAILED for user: "+ user +" at step "+step_count+" "+ new java.util.Date() +"\n" + e);
			return false;
		}
		return success;
	}
	/**
	 * Login to LDAP (Active Directory).
	 * @param vo
	 * @return
	 *
	protected boolean logoutAD(AS2Record vo) {
		boolean success = false;
		String user = vo.get(AS2_USERNAME);
		int step_count = 0;
		try{
			Ini ini = new Ini();
			ini.setSectionProperty("main", "activeDirectoryRealm","org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm");
			ini.setSectionProperty("main", "activeDirectoryRealm.url",_active_directory_realm);
			step_count++;
			Factory<?> factory = new IniSecurityManagerFactory(ini);
			step_count++;
			org.apache.shiro.mgt.SecurityManager securityManager = (org.apache.shiro.mgt.SecurityManager)factory.getInstance();
			step_count++;
			SecurityUtils.setSecurityManager(securityManager);
			step_count++;
			Subject subject = SecurityUtils.getSubject();
			step_count++;	
			subject.logout();
			step_count++;
			success = true;
			System.out.println("AS2AuthenticationServer:loginOutAD: OK - username: "+user );
		}catch(Exception e){
			System.out.println("AS2AuthenticationServer:logOutAD: FAILED for user: "+ user +" at step "+step_count+" "+ new java.util.Date() +"\n" + e);
			return false;
		}
		return success;
	}
*/
	public static void main(String[] args) {
		String user = "";
		String password = "";
		UsernamePasswordToken token =  null;
		int step_count = 0;
		try{
			user = "zrosko";
			password = "Zr201410";
			if(user.length()==0 || password.length()==0){
				System.out.println("AS2AuthenticationServer:loginA problem");
			}
			Ini ini = new Ini();
			ini.setSectionProperty("main", "activeDirectoryRealm","org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm");
			ini.setSectionProperty("main", "activeDirectoryRealm.url",_active_directory_realm);
			step_count++;
			Factory<?> factory = new IniSecurityManagerFactory(ini);
			step_count++;
			org.apache.shiro.mgt.SecurityManager securityManager = (org.apache.shiro.mgt.SecurityManager)factory.getInstance();
			step_count++;
//			token = new UsernamePasswordToken(_domain + "\\" + user, password, false);
//			AuthenticationInfo inf = securityManager.authenticate(token);
//			inf.getCredentials();
			SecurityUtils.setSecurityManager(securityManager);
			step_count++;
			Subject subject = SecurityUtils.getSubject();
			step_count++;
			token = new UsernamePasswordToken(_domain + "\\" + user, password, false);
			subject.login(token);
			step_count++;
			subject.isAuthenticated();
			System.out.println("AS2AuthenticationServer:loginAD: OK - username: "+user);
		}catch(Exception e){
			System.out.println("AS2AuthenticationServer:loginAD: FAILED for user: "+ user +" at step "+step_count+" "+ new java.util.Date() +"\n" + e);
		}
	}
}