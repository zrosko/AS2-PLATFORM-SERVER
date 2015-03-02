package hr.as2.inf.server.logging.servlets.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
 
/**
 * Servlet Filter implementation class RequestLoggingFilter
 * http://www.journaldev.com/1933/java-servlet-filter-example-tutorial
 * 
 * 01.10.2014. - version 1.0. 
 * 
 * <filter>
		<filter-name>AS2LoggingFilter</filter-name>
		<filter-class>hr.as2.inf.server.logging.servlets.filter.AS2LoggingFilter</filter-class>
		<init-param>
			<param-name>log-level</param-name>
			<param-value>ERROR</param-value>
		</init-param>
	</filter>
	<filter-mapping>
			<filter-name>AS2LoggingFilter</filter-name>
			<url-pattern>/*</url-pattern>
			<dispatcher>REQUEST</dispatcher>
	</filter-mapping>
 */
@WebFilter("/AS2LoggingFilter")
public class AS2LoggingFilter implements Filter {
 
//    private ServletContext context;
	private String log_level = "ERROR";
     
    public void init(FilterConfig config) throws ServletException {
//        this.context = config.getServletContext();
        this.log_level = config.getInitParameter("log-level");
    }
 
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        Enumeration<String> params = req.getParameterNames();
        if (log_level.equalsIgnoreCase("INFO")){
        	System.out.println("AS2LoggingFilter - ");
	        while(params.hasMoreElements()){
	            String name = params.nextElement();
	            String value = request.getParameter(name);
	            System.out.println(req.getRemoteAddr() + "::Param::{"+name+"="+value+"} ");
	        }
	        System.out.println("AS2LoggingFilter - ");
	        Cookie[] cookies = req.getCookies();
	        if(cookies != null){
	            for(Cookie cookie : cookies){
	            	System.out.println(req.getRemoteAddr() + "::Cookie::{"+cookie.getName()+","+cookie.getValue()+"}");
	            }
	        }
        }
        chain.doFilter(request, response);
    }
 
    public void destroy() {
        //we can close resources here
    }
 
}
