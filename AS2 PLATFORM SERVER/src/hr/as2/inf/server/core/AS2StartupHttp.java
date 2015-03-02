package hr.as2.inf.server.core;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.security.encoding.AS2Base64;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.common.security.user.AS2UserFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
	    name = "AS2StartupHttp",
	    description = "A servlet that starts first and initiate connections, etc.",
	    urlPatterns = {"/as2", "/as2start", "/as2servlet", "/as2app", "/as2server", "/j2eestart"},
	    loadOnStartup = 1
	)
public class AS2StartupHttp extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static long _started;
	private static AS2Context CONTEXT_KEEPER;
 
    public AS2StartupHttp() {
        super();
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
        writer.println("<html> GET - AS2StartupHttp started at = "+new Date(_started).toString() + "</html>");
        writer.flush();
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		PrintWriter writer = response.getWriter();
        writer.println("<html> POST - AS2StartupHttp started at = "+new Date(_started).toString() + "</html>");
        writer.flush();
	}
	/** 
	 * Initialize transport server and executes the tasks from J2EEContextInit file. 
	 */
    public void init(ServletConfig servletConfig) throws ServletException {
    	super.init(servletConfig);
    	_started = System.currentTimeMillis();
    	System.out.println("AS2StartupHttp:init at "+new Date(_started).toString() );
    	System.out.println("AS2StartupHttp:init server "+servletConfig.getServletContext().getServerInfo());
    	/* Print the list of the parameters. */
    	Enumeration<String> E = getInitParameterNames();
    	while (E.hasMoreElements())
    		System.out.println("AS2StartupHttp.init - Parameter Name: " + (String) E.nextElement());
    	String USER = getInitParameter("USER");
    	if (USER == null)
    		USER = "DEFAULT";
    	/* Should read encoded password. */
    	String PASSWORD = getInitParameter("PASSWORD");
    	if (PASSWORD == null)
    		PASSWORD = "";
    	else
    		PASSWORD = AS2Base64.decode(PASSWORD);
    	String PROPERTIES_PATH = getInitParameter("PROPERTIES_PATH");
    	/* When authorization needed to read URL here is the user and password. */
    	AS2User user = AS2UserFactory.getInstance().getUser(USER);
    	user.setPassword(PASSWORD);
    	AS2UserFactory.getInstance().setSystemUser(user);
    	if (PROPERTIES_PATH != null) {
    		AS2Context.setPropertiesPath(PROPERTIES_PATH);
    	}
    	/* Context needs to be referenced by the transport servlet 
    	 * to keep the singleton references which could get garbage collected. 
    	 */
    	CONTEXT_KEEPER = AS2Context.getInstance();
    	
    	/* Initialize all the tasks found in J2EEContextInit file. */
    	AS2Context.getInstance().init();
    }
	/** 
	 * Call all the tasks found in J2EEContextDestroy file. 
	 */
	public void destroy() {
		AS2Context.getInstance().destroy();
	}
}
