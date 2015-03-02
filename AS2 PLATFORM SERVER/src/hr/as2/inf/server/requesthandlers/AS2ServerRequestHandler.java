/**
 * 
 */
package hr.as2.inf.server.requesthandlers;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.file.AS2FileUtility;
import hr.as2.inf.common.reports.AS2ReportRenderer;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.invokers.AS2InvokerFactory;
import hr.as2.inf.server.security.AS2SecurityConstants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javaxt.utils.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
@SuppressWarnings("serial")
public abstract class AS2ServerRequestHandler extends HttpServlet implements AS2SecurityConstants{
	//Types
	protected static final String RESPONSE_TYPE_JSON = "json";
	protected static final String RESPONSE_TYPE_BYTES = "bytes";
	protected static final String RESPONSE_TYPE_AS2RECORD = "as2record";

	protected static long _httpTransportCallCounter = 0;
	protected AS2Record readRequestFromURL(HttpServletRequest req) throws AS2Exception {
		_httpTransportCallCounter++;
		AS2Record request = new AS2Record();
		try {
			Enumeration<String> E = req.getParameterNames();
			while (E.hasMoreElements()) {
				String name = E.nextElement();
				String value = req.getParameter(name);
				if(req.getParameterValues(name)!=null && req.getParameterValues(name).length!=1){
					int count=0;
					for(String parameter_value:req.getParameterValues(name)){
						request.set(name+"__"+count, parameter_value);						
						count++;
					}
					continue;
				}else{
					value=req.getParameter(name);
				}
				if (value.equalsIgnoreCase("null"))
					value = ""; //handle this in DAO
				request.set(name, value);
			}
//			//Read session and Cookie
			AS2User user = new AS2User();
//	    	HttpSession session = req.getSession(false);
//	    	if(session != null && session.getAttribute("username")!= null){
//	    		user.setUserName(session.getAttribute("username").toString());
//	    	}
			if(req.getAttribute(AS2_USERNAME)!=null)
				user.setUserName(req.getAttribute(AS2_USERNAME).toString());
	    	request.set(AS2Constants.USER_OBJ, user);
		} catch (Throwable t) {
			AS2Exception e = new AS2Exception("511");
			e.setErrorDescription(t.getMessage());
			throw e;
		}
		return request;
	}
	protected AS2Record readRequestFromObject(ServletInputStream in)
			throws AS2Exception {
		_httpTransportCallCounter++;
		AS2Record request = new AS2Record();
		try {
			ObjectInputStream objectStream = new ObjectInputStream(in);
			request = (AS2Record) objectStream.readObject();
		} catch (Throwable t) {
			AS2Exception e = new AS2Exception("511");
			e.setErrorDescription(t.getMessage());
			throw e;
		}
		return request;
	}
	protected void writeResponseAsObject(ServletResponse res,
			Object response) throws Exception {
		res.setContentType("application/octet-stream");
		java.io.ObjectOutputStream objectStream = new java.io.ObjectOutputStream(
				res.getOutputStream());
		objectStream.writeObject(response);
		objectStream.flush();
		objectStream.close();
	}
	protected void writeResponse(HttpServletResponse response,AS2Record as2_request,
			Object res, String type, String format) throws Exception {
		try {
			if(type.equalsIgnoreCase(RESPONSE_TYPE_JSON)){
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().print((JSONObject)res);
				//TODO na formi koja poziva file treba ubaciti skriveno polje FORMAT, remote object i remote method
			}else if (type.equalsIgnoreCase(RESPONSE_TYPE_BYTES)){
				byte[] byteArray = null;
				String reportDisposition =  as2_request.getAsString(AS2ReportRenderer.REPORT_DISPOSITION, AS2ReportRenderer.DEFAULT_REPORT_DISPOSITION);
				if(res instanceof JSONObject){
					JSONObject json_data_envelope = (JSONObject)res;
					JSONObject json_data = json_data_envelope.getJSONObject("response");
					JSONArray dataJson = json_data.getJSONArray("data");
					JSONObject json_record = dataJson.getJSONObject(0);
					byteArray = (byte[])json_record.get("record");
				}
				if(as2_request.get(AS2ReportRenderer.AS2_CLIENT_TYPE).equalsIgnoreCase("java")){
					//client receives pure bytes and create file on the client
//					response.setCharacterEncoding("charset=UTF-8");
					response.setContentType("text/html; charset=UTF-8");
					String content = Base64.encodeBytes(byteArray);
					PrintWriter out = response.getWriter();
					out.println(content);
					out.flush();
					out.close();
					return;
				} else {
					String fileName = AS2FileUtility.prepareFileNameTimestamp("as2");
					response.setContentType("application/" + format+"; charset=UTF-8");
					response.setContentLength(byteArray.length);
					response.setHeader("Content-disposition",reportDisposition +"; filename=\"" + URLEncoder.encode(fileName, "UTF-8") +"."+ format + "\"");
					ServletOutputStream ouputStream = response.getOutputStream();
					ouputStream.write(byteArray, 0, byteArray.length);
					ouputStream.flush();
					ouputStream.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected Object dispatchRequestToInvoker(AS2Record request, AS2InvocationContext context) throws AS2Exception {
		Object res = AS2InvokerFactory.getInstance().getInvoker(AS2InvokerFactory.INVOKER_DEFAULT).invoke(request, context);
		return res;
	}
	protected void setThreadName(String remote_addr, String remote_object,
			String remote_method) {
		String name = Thread.currentThread().getName();
		StringBuilder new_name = new StringBuilder();
		new_name.append(name);
		new_name.append(":");
		new_name.append(remote_addr);
		new_name.append(":");
		new_name.append(remote_object);
		new_name.append(":");
		new_name.append(remote_method);
		Thread.currentThread().setName(name);
	}
	protected void prepareRequestForFacade(AS2Record vo) {
		if(!vo.get("transform_to").equals("")){
			vo.set("@@transform_to",vo.get("transform_to"));
		}
		if(!vo.get("Component").equals("")){
			vo.setRemoteObject(vo.get("Component"));//TODO imena varijabli
		}
		if(!vo.get("Service").equals("")){
			vo.setRemoteMethod(vo.get("Service"));
		}
		if(!vo.get("remoteobject").equals("")){
			vo.setRemoteObject(vo.get("remoteobject"));//TODO imena varijabli
		}
		if(!vo.get("remotemethod").equals("")){
			vo.setRemoteMethod(vo.get("remotemethod"));
		}		
	}
	protected AS2InvocationContext prepareInvocationContext(AS2Record as2_request, String srvRemoteAddr){
		AS2InvocationContext as2_context = new AS2InvocationContext();
		as2_context.setThreadId(Thread.currentThread().getName());
		as2_context.setRemoteObject(as2_request.getRemoteObject());
		as2_context.setRemoteMethod(as2_request.getRemoteMethod());
		as2_context.setRemoteAddr(srvRemoteAddr);	
		return as2_context;
	}
//	@SuppressWarnings("unused")
//	public JSONObject callFacadeServer(AS2Record as2_request, HttpServletRequest req, HttpServletResponse response) throws Exception {
//		String srvRemoteAddr = null;
//		srvRemoteAddr = req.getRemoteAddr();
//		prepareRequestForFacade(as2_request);
//		Object res = null;
//		JSONObject res_data = new JSONObject();
//		JSONArray meta = null;
//		/*
//		res = J2EEApplicationControllerFactory.getInstance().getApplicationController().executeRequest(facade_request); // Execute request
//		*/
//		// Invocation Context
//		AS2InvocationContext as2_context = prepareInvocationContext(as2_request, srvRemoteAddr);
//		// Dispatch Request
//		res = dispatchRequestToInvoker(as2_request, as2_context);
//		
//		if (res != null && res instanceof AS2Record) {
//			AS2Record j2eevo = (AS2Record) res;
//			Object data = j2eevo.getProperty(AS2Record._RESPONSE);
//			JSONArray recordsArray = new JSONArray();
//			JSONObject rec;
//			if (data instanceof AS2RecordList) {
//				AS2RecordList j2eers = (AS2RecordList) data;
//				meta = new JSONArray(j2eers.getMetaDataForJSON());
//				// Data
//				Iterator<?> rows = j2eers.getRows().iterator();
//				while (rows.hasNext()) {
//					rec = new JSONObject();
//					AS2Record row = (AS2Record) rows.next();
//					JSONObject object = new JSONObject();
//					for (int i = 0; i < j2eers.getColumnCounter(); i++) {
//						String columnName = j2eers.getColumnNames().get(i).toString();
//						if(row.getProperty(columnName)!=null)
//							object.put(columnName,row.get(columnName));
//					}
//					if(object.length()!=0){
//						rec.put("record", object);
//						recordsArray.put(rec);
//					}
//				}
//			}else if (data instanceof AS2Record) {
//				AS2Record row = (AS2Record) data;
//				JSONObject object = new JSONObject();
//				rec = new JSONObject();
//				Iterator<?> e = row.getProperties().keySet().iterator();
//				while (e.hasNext()) {
//					String columnName = e.next().toString();
//					if(row.getProperty(columnName)!=null)
//						object.put(columnName,row.get(columnName));
//				}
//				if(object.length()!=0){
//					rec.put("record", object);
//					recordsArray.put(rec);
//				}
//
//			}
//			res_data.put("data", recordsArray);
//			res_data.put("status","0");
//		} else {
//			res_data=getErrorResponseObject("Greška na serveru. Problem s bazom podataka! Razlog:\n"+ res,"-1");
//		}
//		return res_data;
//	}

}
