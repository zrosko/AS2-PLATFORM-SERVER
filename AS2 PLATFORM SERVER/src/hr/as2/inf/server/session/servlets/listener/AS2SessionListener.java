package hr.as2.inf.server.session.servlets.listener;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 *
  http://viralpatel.net/blogs/jsp-servlet-session-listener-tutorial-example-in-eclipse-tomcat/
 	
  01.10.2014. - version 1.0
  
 	<listener>
    <description>AS2SessionListener</description>
    <listener-class>
        hr.as2.inf.server.session.servlets.listener.AS2SessionListener
    </listener-class>
	</listener>    
 */
@WebListener("/AS2SessionListener")
public class AS2SessionListener implements HttpSessionListener {
	private int sessionCount = 0;
	private ServletContext context;

	/**
	 * The sessionCreated() method will be called by the servlet container
	 * whenever a new session is created for this application. An object of
	 * javax.servlet.http.HttpSessionEvent class is passed as an argument to the
	 * sessionCreated method. This object can be used to get the session related
	 * information including session ID. In our example we have used a counter
	 * sessionCounter which counts the number of live session at any given point
	 * of time. Whenever a new session is created, this count gets incremented.
	 */
	public void sessionCreated(HttpSessionEvent event) {
		synchronized (this) {
			sessionCount++;
		}
		this.context = event.getSession().getServletContext();
		context.log("AS2SessionListener - " + new java.util.Date()+": created: " + event.getSession().getId());
		context.log("AS2SessionListener - Total sessions: " + sessionCount);
	}

	/**
	 * The sessionDestroyed() method will be called by the servlet container
	 * whenever an existing session is invalidated. We have used this method in
	 * our example to decrement the session count and display the ID of session
	 * being destroyed.
	 */
	public void sessionDestroyed(HttpSessionEvent event) {
		synchronized (this) {
			sessionCount--;
		}
		this.context = event.getSession().getServletContext();
		context.log("AS2SessionListener - " + new java.util.Date()+": destroyed: " + event.getSession().getId());
		context.log("AS2SessionListener - Total sessions: " + sessionCount);
	}
}