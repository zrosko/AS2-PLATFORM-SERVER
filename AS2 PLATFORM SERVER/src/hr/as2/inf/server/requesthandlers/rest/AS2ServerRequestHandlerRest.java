package hr.as2.inf.server.requesthandlers.rest;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.data.valuelist.AS2ValueListInfo;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.reports.AS2ReportRenderer;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.da.valuelisthandler.AS2ValueListHandler;
import hr.as2.inf.server.requesthandlers.AS2ServerRequestHandler;
import hr.as2.inf.server.security.AS2SecurityConstants;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//json_servlet?Service=izracunajPlanOtplate&Component=hr.adriacomsoftware.app.server.kalkulatori.facade.KalkulatoriFacadeServer&transform_to=hr.adriacomsoftware.app.common.kalkulatori.dto.FinancijskiKalkulatorVo&anuiteti_vrsta=dek&iznos_kredita=123123&kamatna_stopa=11&period_kamatne_stope=g&rok_otplate=1&period_otplate=g&nacin_otplate=2&vrsta_obracuna=k&_operationType=custom&_operationId=izracunajPlanOtplate&_textMatchStyle=exact&_componentId=isc_AS2DynamicForm_0&_dataSource=AnuitetiModel&isc_metaDataPrefix=_&isc_dataFormat=json%20HTTP/1.1
@SuppressWarnings("serial")
//@WebServlet("/json_servlet")
public class AS2ServerRequestHandlerRest extends AS2ServerRequestHandler {
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		serviceAs2(req,resp);
	}	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		serviceAs2(req,resp);
	}
	protected void serviceAs2(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		AS2Record request = null;
		Object response = null;
		JSONObject json_response = null;
		AS2Record as2_request = null;
		String srvRemoteAddr = null;
		long mStart = System.currentTimeMillis();
		try {
			srvRemoteAddr = req.getRemoteAddr();
			request = readRequestFromURL(req);
			prepareRequestForFacade(request);
			// Change thread name
			setThreadName(srvRemoteAddr, request.getRemoteObject(),request.getRemoteMethod());
			StringBuilder tmp = new StringBuilder();
			tmp.append(new java.util.Date() + " " + Thread.currentThread());
			tmp.append(" +++ AS2ServerRequestHandlerRest Start HTTP Start Service Call #: ");
			tmp.append(_httpTransportCallCounter);
			tmp.append(" from ");
			tmp.append(srvRemoteAddr);
			if(request.getProperty(AS2Constants.USER_OBJ)!=null){
				AS2User user = (AS2User)request.getProperty(AS2Constants.USER_OBJ);
				tmp.append(" Username = " + user.getUserName());
			}
			tmp.append(" Remote Method = ");
			try {
				tmp.append(request.getRemoteObject());
				tmp.append(".");
				tmp.append(request.getRemoteMethod());
			} catch (Exception e0) {
				tmp.append("UNKNOWN Remote Method");
			}
			AS2Trace.traceStringOut(AS2Trace.W, tmp.toString());
			if(req.getAttribute(AS2SecurityConstants.AS2_USER_AUTHORIZED)!=null 
					&& req.getAttribute(AS2SecurityConstants.AS2_USER_AUTHORIZED).equals(false)){ //LOGIN REQUIRED
				response = new AS2Exception("551");
			}else{
				as2_request = new AS2Record();
				as2_request.setDummy(request.isDummy());
				as2_request.setRemoteObject(request.getRemoteObject());
				as2_request.setRemoteMethod(request.getRemoteMethod());
				as2_request.setProperties(request.getProperties());
				// Invocation Context
				AS2InvocationContext as2_context = prepareInvocationContext(as2_request, srvRemoteAddr);
				// Dispatch Request
				ServletContext context = this.getServletConfig().getServletContext();
				as2_request.set("war_path",	context.getRealPath(""));
				response = dispatchRequestToInvoker(as2_request, as2_context);
			}
			
		} catch (Exception e) {
			response = e;
			//getErrorResponseObject("Greška na serveru. Razlog:\n"+ e.getMessage(), "-90");
		} finally {
				//TODO meta data - JSONArray meta = new JSONArray();
				//TODO response to JSONObject
//						prepareJSONresponse(response, ((JSONObject)response).getString("status"));
			try {
				json_response = formatRestHeader(transformToRest(request,response));
				//TODO citati ulazni parametar i zamjeniti RESPONSE_TYPE_JSON
				writeResponse(res,request,json_response, request.getAsString("as2_response_type", RESPONSE_TYPE_JSON), request.getAsString(AS2ReportRenderer.AS2_REPORT_FORMAT,AS2ReportRenderer.DEFAULT_REPORT_FORMAT)); 
			} catch (Exception endException) {
				endException.printStackTrace();
			}
			StringBuilder tmp = new StringBuilder();
			tmp.append(new java.util.Date() + " " + Thread.currentThread());
			tmp.append(" --- AS2ServerRequestHandlerRest End HTTP End Service ");
			tmp.append(" from ");
			tmp.append(srvRemoteAddr);
			if(request.getProperty(AS2Constants.USER_OBJ)!=null){
				AS2User user = (AS2User)request.getProperty(AS2Constants.USER_OBJ);
				tmp.append(" Username = " + user.getUserName());
			}
			tmp.append(" Remote Method = ");
			try {
				tmp.append(request.getRemoteObject());
				tmp.append(".");
				tmp.append(request.getRemoteMethod());
			} catch (Exception e1) {
				tmp.append("UNKNOWN Remote Method ");
			}
			tmp.append(" time ");
			tmp.append((System.currentTimeMillis() - mStart + " mills"));
			AS2Trace.traceStringOut(AS2Trace.W, tmp.toString());
		}
		AS2Trace.trace(AS2Trace.I, " AS2ServerRequestHandlerRest.service end");
		request = null; // garbage collection
		response = null; // garbage collection
		as2_request = null; // garbage collection
	}

	protected JSONObject transformToRest(AS2Record request, Object response) throws Exception{
		JSONArray json_meta_array = null;
		JSONArray json_data_array = null;
		JSONObject json_response = null;
		//Exception handling
		if(response instanceof AS2Exception){
			json_response = getErrorResponseObject(((AS2Exception)response).getDetailDescription(),"-"+ ((AS2Exception)response).getErrorCode());
			request.set("as2_response_type", RESPONSE_TYPE_JSON);
		}else if(response instanceof Exception){
			json_response = getErrorResponseObject(((Exception)response).getMessage(), "-1");//TODO status
			request.set("as2_response_type", RESPONSE_TYPE_JSON);
		}else if(response instanceof AS2Record){
			AS2Record as2_record = (AS2Record) response;
			Object data = as2_record.getProperty(AS2Record._RESPONSE);
			json_data_array = new JSONArray();
			JSONObject rec = null;
			if (data instanceof AS2RecordList) {
				AS2RecordList as2_recordlist = (AS2RecordList) data;
				//meta
//				json_meta_array = new JSONArray();
//				for(String column:as2_recordlist.getMetaData().keySet()){
//					AS2MetaData metaData = as2_recordlist.getMetaData(column);
//					JSONObject meta = new JSONObject();
//					meta.put("_columnLabel",metaData.getColumnLabel());
//					meta.put("_columnTypeName",metaData.getColumnTypeName());
//					meta.put("_columnType",metaData.getColumnType());
//					meta.put("_columnDisplaySize",metaData.getColumnDisplaySize());
//					json_meta_array.put(meta);
//				}
//				JSONObject rec_meta = new JSONObject();
//				rec_meta.put("meta", json_meta_array);
//				json_data_array.put(rec_meta);
				//meta
				Iterator<AS2Record> rows = as2_recordlist.iteratorRows();
				while (rows.hasNext()) {
					AS2Record row =  rows.next();
					JSONObject json_row = new JSONObject();
					Iterator<String> iteratorKeys = row.iteratorKeys();
					while (iteratorKeys.hasNext()) {
						String key = iteratorKeys.next();
						if(row.getProperty(key)!=null)
							json_row.put(key,row.get(key));
					}
					rec = new JSONObject();
					if(json_row.length()!=0){
						rec.put("record", json_row);
						json_data_array.put(rec);
					}
				}
			}else if (data instanceof AS2Record) {
				AS2Record row = (AS2Record) data;
				JSONObject json_row = new JSONObject();
				Iterator<String> iteratorKeys = row.iteratorKeys();
				while (iteratorKeys.hasNext()) {
					String key = iteratorKeys.next();
					json_row.put(key,row.get(key));
				}
				rec = new JSONObject();
				if(json_row.length()!=0){
					rec.put("record", json_row);
					json_data_array.put(rec);
				}
			}else if (data instanceof byte[]) {//TODO novo testirati
				rec = new JSONObject();
				rec.put("record", data);
				json_data_array.put(rec);
			}else{
				rec = new JSONObject();
				rec.put("record", "");
				json_data_array.put(rec);
			}
			 json_response = new JSONObject();
			 json_response.put("data", json_data_array);
			 json_response.put("status","0");
			//need paging
		    if (AS2ValueListHandler.getInstance().getPropertyValue(request.getComponentAndService()) != null){
				AS2ValueListInfo info = (AS2ValueListInfo)as2_record.getAsObject(AS2Record._VALUE_LIST_INFO);
				json_response.put("startRow",request.get("_startRow"));
				json_response.put("endRow",request.get("_endRow"));
//				if(info.getPosition()+info.getSize()-1>info.getMax())
//					json_response.put("endRow",info.getMax());
//				else
//					json_response.put("endRow",info.getPosition()+info.getSize()-1);
				json_response.put("totalRows",info.getTotalRows());
		    }
		}
		return json_response;
	}
	//TODO Koristimo kod add ili updatea //// u obliku field_name:errorMessage, field_name naziv polja u recordu na kojem se dogodila pogreška
