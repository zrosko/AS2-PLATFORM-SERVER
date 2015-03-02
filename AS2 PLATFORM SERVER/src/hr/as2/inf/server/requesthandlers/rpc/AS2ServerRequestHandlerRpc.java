package hr.as2.inf.server.requesthandlers.rpc;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.invokers.AS2InvokerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AS2ServerRequestHandlerRpc {/*extends RemoteServiceServlet implements AS2TransportClientGwtRpc {

	private static final long serialVersionUID = 6484778042110838474L;
	private static long _gwtRpcTransportCallCounter = 0;

//	@Override
//	public List<AS2GwtValueObject> fetch() {
//		AS2GwtValueObject req = new  AS2GwtValueObject();
//		AS2GwtResultSet list = (AS2GwtResultSet)service(req);
//		return list.returnList();
//	}

	@Override
	public AS2GwtValueObject fetch() {
		AS2GwtValueObject req = new  AS2GwtValueObject();
		AS2GwtValueObject list = service(req);
		return list;

	}

	@Override
	public AS2GwtValueObject fetch(AS2GwtValueObject data) {
//		J2EETransaction.begin();
//		J2EEKomponentaVo vo = new J2EEKomponentaVo();
//		J2EEUser _user = new J2EEUser();
//		_user.setApplication("zah");
//		vo.set(J2EEConstants.USER_OBJ, _user);
//		J2EEKomponentaRs rs = RBACSecurityAuthorizationFacadeServer
//				.getInstance().procitajSveKomponente(vo);
//		AS2DataTransferObjectList list = new AS2DataTransferObjectList(rs);
//		J2EETransaction.commit();
//		AS2ValueObject req = new  AS2ValueObject();
//		req.setService("procitajSveKategorije");
//		req.setComponent("hr.adriacomsoftware.app.server.or.facade.OrMetodologijaFacadeServer");
//		AS2GwtResultSet list = new AS2GwtResultSet();
////		for(int i=0;i<data.getSize();i++)
////			data.get

		return service(data);
	}


	@Override
	public AS2GwtValueObject add(AS2GwtValueObject data) {
		return service(data);
	}

	@Override
	public void remove(AS2GwtValueObject data) {
		service(data);
	}

	@Override
	public AS2GwtValueObject update(AS2GwtValueObject data) {
		return service(data);
	}

	@SuppressWarnings("unchecked")
	private AS2GwtValueObject service(AS2GwtValueObject req) {
		AS2Record request = new AS2Record();
		//TODO temp
		J2EEUser user = new J2EEUser();
		request.set(J2EEConstants.USER_OBJ, user);
		AS2GwtValueObject res = new AS2GwtValueObject();
		Object response = null;
		String srvRemoteAddr = null;
		_gwtRpcTransportCallCounter++;
		long mStart = System.currentTimeMillis();

		try {
			String name;
			request.setRemoteObject(req.getComponent());
			request.setRemoteMethod(req.getService());

	        Iterator<String> E = req.keys();
	        while (E.hasNext()) {
	            name = E.next();
	            Object value = req.getAttribute(name);
                request.set(name,value);
	        }

			srvRemoteAddr = "";//req.getRemoteAddr();
			J2EETrace.trace(J2EETrace.I, "AS2TransportServerGwtRpc.service start");
			StringBuffer tmp = new StringBuffer();
			tmp.append(new java.util.Date() + " " + Thread.currentThread());
			tmp.append(" +++ GWT RPC Start Service Call from ");
			tmp.append(srvRemoteAddr);
			tmp.append(" Service = ");
			try{
				tmp.append(request.getRemoteObject());
				tmp.append(".");
				tmp.append(request.getRemoteMethod());
			}catch(Exception e0){
				tmp.append("UNKNOWN Service");
			}

			J2EETrace.traceStringOut(J2EETrace.W, tmp.toString());
			Object facade_response = AS2InvokerFactory.getInstance().getInvoker(null).invoke(request, null);
			//response = J2EEApplicationControllerFactory.getInstance().getApplicationController().executeRequest(request);	//Execute request
		} catch (Exception e) {
			J2EETrace.trace(J2EETrace.E, e, " AS2TransportServerGwtRpc.service ");
			response = e;
		} finally {
			try{
				if(response instanceof AS2Exception){
					res.setStatus("E");
					res.setMessage(response.toString());//TODO
				}else{
					response = ((AS2Record) response).getAsObject(AS2Record._RESPONSE);
					if (response instanceof AS2RecordList){
						//**************Convert J2EEResultSet to AS2GwtResultSet**************
						AS2GwtResultSet rs = new AS2GwtResultSet();
						Iterator<AS2Record> E = ((AS2RecordList)response).getRows().iterator();
						while (E.hasNext())	{
							AS2Record row = E.next();
							AS2GwtValueObject cvo = new AS2GwtValueObject();
							cvo.setAllAttributes((LinkedHashMap<String,String>)((Map)row.getProperties()));
							rs.addRow(cvo);
						}
						//ColumnSizes, ColumnNames, MetaData
						rs.setColumnSizes(((AS2RecordList) response).getColumnSizes());
						rs.setColumnNames(((AS2RecordList) response).getColumnNames());
						rs.setMetaData(((AS2Record) response).getMetaData());
						//**************Convert J2EEResultSet to AS2GwtResultSet**************
						res = rs;
					}else if (response instanceof AS2Record){
						//**************Convert AS2Record to AS2GwtValueObject**************
						AS2GwtResultSet rs = new AS2GwtResultSet();
						AS2GwtValueObject vo = new AS2GwtValueObject();
						Set<String> E = ((AS2Record)response).keys();
				        for (String name : E){
				            Object value = ((AS2Record)response).getAsObject(name);
				            vo.setAttribute(name,value);
				        }
				        rs.addRow(vo);
				        rs.setMetaData(((AS2Record)response).getMetaData());
				        rs.setColumnNames(new ArrayList<String>(rs.getMetaData().keySet()));
				        //**************Convert AS2Record to AS2GwtValueObject**************
				        res=rs;
					}else {
						AS2GwtResultSet rs = new AS2GwtResultSet();
						rs.addRow(new AS2GwtValueObject());
						res = rs;
					}
				}
			}catch(Exception endException){
				J2EETrace.trace(J2EETrace.E, endException, " AS2TransportServerGwtRpc.service.writeResponse ");
			}
			StringBuffer tmp = new StringBuffer();
			tmp.append(new java.util.Date() + " " + Thread.currentThread());
			tmp.append(" --- GWT RPC End Service Call #: ");
			tmp.append(_gwtRpcTransportCallCounter);
			tmp.append(" from ");
			tmp.append(srvRemoteAddr);
			tmp.append(" Service = ");
			try{
				tmp.append(request.getRemoteObject());
				tmp.append(".");
				tmp.append(request.getRemoteMethod());
			}catch(Exception e1){
				tmp.append("UNKNOWN Service");
			}
			tmp.append(" time ");
			tmp.append((System.currentTimeMillis() - mStart + " mills"));

			J2EETrace.traceStringOut(J2EETrace.W, tmp.toString());
		}
		J2EETrace.trace(J2EETrace.I, " _gwtRpcTransportCallCounter.service end");
		request = null; // garbage collection
		response = null; // garbage collection
		return res;
	}

//	@SuppressWarnings("rawtypes")
//	private LinkedHashMap<String,String> convertHashtableToHashMap(Hashtable<?, ?> hashtable){
//		LinkedHashMap<String,String> hashMap = new LinkedHashMap<String,String>();
//		Iterator<?> it = hashtable.entrySet().iterator();
//	    while (it.hasNext()) {
//	        Map.Entry pairs = (Map.Entry)it.next();
//	        hashMap.put(pairs.getKey().toString(),(pairs.getValue()!=null)?pairs.getValue().toString():null);
//	        it.remove(); // avoids a ConcurrentModificationException
//	    }
//	    return hashMap;
//
//	}
	*/
}

