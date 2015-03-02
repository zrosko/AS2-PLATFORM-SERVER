package hr.as2.inf.server.requesthandlers.http;
// U Cotrollers na serveru korisitit @anotacije kod pozivanja facade servera.
//http://www.vogella.com/tutorials/JavaAnnotations/article.html
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.requesthandlers.AS2ServerRequestHandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
//import javax.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
//@WebServlet("/handler_http_java_j2ee")
public class AS2ServerRequestHandlerHttp extends AS2ServerRequestHandler {
	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		AS2Record request = null;
		Object response = null;
		AS2Record as2_request = null;
		String srvRemoteAddr = null;
		long mStart = System.currentTimeMillis();
		try {
			srvRemoteAddr = req.getRemoteAddr();
			request = readRequestFromObject(req.getInputStream());
			// Change thread name
			setThreadName(srvRemoteAddr, request.getRemoteObject(),request.getRemoteMethod());
			StringBuilder tmp = new StringBuilder();
			tmp.append(new java.util.Date() + " " + Thread.currentThread());
			tmp.append(" +++ AS2ServerRequestHandlerHttp Start HTTP Start Service Call from ");
			tmp.append(srvRemoteAddr);
			tmp.append(" Remote Method = ");
			try {
				tmp.append(request.getRemoteObject());
				tmp.append(".");
				tmp.append(request.getRemoteMethod());
			} catch (Exception e0) {
				tmp.append("UNKNOWN Service");
			}
			AS2Trace.traceStringOut(AS2Trace.W, tmp.toString());
			as2_request = new AS2Record();
			as2_request.setDummy(request.isDummy());
			as2_request.setRemoteObject(request.getRemoteObject());
			as2_request.setRemoteMethod(request.getRemoteMethod());
			as2_request.setProperties(request.getProperties());
			// Invocation Context
			AS2InvocationContext as2_context = prepareInvocationContext(as2_request, srvRemoteAddr);
			// Dispatch Request
			response = dispatchRequestToInvoker(as2_request, as2_context);
		} catch (Exception e) {
			response = e;
		} finally {
			try {
				writeResponseAsObject(res, response); 
			} catch (Exception endException) {
				endException.printStackTrace();
			}
			StringBuilder tmp = new StringBuilder();
			tmp.append(new java.util.Date() + " " + Thread.currentThread());
			tmp.append(" --- AS2ServerRequestHandlerHttp End HTTP End Service Call #: ");
			tmp.append(_httpTransportCallCounter);
			tmp.append(" from ");
			tmp.append(srvRemoteAddr);
			tmp.append(" Service = ");
			try {
				tmp.append(request.getRemoteObject());
				tmp.append(".");
				tmp.append(request.getRemoteMethod());
			} catch (Exception e1) {
				tmp.append("UNKNOWN Service");
			}
			tmp.append(" time ");
			tmp.append((System.currentTimeMillis() - mStart + " mills"));
			AS2Trace.traceStringOut(AS2Trace.W, tmp.toString());
		}
		AS2Trace.trace(AS2Trace.I, " AS2ServerRequestHandlerHttp.service end");
		request = null; // garbage collection
		response = null; // garbage collection
		as2_request = null; // garbage collection
	}
}