//	private JSONObject getErrorServerValidationResponseObject(String message, String status) throws JSONException {
//	}
	
	protected JSONObject getErrorResponseObject(String message, String status) throws JSONException {
		JSONObject object = new JSONObject();
		object.put("errors", message);
		object.put("status",status);	
		return object;
	}

	protected JSONObject formatRestHeader(JSONObject json_data)
			throws JSONException {
		JSONObject json_response = new JSONObject();
		JSONObject json_header = new JSONObject();
		json_header.put("status", json_data.getInt("status"));// 0=STATUS_SUCCESS,-90=STATUS_TRANSPORT_ERROR, -1=SERVER_FAILURE, -7=STATUS_LOGIN_REQUIRED
		if(!json_data.isNull("startRow"))
			json_header.put("startRow", json_data.getString("startRow"));
		else
			json_header.put("startRow", 0);
		if (!json_data.isNull("data") && json_data.getJSONArray("data") != null) {
			JSONArray dataJson = json_data.getJSONArray("data");
			if(!json_data.isNull("totalRows"))
				json_header.put("totalRows", json_data.getString("totalRows"));
			else
				json_header.put("totalRows", dataJson.length());
			json_header.put("data", dataJson);
			if(!json_data.isNull("totalRows"))
				json_header.put("endRow", json_data.getString("endRow"));
			else
				json_header.put("endRow", dataJson.length());
		} else if (!json_data.isNull("errors") && json_data.getString("errors").length() != 0) {
			json_header.put("totalRows", 1);
			json_header.put("data", json_data.getString("errors"));
			json_header.put("endRow", 1);
		}
		return json_response.put("response", json_header);
	}
}

