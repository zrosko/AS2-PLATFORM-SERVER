package hr.as2.inf.server.jsp;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.invokers.AS2InvokerFactory;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class J2EEDefaultJSPController extends HttpServlet{
	private static AS2Context CONTEXT_KEEPER;
	public static final String TO_PAGE = "@@to_page";
	public static final String FROM_PAGE = "@@from_page";
	public static final String ERROR_PAGE = "@@error_page";
	public static final String RESPONSE_DATA = "response_data";
	public static final String REQUEST_DATA = "request_data";
	public static final String ERROR_DATA = "error_data";
	public static final String DEFAULT_ERROR_PAGE = "/jsp/Error.jsp";
	public static final String DISPLAY_DETAIL_MSG= "@@error_code";
	public static final String MESSAGE_SERVICE = "getMessageDetails";
	public static final String MESSAGE_COMPONENT = "hr.banksoft.inf.server.services.J2EEExceptionService";
	protected static long _htmlTransportCallCounter = 0;
	protected boolean _useRelativeURIs = true;

/** 
 * Call all the tasks (class methods) found in J2EEContextDestroy.properties file.
 * J2EEContextDestroy.properties is used to list any function call needed to be 
 * called when the current process is being stoped.
 * It can be used to close data base connections, etc.
 */
public void destroy() {
	AS2Context.getInstance().destroy();
}
public void init(ServletConfig servletConfig) throws ServletException {
    //org.apache.log4j.PropertyConfigurator.configure(
    //hr.banksoft.inf.common.services.J2EEContext.getPropertiesPath()+
    //"log4j.properties");
    /* Context needs to be referenced by the transport servlet 
     * to keep the singleton references which could get garbage collected. 
     */
     super.init(servletConfig);
    CONTEXT_KEEPER = AS2Context.getInstance();
    /* Initialize all the tasks found in J2EEContextInit file. */
	AS2Context.getInstance().init();
}

public void callPage(
    String s, 
    HttpServletRequest httpservletrequest, 
    HttpServletResponse httpservletresponse)
    throws IOException, ServletException {
    callURI(s, httpservletrequest, httpservletresponse);
}
public void callURI(
    String s, 
    HttpServletRequest httpservletrequest, 
    HttpServletResponse httpservletresponse)
    throws IOException, ServletException {
    ServletContext servletcontext;
    String s1 = null;
    if (_useRelativeURIs) {
        servletcontext = getServletContext();
        s1 = s;
    } else {
        servletcontext = getServletContext().getContext(s);
    }
    RequestDispatcher requestdispatcher = servletcontext.getRequestDispatcher(s1);
    requestdispatcher.forward(httpservletrequest, httpservletresponse);
}
public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws IOException {
    viewService(req, res);
}
public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws IOException {
    viewService(req, res);
}
public String getParameter(HttpServletRequest request, String parameterName) {
    String[] parameterValues = null;
    String parameterValue = null;
    parameterValues = request.getParameterValues(parameterName);
    if (parameterValues != null)
        parameterValue = parameterValues[0];
    return parameterValue;
}
protected AS2Record readHTMLRequest(
	HttpServletRequest req, 
	HttpServletResponse res)
	throws Exception {

	AS2Record inputFields = new AS2Record();
	String service = null;
	String component = null;

	try {
		service = req.getParameterValues("Service")[0];
		component = req.getParameterValues("Component")[0];
	} catch (Exception e) {
		//No need to handle the NullPointerException
		//in the case service or component are not passed in.
	}

	inputFields.setRemoteMethod(service);
	inputFields.setRemoteObject(component);
	//inputFields.set("LogInHostName", req.getRemoteAddr());
	Enumeration E = req.getParameterNames();

	while (E.hasMoreElements()) {

		String name = (String) E.nextElement();
		String value = req.getParameterValues(name)[0];
    //Ignoriraj Service i Component medu poslovnim podacima. 
    //Ova dva polja su sistemska pomocna polja.
    if(!name.equals("Service") && !name.equals("Component") && !name.startsWith("@@"))
      inputFields.set(name, value);
	}

	return inputFields;
}
public static void setRequestAttribute(
    String s, 
    Object obj, 
    HttpServletRequest httpservletrequest)
    throws IOException, ServletException {
    httpservletrequest.setAttribute(s, obj);
}
public void viewService(HttpServletRequest req, HttpServletResponse res) throws IOException {
	AS2Record request = null; 
	AS2Record response = null;
	String srvRemoteAddr = null;

	//synchronized (this) {
		_htmlTransportCallCounter++;
	//}
	
	long _start = System.currentTimeMillis();
	
	String toPage = getParameter(req,TO_PAGE);
	String fromPage = getParameter(req,FROM_PAGE);
	String errorPage = getParameter(req,ERROR_PAGE);
	if(errorPage == null)
		errorPage = DEFAULT_ERROR_PAGE;
	
	try {
		srvRemoteAddr = req.getRemoteAddr();
		request = readHTMLRequest(req, res);
    request.set("@@IP_ADDRESS", srvRemoteAddr);
    if(fromPage!=null && fromPage.equals("/jsp/Login.jsp")) {
    		AS2User _user = new AS2User();
    		_user.setUserName(request.get("USER"));
    		_user.setPassword(request.get("PASSWORD"));
    		request = _user;
			request.setRemoteMethod("logIn");
			request.setRemoteObject("hr.adriacomsoftware.inf.server.security.authentication.rbac.facade.RBACSecurityAuthenticationFacadeServer");
      if(request.get("USER").equalsIgnoreCase("sa"))
        toPage = "/jsp/Message.jsp";
      else
        toPage = "/jsp/Exception.jsp";     
    } else {
    	//J2EEServerAdministrationFacadeServer.getInstance().validateSecurity(srvRemoteAddr);
     }
		//here do display message details if user entered @@error_code
		if(request.get(DISPLAY_DETAIL_MSG)!=null){
			request.setRemoteMethod(MESSAGE_SERVICE);
			request.setRemoteObject(MESSAGE_COMPONENT);
			toPage = errorPage;
		}
		StringBuffer tmp = new StringBuffer();
		tmp.append(new java.util.Date() + " " + Thread.currentThread());
		tmp.append(" +++ JSP Start Service Call from ");
		tmp.append(srvRemoteAddr);
		tmp.append(" Service = ");
		try{
			tmp.append(request.getRemoteMethod());
		}catch(Exception e0){
			tmp.append("UNKNOWN Service");
		}
		
		AS2Trace.traceStringOut(AS2Trace.W, tmp.toString());
		
		J2EEBean bean = new J2EEBean(request);
		setRequestAttribute(REQUEST_DATA, bean ,req);

		if(request.getRemoteMethod() != null && request.getRemoteObject() != null){
			// add req and res for possible session processing
			//J2EESessionService.getInstance().addServletRequest(req);	
			//J2EESessionService.getInstance().addServletResponse(res);	
			//execute the service, start transaction
			Object o = AS2InvokerFactory.getInstance().getInvoker(null).invoke(request, /*TODO context*/ null);
			//TODO TEST iznad 1 Object o=J2EEApplicationControllerFactory.getInstance().getApplicationController().executeRequest(request);	
			if (o instanceof Throwable){
				setRequestAttribute(ERROR_DATA,o,req);
				callPage(errorPage,req,res);
			}
			else if (o instanceof AS2Record){
				response = (AS2Record) o;
				Object data=response.getAsObject("response");
				
				if(data instanceof AS2RecordList){
					AS2RecordList resp=(AS2RecordList)data;
					if (resp.isOneRowOnly()){
						J2EEBean mb=new J2EEBean((AS2Record)data);
						setRequestAttribute(RESPONSE_DATA,mb,req);
					}else{
						J2EEBeanList mbl=new J2EEBeanList(resp);
						setRequestAttribute(RESPONSE_DATA, mbl , req);
					}
					//if there are nestedResultSets, set them in Request
					/* TODO 10 linija ispod
					if (resp.hasMoreResultSets()){
						Vector nestedResultSetNames=resp.getNestedResultSetNames();
						for (int i=0; i<nestedResultSetNames.size();i++){
							String s=nestedResultSetNames.elementAt(i).toString();
							AS2RecordList nextedRs=resp.getResultSet(s);
							if (nextedRs.isOneRowOnly()){
								J2EEBean mb=new J2EEBean((AS2Record)nextedRs);
								setRequestAttribute(s,mb,req);
							}else{
								J2EEBeanList mbl=new J2EEBeanList(nextedRs);
								setRequestAttribute(s, mbl, req);
							}
						}
						
					}*/
									
				}else if (data instanceof AS2Record){
					AS2Record resp=(AS2Record)data;
					J2EEBean mb = new J2EEBean(resp);
					mb.setRemoteMethod(resp.getRemoteMethod());
					mb.setRemoteObject(resp.getRemoteObject());
					setRequestAttribute(RESPONSE_DATA,mb,req);
				}else{
					setRequestAttribute(RESPONSE_DATA,data,req);
				}			
			
				if (toPage!=null)
					callPage(toPage,req,res);
				else if (fromPage!=null)
					callPage(fromPage,req,res);
				else if (errorPage!=null)
					callPage(errorPage,req,res);
			}else{
				setRequestAttribute(RESPONSE_DATA,o,req);
				if (toPage!=null)
					callPage(toPage,req,res);
				else if (fromPage!=null)
					callPage(fromPage,req,res);
				else if (errorPage!=null)
					callPage(errorPage,req,res);
			}
		}else{
			// just do the JSP page call, no service/transaction call
			if (toPage!=null)
				callPage(toPage,req,res);
			else if (fromPage!=null)
				callPage(fromPage,req,res);
			else if (errorPage!=null)
				callPage(errorPage,req,res);
		}
	} catch (Throwable e) {
		//need to handle uset intrrupts (Stop button) by out.flush(). ???
		if (errorPage!=null){
			try{
				setRequestAttribute(ERROR_DATA,e,req);
				callPage(errorPage,req,res);
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
	} finally {

		StringBuffer tmp = new StringBuffer();
		tmp.append(new java.util.Date() + " " + Thread.currentThread());
		tmp.append(" --- JSP End Service Call #: ");
		tmp.append(_htmlTransportCallCounter);
		tmp.append(" from ");
		tmp.append(srvRemoteAddr);
		tmp.append(" Service = ");
		try{
			tmp.append(request.getRemoteMethod());
		}catch(Exception e1){
			tmp.append("UNKNOWN Service");
		}
		tmp.append(" time ");
		tmp.append((System.currentTimeMillis() - _start + " mills"));
		
		AS2Trace.traceStringOut(AS2Trace.W, tmp.toString());
	}
	return;
}
}
