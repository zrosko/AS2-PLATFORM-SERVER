/* @(#) J2EETransportServerJMSAsyncRunner.java	1.0. 10.6.2010.
 * Copyright 2010 Acriacom Software d.o.o.
 * All rights reserved.
 * For more information, please contact:
 * Autor: Zdravko Roško
 * adriacom.software@si.t-com.hr;zrosko@gmail.com
 */
package hr.as2.inf.server.requesthandlers.jms;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.exceptions.AS2TransportException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.invokers.AS2InvokerFactory;

import java.util.Properties;

//import javax.jms.Queue;
//import javax.jms.QueueConnection;
//import javax.jms.QueueConnectionFactory;
//import javax.jms.QueueSender;
//import javax.jms.QueueSession;
//import javax.jms.TextMessage;
/** A JMS worker thread to process a XML messages.
  */
public class AS2ServerRequestHandlerJmsAsyncRunner implements Runnable {
	public static String _class = "hr.as2.inf.server.requesthandlers.jms.J2EETransportServerJMSAsyncRunner";
		/* Thread specific. */
		private AS2Record request;
		Object response = null;
		AS2Record as2_request = null;
		String srvRemoteAddr = null;
		
//		private String _DDS_CLIENT_ID = "";
//		private String _DDS_MESSAGE_ID = "";
		/* Static. Public because need to close it at finaly of sender JMSAsync. */
//		public static QueueSender producer;
//	    public static QueueConnection connection;
//	    public static QueueSession session;
//	    public static QueueConnectionFactory connectionFactory;
//	    public static Queue destination_sender;
	    static String  _SENDER_QUEUE	= "TIBCO.DEV.ESB.DDSIntegration.Response.External.Incoming.DIS";
	    static String  _HOST	    	= "tcp://172.16.130.51:7222";
	    static String  _USER        	= "ddsems";
	    static String  _PASSWORD    	= "dds";
	    static boolean _initialized = false;

public AS2ServerRequestHandlerJmsAsyncRunner(AS2Record req, String dds_client_id,String _dds_message_id) {
	request = req;
//	_DDS_CLIENT_ID = dds_client_id;
//	_DDS_MESSAGE_ID = _dds_message_id;
	
	if(!_initialized){
		initialize(AS2Context.getInstance().getProperties());
		connect();
        _initialized = true;
	}
}
/**
 * Call the application main controller.
 */
public void run() {
    try {
		as2_request = new AS2Record();
		as2_request.setDummy(request.isDummy());
		as2_request.setRemoteObject(request.getRemoteObject());
		as2_request.setRemoteMethod(request.getRemoteMethod());
		as2_request.setProperties(request.getProperties());
		// Invocation Context
		AS2InvocationContext as2_context = prepareInvocationContext(as2_request, srvRemoteAddr);
		// Dispatch Request
		Object res = AS2InvokerFactory.getInstance().getInvoker(AS2InvokerFactory.INVOKER_DEFAULT).invoke(request, as2_context);
        if(res instanceof AS2Record){
        	AS2Record as2_res = (AS2Record)res;
        	transmitMessage((AS2Record)as2_res.getAsObject(AS2Record._RESPONSE));
        }else if(res instanceof Exception)
        	throw (Exception) res;
    } catch (Exception e) {
        e.printStackTrace();
    }
}
public void initialize(Properties p) {
	_SENDER_QUEUE = p.getProperty(_class + ".SENDER_QUEUE", _SENDER_QUEUE);
	_HOST = p.getProperty(_class + ".HOST", _HOST);
	_USER = p.getProperty(_class + ".USER", _USER);
	_PASSWORD = p.getProperty(_class + ".PASSWORD", _PASSWORD);
}
public void connect() {
	try {
//		connectionFactory = new com.tibco.tibjms.TibjmsQueueConnectionFactory(_HOST);
//	    connection = connectionFactory.createQueueConnection(_USER,_PASSWORD);
//	    session = connection.createQueueSession(false,javax.jms.Session.AUTO_ACKNOWLEDGE);
//	    destination_sender = session.createQueue(_SENDER_QUEUE);
//	    producer = session.createSender(null);
//	    /* Start */
//	    connection.start();
	} catch (Exception e) {
		e.printStackTrace();
    }
}
public static void disconnect() throws Exception {
//    try { if (producer != null) {producer.close();} } catch (Exception e) {e.printStackTrace();}
//    try { if (session != null) {session.close();} } catch (Exception e) {e.printStackTrace();}
//    try { if (connection != null) {connection.close();} } catch (Exception e) {e.printStackTrace();}
}
protected AS2InvocationContext prepareInvocationContext(AS2Record as2_request, String srvRemoteAddr){
	AS2InvocationContext as2_context = new AS2InvocationContext();
	as2_context.setThreadId(Thread.currentThread().getName());
	as2_context.setRemoteObject(as2_request.getRemoteObject());
	as2_context.setRemoteMethod(as2_request.getRemoteMethod());
	as2_context.setRemoteAddr(srvRemoteAddr);	
	return as2_context;
}
public synchronized void transmitMessage (AS2Record _msgIn) throws AS2Exception {
//	TextMessage _msg = null;
    try {
//        _msg = session.createTextMessage();
        //XML replay BEGIN
        StringBuffer _xml = new StringBuffer();
        _xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        _xml.append("<design_message>");
        //header
        _xml.append("<header>");
        _xml.append("<from>DIS");
        _xml.append("</from>");
        _xml.append("<to>DDS");
        _xml.append("</to>");
        _xml.append("<dis_component>");
        _xml.append(_msgIn.getRemoteObject());
        _xml.append("</dis_component>");
        _xml.append("<dis_service>");
        _xml.append(_msgIn.getRemoteMethod());
        _xml.append("</dis_service>");
        _xml.append("<transaction_id>");
        _xml.append(_msgIn.get("@@transactionToken"));
        _xml.append("</transaction_id>");
        //replay 
        _xml.append("<replay>");
        _xml.append("<status>");
        _xml.append(_msgIn.get("@@status"));
        _xml.append("</status>");
        _xml.append("<error_code>");
        _xml.append(_msgIn.get("@@error_code"));
        _xml.append("</error_code>");
        _xml.append("</error_message>");
        _xml.append(_msgIn.get("@@error_message"));
        _xml.append("</error_message>");
        _xml.append("</replay>");
        _xml.append("</header>");
//        _msg.setText(_xml.toString());
//        _msg.setStringProperty("DDS_CLIENT_ID", _DDS_CLIENT_ID);
//        _msg.setStringProperty("DDS_MESSAGE_ID", _DDS_MESSAGE_ID);
//        String _service = _msgIn.getService();
//        if(_service==null || _service.length()<1)
//        	_service="DDSServerAction";
//        _msg.setStringProperty("MESSAGE_TYPE", _service);
//        _msg.setStringProperty("MESSAGE_STATUS", "OK"); 
//        producer.send(destination_sender,_msg);
        StringBuffer tmp = new StringBuffer();
		tmp.append(new java.util.Date() + " " + Thread.currentThread());
		tmp.append(" --- JMS Start Service Call #: ");
		tmp.append(_msgIn.getRemoteObject());
		tmp.append(" component ");
		tmp.append(_msgIn.getRemoteMethod());
		tmp.append(" service ");
		AS2Trace.traceStringOut(AS2Trace.W, tmp.toString()); 
    } catch (Exception e) {
        AS2TransportException exc = new AS2TransportException("509");
        exc.addCauseException(e);
        throw exc;
    } finally {
        try {
            //producer.close();
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.W, "Can not close ??: " + e.toString());
        }
    }
}
}

