package hr.as2.inf.server.security.authentication.servlets.filters;

import hr.as2.inf.server.security.AS2SecurityConstants;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * http://www.journaldev.com/1933/java-servlet-filter-example-tutorial
 * http://stackoverflow.com/questions/16060390/sharing-session-in-web-applications
 *  
	01.10.2014 - version 1.0
	
 	 <filter>
    	<filter-name>AS2AuthenticationFilter</filter-name>
    	<filter-class>
        	hr.as2.inf.server.security.authentication.servlets.filters.AS2AuthenticationFilter
     	</filter-class>
    	 </filter>
	<filter-mapping>
	    <filter-name>AS2AuthenticationFilter</filter-name>
	    <url-pattern>/*</url-pattern>
	</filter-mapping>
 *
 */
@WebFilter("/AS2AuthenticationFilter")
public class AS2AuthenticationFilter implements Filter, AS2SecurityConstants {
    //Authentitation type
    public static int AUTHENTICATION_NA = 0;
    public static int AUTHENTICATION_AD = 1;
    public static int AUTHENTICATION_LDAP = 2;
    public static int AUTHENTICATION_JDBC = 3;
    public static int AUTHENTICATION_TYPE = AUTHENTICATION_AD;
	private String _login_url = "module/login/login.jsp";//default
	private static String _post_action = "security_check";
	private static String _as2_servlet_action = "json_servlet";

	public void init(FilterConfig config) throws ServletException {
		String parametar_value = null;
		parametar_value = config.getInitParameter(AS2_LOGIN_LINK);
		if (parametar_value != null && parametar_value.length() > 0)
			_login_url = parametar_value;
		//authentication_type
		parametar_value = config.getInitParameter(AS2_AUTHENTICATION_TYPE);
		if (parametar_value != null && parametar_value.length() > 0)
			AUTHENTICATION_TYPE = Integer.parseInt(parametar_value);
	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		if(AUTHENTICATION_TYPE==AUTHENTICATION_NA){
			req.setAttribute(AS2_USER_AUTHORIZED, true);
			chain.doFilter(req, res);//TODO
		}else if (request.getServletPath().contains(_post_action)
				|| request.getServletPath().contains("/module/")
				/*|| request.getServletPath().contains("login")*/) {
			chain.doFilter(req, res);
		} else {
			if (doFilterNeedLogin(req,request, response)) {
				if (request.getServletPath().endsWith(_as2_servlet_action)){
					req.setAttribute(AS2_USER_AUTHORIZED, false);
					chain.doFilter(req, res);
				}else{
					response.sendRedirect(_login_url);
				}
			} else {
				chain.doFilter(req, res);
			}
		}
	}
	
	protected boolean doFilterNeedLogin(ServletRequest req, HttpServletRequest request,	HttpServletResponse response) throws ServletException, IOException {
//		HttpSession session = request.getSession(false);
//		if (session!=null){
//			Object as2_session = session.getAttribute(AS2_SESSION);
//			Object username= session.getAttribute(AS2_USERNAME);
//			if(checkAs2Session(username,as2_session,request)){
//				return false;
//			}else{
//				return doFilterNeedLoginCookie(request,response);
//			}
//		}else{
			return doFilterNeedLoginCookie(req,request,response);
//		} 	
	}

	//TODO used in case a cookie option is required
    protected boolean doFilterNeedLoginCookie(ServletRequest req, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	Object username="";
    	Object as2_session="";
    	boolean has_session = false;
    	boolean has_user = false;
    	Cookie[] cookies = request.getCookies();
    	if(cookies != null){
	    	for(Cookie cookie : cookies){
	    		if(cookie.getName().equalsIgnoreCase(AS2_USERNAME)){
	    			has_user = true;
	    			username=cookie.getValue();
	    		}else if(cookie.getName().equalsIgnoreCase(AS2_SESSION)){
	    			as2_session=cookie.getValue();
//	    			if(session!=null)
//	    			HttpSession session = request.getSession(true);
	    			if(checkAs2Session(username,as2_session, request)){
//	    				if(session!=null && 
//	    						session.getAttribute(AS2_SESSION)==null){//Ako je novi session
//	    					session.setAttribute(AS2_USERNAME, username);
//		            		session.setAttribute(AS2_SESSION, as2_session);
//		            		session.setMaxInactiveInterval(AS2_MAX_AGE_INTERVAL);
//	    				}
	    				has_session=true;
	    			}
	    		}
	    	}
    	}
    	if(has_user && has_session){
    		req.setAttribute(AS2_USERNAME, username);
    		req.setAttribute(AS2_SESSION, as2_session);
    		return false;
    	}else {
    		return true;
    	}
    }
    
    private boolean checkAs2Session(Object username, Object as2_session, HttpServletRequest request) {
  		if(as2_session!=null && username!=null){
  			String as2_ses = as2_session.toString();			
  			if(as2_ses.contains(username.toString()) 
  					&& as2_ses.contains("as2")
  	    			&& as2_ses.contains(request.getRemoteAddr())){
  				return true;
  			}
  		}
  		return false;
  	}
    
	public void destroy() {
		// close any resources here
	}
}
