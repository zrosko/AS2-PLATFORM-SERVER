package hr.as2.inf.server.requesthandlers.rest.charts;

import hr.as2.inf.common.charts.AS2ChartConstants;
import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.reports.AS2ReportRenderer;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.charts.da.jdbc.AS2ChartTreeJdbc;
import hr.as2.inf.server.requesthandlers.rest.AS2ServerRequestHandlerRest;
import hr.as2.inf.server.transaction.AS2Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//@WebServlet("/json_servlet_charts")
public class AS2ServerRequestHandlerCharts extends AS2ServerRequestHandlerRest {
	private static final long serialVersionUID = -6559365501107457535L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
//		JSONArray meta = new JSONArray();
		JSONObject res_data = null;
		AS2Record request = readRequestFromURL(req);
//		prepareRequestForFacade(request);
		res_data = new JSONObject();
		JSONObject json_response = new JSONObject();
		try {
			if (request.get("_componentId").equalsIgnoreCase("pokazateljiTreeGrid"))
				res_data = servicePokazatelji(request, req, resp);
			else if(request.get("remotemethod").equalsIgnoreCase("listajOperativniTroskoviDetalji") || request.get("remotemethod").equals("citajIzvjestaj")){
				serviceAs2(req, resp);
				return;
			}
			else {
				if (request.getProperty(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE) != null) {
					res_data = serviceChartsDB(request, req, resp);
//					meta = res_data.getJSONArray("meta");
				}
			}
		} catch (Exception e) {
			try {
				json_response = getErrorResponseObject("Greška na serveru. Razlog: " + e.getMessage(),"-1");
				request.set("as2_response_type", RESPONSE_TYPE_JSON);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		try {
			if(res_data.isNull("status"))
				res_data.put("status", "0");
			json_response = formatRestHeader(res_data);
			writeResponse(resp,request,json_response, request.getAsString("as2_response_type", RESPONSE_TYPE_JSON), request.getAsString(AS2ReportRenderer.AS2_REPORT_FORMAT,AS2ReportRenderer.DEFAULT_REPORT_FORMAT));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	@SuppressWarnings("unchecked")
//	private JSONObject callFacadeServer(AS2Record facade_request, HttpServletRequest req, HttpServletResponse response) throws Exception {
//		prepareRequestForFacade(facade_request);
//		Object res = null;
//		JSONObject res_data = new JSONObject();
//		JSONArray meta = null;
//		res = J2EEApplicationControllerFactory.getInstance().getApplicationController().executeRequest(facade_request); // Execute request
//		if (res != null && res instanceof AS2Record) {
//			AS2Record j2eevo = (AS2Record) res;
//			Object data = j2eevo.getAsObject(AS2Record._RESPONSE);
//			JSONArray recordsArray = new JSONArray();
//			JSONObject rec;
//			if (data instanceof AS2RecordList) {
//				AS2RecordList j2eers = (AS2RecordList) data;
//				meta = new JSONArray(j2eers.getMetaDataForJSON());
//				// Data
//				Iterator<AS2Record> rows = j2eers.getRows().iterator();
//				while (rows.hasNext()) {
//					rec = new JSONObject();
//					AS2Record row = rows.next();
//					JSONObject object = new JSONObject();
//					for (int i = 0; i < j2eers.getColumnCounter(); i++) {
//						String columnName = j2eers.getColumnNames().get(i)
//								.toString();
//						if(!columnName.equals("")){
//							object.put(columnName,row.get(j2eers.getColumnNames().get(i).toString()));
//						}
//					}
//					if(object.length()!=0){
//						rec.put("record", object);
//						recordsArray.put(rec);
//					}
//				}
//			}else if (data instanceof AS2Record) {
//				AS2Record row = (AS2Record) data;
//				Iterator<String> e = row.keys().iterator();
//				JSONObject object = new JSONObject();
//				rec = new JSONObject();
//				while (e.hasNext()) {
//					String key = e.next();
//					if(!key.equals("")){
//						object.put(key,row.get(key));
//					}
//					
//				}
//				if(object.length()!=0){
//					rec.put("record", object);
//					recordsArray.put(rec);
//				}
//			}
//			res_data.put("data", recordsArray);
//		} else {
//			res_data.put("error","Greška na serveru. Problem s bazom podataka! Razlog: "+ res);
//		}
//		return res_data;
//	}
//	
//	private void prepareRequestForFacade(AS2Record vo) {
//		if(vo.get("transform_to").equals(""))
//			//vo.set("@@transform_to","hr.adriacomsoftware.inf.common.dto.J2EEValueObject");
//			vo.set("@@transform_to","hr.as2.inf.common.data.AS2Record");
//
//		else
//			vo.set("@@transform_to",vo.get("transform_to"));
//		if(!vo.get("Component").equals(""))
//			vo.setRemoteObject(vo.get("Component"));
//		else{
//			vo.setRemoteObject("hr.adriacomsoftware.app.server.kpi.facade.KpiFacadeServer");
//			vo.set("Component","hr.adriacomsoftware.app.server.kpi.facade.KpiFacadeServer");
//		}
//		if(!vo.get("Service").equals(""))
//			vo.setRemoteMethod(vo.get("Service"));
//		else{
//			vo.setRemoteMethod("citajVrijednostiPokazatelja");
//			vo.set("Service","citajVrijednostiPokazatelja");
//		}
//		J2EEUser user = new J2EEUser();
//		vo.set(J2EEConstants.USER_OBJ, user);
//	}

	private JSONObject serviceChartsDB(AS2Record vo,
			HttpServletRequest req, HttpServletResponse resp) throws Exception{
		JSONObject res_data = new JSONObject();
		JSONArray dataArray = new JSONArray();
		JSONArray meta = new JSONArray();
		prepareRequestForDatabase(vo);
		int chartTypeNo=1;
		String chartType="";
		Object res=null;
//		if(vo.getProperty("chartTypeNo")!=null)
//			chartTypeNo=vo.getAsInt("chartTypeNo");
		if(vo.getAsObject(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE_NO)!=null)
			chartTypeNo=vo.getAsInt(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE_NO);
		for(int i=0;i<chartTypeNo;i++){
			chartType = getChartType(vo,chartTypeNo,chartType,i);
			if (chartTypeNo!=1){
//			if (vo.getService().equals("listajKpiGrDemografijaStablo")) {//TODO promjeniti u service
				if (vo.get("razina1").equalsIgnoreCase("građani")
						&&vo.get("razina2").equalsIgnoreCase("klijenti")){
					if(chartType.equalsIgnoreCase("Line"))
						vo.set("razina3", "Demografija_vrsta_godina");
					else
						vo.set("razina3", "Demografija_vrsta_usporedba");
				}else if (vo.get("razina1").equalsIgnoreCase("banka")
						&&vo.get("razina2").equalsIgnoreCase("klijenti")){
					if(i==0){//prvi diagram
						vo.set("razina3", "Prodaja_gradani");
						vo.set("chartTitle", "Prodaja građani");
					}else if(i==1){//drugi diagram
						vo.set("razina3", "Prodaja_pravne_osobe");
						vo.set("chartTitle", "Prodaja pravne osobe");
					}else
						vo.set("razina3", "TODO");
				}else if(vo.get("razina1").equalsIgnoreCase("Naslov")){
					vo.set("title", "CIR");
					vo.set("razina2", chartType);
//					if(chartType.equals("BasicPie")){
//						vo.set("razina2", "BasicPie");
//					}else if(chartType.equals("StockLineChart")){
//						vo.set("razina2", "StockLineChart");
//					}else{
//						vo.set("razina2", "AreaLine");
//					}
				}
			//}
			}
//			vo.set("chartType", chartType);
			vo.set(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE, chartType);
			AS2Record as2_request = new AS2Record();
			as2_request.setDummy(vo.isDummy());
			as2_request.setRemoteObject(vo.getRemoteObject());
			as2_request.setRemoteMethod(vo.getRemoteMethod());
			as2_request.setProperties(vo.getProperties());
			// Invocation Context
			AS2InvocationContext as2_context = prepareInvocationContext(as2_request,req.getRemoteAddr());
			// Dispatch Request
			ServletContext context = this.getServletConfig().getServletContext();
			as2_request.set("war_path",	context.getRealPath(""));
			res = dispatchRequestToInvoker(as2_request, as2_context);
//			res = J2EEApplicationControllerFactory.getInstance().getApplicationController().executeRequest(vo); // Execute request
			if (res!=null && res instanceof AS2Record) {
				AS2Record j2eevo = (AS2Record) res;
				Object data = j2eevo.getAsObject(AS2Record._RESPONSE);
				if (data instanceof AS2RecordList) {
					AS2RecordList j2eers = (AS2RecordList) data;
					meta = new JSONArray(j2eers.getMetaDataForJSON());
					JSONObject chartRecords = new JSONObject();
					JSONObject chart = new JSONObject();
					chartRecords = getChartRecords(vo, j2eers, chartType);
					chart = getChartDetails(vo,j2eers, chartRecords, chartType);
					dataArray.put(chart);
				} else {
					HashMap<String,String> error = new HashMap<String,String>();
					error.put("error", "Greška na serveru. Problem s bazom podataka!");
					dataArray.put(error);
				}
			}
		}
		res_data.put("data",dataArray);
		res_data.put("meta", meta);
		return res_data;
	}

	private void prepareRequestForDatabase(AS2Record vo) {
		if(vo.get("transform_to").equals(""))
			vo.set("@@transform_to","hr.as2.inf.common.data.AS2Record");
		else
			vo.set("@@transform_to",vo.get("transform_to"));
		if(vo.getRemoteObject().equals(""))
			vo.setRemoteObject("hr.adriacomsoftware.app.server.kpi.facade.KpiFacadeServer");
		else
			vo.setRemoteObject(vo.get("Component"));
		if(vo.getRemoteMethod().equals(""))
			vo.setRemoteMethod("citajVrijednostiPokazatelja");
		else
			vo.setRemoteMethod(vo.get("Service"));

		AS2User user = new AS2User();
		vo.set(AS2Constants.USER_OBJ, user);

	}

	private String getChartType(AS2Record vo, int chartTypeNo,
			String chartType, int i) {
		if (i == 0) {
			chartType = vo.get(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE);
		}
		if (vo.getProperty(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE + i) != null) {
			chartType = vo.get(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE + i);

		}
		return chartType;
	}

	private JSONObject getChartRecords(AS2Record vo, AS2RecordList rs,String chartType) throws JSONException {
		JSONObject chartRecords = new JSONObject();
		if (chartType.equalsIgnoreCase("BasicPie")){
			chartRecords = getPieChartRecordsArray(vo, rs);
		}else if(chartType.equalsIgnoreCase("StockLineChart")){
			chartRecords = getStockLineChartRecordsArray(vo, rs);
		}else {
			chartRecords = getChartRecordsArray(vo, rs);
		}
		return chartRecords;
	}

	private JSONObject getChartDetails(AS2Record vo,AS2RecordList j2eers,JSONObject chartRecords,String chartType) throws JSONException {
		JSONObject chart=new JSONObject();
		JSONArray chartArray = new JSONArray();
		JSONObject details = new JSONObject();
		if(!chartRecords.isNull("x"))
			details.put("x", chartRecords.get("x"));
		else{
			details.put("x","");
		}
		if(!chartRecords.isNull("oznake") && chartRecords.getJSONObject("oznake").length()!=0)
			details.put("oznake", chartRecords.get("oznake"));
		if(j2eers.getRowAt(0)!=null)
			details.put("yTitle", j2eers.getRowAt(0).get("y_naslov"));
		else
			details.put("yTitle", "");
		details.put(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE, chartType);
		if(!vo.get("chartTitle").equals(""))
			details.put("title", vo.get("chartTitle"));
		else
			details.put("title", details.get("yTitle"));
		details.put("pokazateljId",vo.get("pokazateljId"));
		details.put("subtitle", getSubtitle(vo));
		details.put("razina",vo.get("razina1")+"/"+vo.get("razina2")+"/"+vo.get("razina3"));
		JSONObject chartDetails = new JSONObject();
		chartDetails.put("chartDetails",details);
		chartArray.put(chartDetails);
		JSONObject tempObject = new JSONObject();
		tempObject.put("records",chartRecords.get("data"));
		chartArray.put(tempObject);
		chart.put("chart", chartArray);
		JSONObject record = new JSONObject();
		record.put("record", chart);
		return record;
	}

	private String getSubtitle(AS2Record vo) {
		int count=1;
		String subtitle="";
		while(!vo.get("chartToolStripItem"+count).equals("")){
			String item =  vo.get("chartToolStripItem"+count);
			if(!item.equals("")){
				String itemName = item.substring(0, 1);
				itemName = itemName.toUpperCase();
				itemName = itemName + item.substring(1, item.length());
				itemName = itemName.replace("_", " ");
				subtitle = subtitle + itemName+ ": "+ vo.get(item);
				if(!vo.get(vo.get("chartToolStripItem"+(count+1))).equals(""))
					subtitle = subtitle + ", ";
			}
			count++;
		}
		return subtitle;
	}

	private JSONObject getChartRecordsArray(AS2Record facade_request,AS2RecordList j2eers) throws JSONException {
		JSONArray y;
		JSONObject oznake = new JSONObject();
		// Data
		JSONObject chartRecords = new JSONObject();
		JSONArray recordsArray = new JSONArray();

		for(int i=0;i<j2eers.getColumnCounter();i++){
			Iterator<AS2Record> a = j2eers.getRows().iterator();
			y = new JSONArray();
			
			while (a.hasNext()) {
				AS2Record temp = a.next();
				if(j2eers.getMetaDataForName(j2eers.getColumnNames().get(i).toString()).getColumnTypeName().contains("decimal")){
					y.put(temp.getAsDouble(j2eers.getColumnNames().get(i).toString()));
				}else
					y.put(temp.get(j2eers.getColumnNames().get(i).toString()));
				if(!temp.get("oznaka").equals("")){
					oznake.put(temp.get("x"),temp.get("oznaka"));
				}
			}
			JSONObject object = new JSONObject();
			if(j2eers.getColumnNames().get(i).toString().equals("x")){
				chartRecords.put("x", y);
			}else if(j2eers.getColumnNames().get(i).toString().startsWith("#")){
				String columnName = j2eers.getColumnNames().get(i).toString();
				columnName = columnName.substring(1, columnName.length());
				object.put(columnName, y);
				object.put("yType", j2eers.getMetaDataForName(j2eers.getColumnNames().get(i).toString()).getColumnTypeName());
				object.put("title", columnName);
				JSONObject record = new JSONObject();
				record.put("record",object);
				recordsArray.put(record);
			}
		}
		if(oznake.length()!=0)
			chartRecords.put("oznake",oznake);
		chartRecords.put("data", recordsArray);
		return chartRecords;
	}

//	@SuppressWarnings("unchecked")
//	private JSONObject getChartRecordsArrayYnaslov(AS2Record facade_request,AS2RecordList j2eers) throws JSONException {
//		JSONArray x = new JSONArray();
//		JSONObject oznake = new JSONObject();
//		JSONArray y;
//		int count = 1;
//		int xcount = 0;
//		// Data
//		JSONObject chartRecords = new JSONObject();
//		JSONArray recordsArray = new JSONArray();
//		Iterator<AS2Record> a = j2eers.getRows().iterator();
//		while (a.hasNext()) {
//			AS2Record temp = a.next();
//			y = new JSONArray();
//			if (!j2eers.getColumnNames().contains(
//					"y" + count + "_naziv")) {
//				break;
//			}
//			if (temp.getProperty("y" + count + "_naziv") == null) {
//				count++;
//				continue;
//			}
//			Iterator<AS2Record> b = j2eers.getRows().iterator();
//
//			while (b.hasNext()) {
//				AS2Record temp1 = b.next();
//				y.put(temp1.getAsDouble("y" + count));
//				if(xcount==0)
//					x.put(temp1.get("x"));
//				if(!temp1.get("oznaka").equals("")){
//					oznake.put(temp1.get("x"),temp1.get("oznaka"));
//				}
//			}
//			xcount=1;
//			// y
//			JSONObject object = new JSONObject();
//			object.put("y", y);
//			object.put("yType", "Double");
//			object.put("title", temp.get("y" + count + "_naziv"));
//			JSONObject record = new JSONObject();
//			record.put("record",object);
//			recordsArray.put(record);
//			count++;
//		}
//		chartRecords.put("x", x);
//		chartRecords.put("oznake",oznake);
//		//*chartRecords.put("records", recordsArray);
//		chartRecords.put("data", recordsArray);
//		return chartRecords;
//	}

	private JSONObject getPieChartRecordsArray(AS2Record facade_request,AS2RecordList j2eers) throws JSONException {
		String x="";
		// Data
		JSONObject chartRecords=new JSONObject();
		JSONArray recordsArray = new JSONArray();
		Iterator<AS2Record> a = j2eers.getRows().iterator();
		while (a.hasNext()) {
			AS2Record temp = a.next();
			// y
			JSONObject object = new JSONObject();
			object.put("y", temp.getAsDouble("y"));
			object.put("yType", "Double");
			x = temp.get("x");
			object.put("x", x );
			object.put("title", x );
			JSONObject record = new JSONObject();
			record.put("record",object);
			recordsArray.put(record);
		}
		chartRecords.put("x", x);
		//*chartRecords.put("records", recordsArray);
		chartRecords.put("data", recordsArray);
		return chartRecords;
	}
	
	private JSONObject getStockLineChartRecordsArray(AS2Record facade_request,AS2RecordList j2eers) throws JSONException {
		List<String> x = new ArrayList<String>();
		List<Double> y = new ArrayList<Double>();
		JSONObject object = new JSONObject();
		int count =0;
		JSONObject chartRecords=new JSONObject();
		JSONArray recordsArray = new JSONArray();
		Iterator<AS2Record> a = j2eers.getRows().iterator();
		while (a.hasNext()) {
			AS2Record temp = a.next();
			y.add(temp.getAsDouble("y"));
			x.add(temp.get("x"));
			if(count==0){
				object.put("title", temp.get("y_naslov"));
				count++;
			}
 		}
		object.put("yType", "Double");
		object.put("y", y.toArray());
		object.put("x", x.toArray());
		JSONObject record = new JSONObject();
		record.put("record",object);
		recordsArray.put(record);
		chartRecords.put("data", recordsArray);
		return chartRecords;
	}
	

//	private void prepareJSONresponse(HttpServletResponse resp,
//			JSONObject json_data, JSONArray res_meta)
//			throws JSONException {
//		JSONObject json_response = new JSONObject();
//		JSONObject json_header = new JSONObject();
//		json_header.put("status", "0");//json_data.getString("status"));// 0=STATUS_SUCCESS,-90=STATUS_TRANSPORT_ERROR, -1=SERVER_FAILURE
//		json_header.put("startRow", 0);
//		if (!json_data.isNull("data") && json_data.getJSONArray("data") != null) {
//			JSONArray dataJson = json_data.getJSONArray("data");
//			json_header.put("totalRows", dataJson.length());
//			json_header.put("data", dataJson);
//			json_header.put("endRow", dataJson.length());
//			
//		} else if (!json_data.isNull("errors") && json_data.getString("errors").length() != 0) {
//			json_header.put("totalRows", 1);
//			json_header.put("data", json_data.getString("errors"));
//			json_header.put("endRow", 1);
//		}
//		json_response.put("response", json_header);
//		try {
//			resp.setContentType("application/json; charset=UTF-8");
//			resp.getWriter().print(json_response);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	private JSONObject servicePokazatelji(AS2Record facade_request, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		JSONArray pokazatelji = new JSONArray();
		JSONObject pokazatelj = new JSONObject();
		JSONObject pokazatelj_data = new JSONObject();
		JSONObject res_data = new JSONObject();
		AS2RecordList j2eers = null;
		AS2Transaction.begin();
		AS2ChartTreeJdbc dao = new AS2ChartTreeJdbc();
		AS2Record vo_tree = new AS2Record();
		vo_tree.set("application", facade_request.get("application_id"));
		facade_request.set(AS2Constants.FIND_CRITERIA, vo_tree);
		facade_request.set(AS2Constants.ORDER_BY_CLAUSE, "order by order_by");
		try{
			j2eers = dao.daoFind(facade_request);
		}catch(Exception e){
			AS2Transaction.rollback();
		}
		AS2Transaction.commit();
		Iterator<AS2Record> E = j2eers.getRows().iterator();
		while (E.hasNext()){
			AS2Record row = E.next();
			pokazatelj = new JSONObject();
			pokazatelj_data = new JSONObject();
			//TODO security
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__ID,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__ID));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__PARENT_ID,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__PARENT_ID));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__NAME,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__NAME));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__ICON,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__ICON));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE1,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE1));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__TAB,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__TAB));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE_NO,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__CHART_TYPE_NO));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__TOOL_STRIP_TYPE,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__TOOL_STRIP_TYPE));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__SERVICE,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__SERVICE));
			pokazatelj_data.put(AS2ChartConstants.AS2_CHART_TREE__COMPONENT,row.getAsString(AS2ChartConstants.AS2_CHART_TREE__COMPONENT));
			//pokazatelj.put("pokazatelj", pokazatelj_data);
			pokazatelj.put("record", pokazatelj_data);
			pokazatelji.put(pokazatelj);
		}
		res_data.put("data",pokazatelji);
		return res_data;
	}
}