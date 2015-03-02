
/* @(#) J2EETransportServerJMSAsync.java	1.0. 10.6.2010.
 * Copyright 2010 Acriacom Software d.o.o.
 * All rights reserved.
 * For more information, please contact:
 * Autor: Zdravko Roško
 * adriacom.software@si.t-com.hr;zrosko@gmail.com
 */
package hr.as2.inf.server.requesthandlers.jms;
/** A JMS transport server listener. Used to listen on XML messages
  * and to start the processing thread. The reply to the client is done 
  * inside the newly create thread (writting to a queue).
  */
import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.logging.AS2Trace;

import java.util.Properties;

//import javax.jms.ExceptionListener;
//import javax.jms.JMSException;
//import javax.jms.Message;
//import javax.jms.MessageListener;
//import javax.jms.Queue;
//import javax.jms.QueueConnection;
//import javax.jms.QueueConnectionFactory;
//import javax.jms.QueueReceiver;
//import javax.jms.QueueSession;
//import javax.jms.TextMessage;

public class AS2ServerRequestHandlerJmsAsync {// implements ExceptionListener, MessageListener {
	//used as a prefix for property names inside J2EEContext.property file 
	private static String _class = "hr.as2.inf.server.requesthandlers.jms.J2EETransportServerJMSAsync";
	private static AS2ServerRequestHandlerJmsAsync _instance = null; //singleton
	//private static AS2Context CONTEXT_KEEPER; //needed to keep the reference to singleton classes
	//private static long jmsTransportCallCounter = 0; //counter for a number of inovcations of this transport
	/* Default setup values, replaced by values from J2EEContext.properties
	 * at "initialize" method in case the values are defined inside the
	 * J2EEContext.properties files.
	 */
    String  _HOST	    	= "tcp://172.16.130.51:7222";
    String  _USER        	= "ddsems";
    String  _PASSWORD    	= "dds";
    String  _RECEIVER_QUEUE	= "TIBCO.DEV.ESB.DDSIntegration.Request.External.Outgoing.DIS";
    /*-----------------------------------------------------------------------
     * Variables
     *----------------------------------------------------------------------*/
//    QueueConnectionFactory 	connectionFactory = null;
//    QueueConnection      	connection = null;
//    QueueSession         	session = null;
//    QueueReceiver 			consumer = null;
//    Queue     				destination_receiver = null;
    //initialization
    static boolean _initialized = false;
    static boolean _user_listener = false;//false is default
    static boolean _stop_it = false;
    /* Constructor. 
     */
    private AS2ServerRequestHandlerJmsAsync() throws Exception {
    	//CONTEXT_KEEPER = AS2Context.getInstance();
        AS2Context.setSingletonReference(this);
        initialize(AS2Context.getInstance().getProperties());
        _initialized = true;
        init();
    }
    /* Singleton.
     */
    public static AS2ServerRequestHandlerJmsAsync getInstance() {
    	
    	AS2Trace.trace(AS2Trace.W,"J2EETransportServerJMSAsync.init: getInstance");
    	if (_instance == null){
    		try {
    			_instance = new AS2ServerRequestHandlerJmsAsync();
    		} catch (Exception e) {
    			AS2Trace.trace(AS2Trace.E,"J2EETransportServerJMSAsync.init: getInstance EXCEPTION "+e);
    			e.printStackTrace();
    			AS2Trace.trace(AS2Trace.E, "J2EETransportServerJMSAsync.Constructor ? " + e.getMessage());
    		}
    	}
    	return _instance;
    }
    /* Called by console or API to stop the transport.
     */
    public void setStopIt(boolean value){
    	_stop_it = value;
    }
    /* Uses J2EEContext.properties setup properties to replace hardcoded values. 
     */
    protected void initialize(Properties p) {
    	_RECEIVER_QUEUE = p.getProperty(_class + ".RECEIVER_QUEUE", _RECEIVER_QUEUE);
    	_HOST = p.getProperty(_class + ".HOST", _HOST);
    	_USER = p.getProperty(_class + ".USER", _USER);
    	_PASSWORD = p.getProperty(_class + ".PASSWORD", _PASSWORD);//TODO decription
    }
	/* The 2 options while listening to incomming messages from a client.
	 * 1. Use a listener by implementing javax.jms.MessageListener (not the defalut here).
	 * 2. Use a local loop to receive the incomming messages.
	 * Second options is tested more and looks more reliable.
	 */
    private void init() throws Exception {
    	AS2Trace.trace(AS2Trace.W, "J2EETransportServerJMSAsync.init: start");
//    	connectionFactory = new com.tibco.tibjms.TibjmsQueueConnectionFactory(_HOST);
//        connection = connectionFactory.createQueueConnection(_USER,_PASSWORD);
//        session = connection.createQueueSession(false,javax.jms.Session.AUTO_ACKNOWLEDGE);
//        destination_receiver = session.createQueue(_RECEIVER_QUEUE);
//        consumer = session.createReceiver(destination_receiver);  
        
    	if(_user_listener){
//	        connection.setExceptionListener(this);
//        	consumer.setMessageListener(this);
//          connection.start();
        }else{
//            connection.start();
        	
        	while (!_stop_it) {//keep receiving messages till setStopIt(true)
        	    Thread.sleep(5000);
        	    System.out.println("SPAVA");
//				Message msg = consumer.receive(1000);
//				if (msg != null) {
//					if (msg instanceof TextMessage) {
//						onMessage(msg);
//					} else {
//						J2EETrace.trace(J2EETrace.W, "J2EETransportServerJMSAsync.init: invalid message type");
//					}
//				}
			}
        }
    	AS2Trace.trace(AS2Trace.W, "J2EETransportServerJMSAsync.init: stop");
    }
    /* When garbage collected do cleanup (close all open resources).
     * (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    public void finalize() {
    	try {    
    	    System.out.println("SPAVA:FINALIZE");
   			destroy();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    /* Closes all open resources (Queue, Session, Connection,...)
     */
    public void destroy() {
    	AS2Trace.trace(AS2Trace.W, "J2EETransportServerJMSAsync.destroy: start");
    	try{
    	    System.out.println("SPAVA:DESTROY");
    		setStopIt(true);//signal to stop the loop
//	   	    if(consumer != null) consumer.close();
//	        if(session != null) session.close();
//	        if(connection != null) connection.close();
//	        if(J2EETransportServerJMSAsyncRunner.producer!= null) J2EETransportServerJMSAsyncRunner.producer.close();
//	        if(J2EETransportServerJMSAsyncRunner.session!=null) J2EETransportServerJMSAsyncRunner.session.close();
//	        if(J2EETransportServerJMSAsyncRunner.connection!=null) J2EETransportServerJMSAsyncRunner.connection.close();
    	}catch(Exception e){
    		e.printStackTrace();
    		AS2Trace.trace(AS2Trace.E, "J2EETransportServerJMSAsync.destroy error: "+ e.getMessage());
    	}
        AS2Trace.trace(AS2Trace.W, "J2EETransportServerJMSAsync.destroy: end");
    }
    /* ExceptionListener implementacija metoda.
     * (non-Javadoc)
     * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
     */
//    public void onException(JMSException e)    {
//    	J2EETrace.trace(J2EETrace.E, "J2EETransportServerJMSAsync.onException: "+ e.getMessage());
//    }
    /* Each message received by this transport is processed here:
     * 1. Print the +++ JMS Start Serivce Call - message to the System output.
     * 2. Read a received XML message and its properties.
     * 3. Start a new thread by passing the received message and properties to it.
     */
//    public void onMessage(Message msg) {
//    	long mStart = System.currentTimeMillis();
//        try  {
//        	J2EETrace.trace(J2EETrace.I, "J2EETransportServerJMSAsync.service start");
//        	StringBuffer tmp = new StringBuffer();
//    		tmp.append(new java.util.Date() + " " + Thread.currentThread());
//    		tmp.append(" +++ JMS Start Service Call #: ");
//    		tmp.append(jmsTransportCallCounter++);
//    		tmp.append(" time ");
//    		tmp.append(mStart);
//    		J2EETrace.traceStringOut(J2EETrace.W, tmp.toString()); 
//    		//msg.acknowledge(); //not needed since javax.jms.Session.AUTO_ACKNOWLEDGE is used
//            if(msg instanceof TextMessage){
//	            J2EEValueObject request = new J2EEValueObject(); //new and empty value object
//	            request.set("@@XML", ((TextMessage)msg).getText()); //client XML message
//	            request.set(J2EEValueObject._CTS_SERVICE,"@@CTS"); //used by server
//	            /** PROPERTIES **/
//	            Enumeration propNames = msg.getPropertyNames();
//	            String eachName;
//	            Object eachValue;
//	            while (propNames.hasMoreElements()) {
//	                eachName = (String) propNames.nextElement();
//	                eachValue = msg.getObjectProperty(eachName);
//	                request.set("@@" + eachName, eachValue);
//	            } 
//	           	String _dds_client_id = request.get("@@DDS_CLIENT_ID");
//	        	String _dds_message_id = request.get("@@DDS_MESSAGE_ID");
//	        	// new thread to do a message processing
//	            J2EETransportServerJMSAsyncRunner ctrl = new J2EETransportServerJMSAsyncRunner(request,_dds_client_id, _dds_message_id);
//				Thread _thread = new Thread(ctrl);
//				_thread.start();
//            }else{
//            	J2EETrace.traceStringOut(J2EETrace.E, "J2EETransportServerJMSAsync.onMessage.ERROR: invalid message: " + msg);
//            }
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//           	J2EETrace.traceStringOut(J2EETrace.E, "J2EETransportServerJMSAsync.onMessage.ERROR: Exception " + msg);
//            //System.exit(-1);
//        }
//    }
    /* Used by administrator console or API to stop the loop or listener.
     * It does a cleanup by calling destroy().
     */
    public void stopJMSServer() {
    	AS2Trace.trace(AS2Trace.W, "J2EETransportServerJMSAsync.stopJMSServer ...");
    	try {
    		//sets the stop indicator to true
    		setStopIt(true);
    		// call all the tasks found in J2EEContextDestroy file.
    		AS2Context.getInstance().destroy();
    		// close queue, session, connection
    		destroy();
    		AS2Trace.trace(AS2Trace.W, "J2EETransportServerJMSAsync.stopJMSServer exit(0)...");
    		System.exit(0);
    	} catch (Exception e) {
    		AS2Trace.trace(AS2Trace.E, "J2EETransportServerJMSAsync.stopJMSServer: ERROR: " + e.getMessage());
    	}
    }    

}


