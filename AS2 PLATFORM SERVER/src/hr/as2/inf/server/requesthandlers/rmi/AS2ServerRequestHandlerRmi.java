package hr.as2.inf.server.requesthandlers.rmi;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.requesthandlers.rmi.AS2RequestHandlerAS2RecordRmi;
import hr.as2.inf.common.requesthandlers.rmi.AS2RequestHandlerHashtableRmi;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.invokers.AS2InvokerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class AS2ServerRequestHandlerRmi extends UnicastRemoteObject implements
		AS2RequestHandlerAS2RecordRmi, AS2RequestHandlerHashtableRmi {
	private static AS2ServerRequestHandlerRmi _instance = null;
	private static final long serialVersionUID = 1L;
	protected Registry registry;
	private static long rmiTransportCallCounter = 0;

	public AS2ServerRequestHandlerRmi() throws RemoteException {
		//super();
		init();
	}
    public static AS2ServerRequestHandlerRmi getInstance() {
        if (_instance == null) {
            try {
                _instance = new AS2ServerRequestHandlerRmi();
            } catch (Exception e) {
                AS2Trace.trace(AS2Trace.E, "J2EETransportServerRMI.Constructor ? " + e.getMessage());
            }
        }
        return _instance;
    }
	/**
     * Halts the server's RMI operations. Causes the server to unbind itself
     * from the registry.
     */
    public void destroy() {
        AS2Trace.trace(AS2Trace.W, "Start destroy RMI Transport");
        try {
            if (registry != null)
                registry.unbind(getRegistryName());
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, "Problem unbinding from RMI registry: " + e.getMessage());
        }
        AS2Trace.trace(AS2Trace.W, "End destroy RMI Transport");
    }
	   /**
     * Begins the server's RMI operations. Causes the server to bind itself to
     * the registry.
     */
    public void init() {
        AS2Trace.trace(AS2Trace.W, "Start init RMI Transport");
        try {
            /*
             * Creates and installs a new security manager. A Security manager
             * must be installed so that you can load classes that are not in
             * the local CLASSPATH. RMI must be able to load classes that are
             * not in the local CLASSPATH.
             */
            // System.setSecurityManager (new RMISecurityManager());
            // Try to find the appropriate registry already running
            registry = LocateRegistry.getRegistry(getRegistryPort());
            registry.list(); // Verify it's alive and well
        } catch (Exception e) {
            // Couldn't get a valid registry
            registry = null;
        }
        // If we couldn't find it, we need to create it.
        // (Equivalent to running "rmiregistry")
        if (registry == null) {
            try {
                registry = LocateRegistry.createRegistry(getRegistryPort());
            } catch (Exception e) {
                AS2Trace.trace(AS2Trace.E, "Could not get or create RMI registry on port " + getRegistryPort() + ": " + e.getMessage());
                return;
            }
        }
        // If we get here, we must have a valid registry.
        // Now register this class instance with that registry.
        try {
            registry.rebind(getRegistryName(), this);
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, "Could not bind to RMI registry: " + e.getMessage());
            return;
        }
        AS2Trace.trace(AS2Trace.W, "End init RMI Transport");
    }
    /**
     * Returns the port where the registry should be running. By default the
     * port is the default registry port (1099). This can be overridden with the
     * <tt>RMI_REGISTRY_PORT</tt> AS2Context parameter.
     * 
     * @return the port for the registry
     */
    protected int getRegistryPort() {
        // First port choice is the "RMI_REGISTRY_PORT" AS2Context parameter
        try {
            int port = Integer.parseInt(AS2Context.getInstance().getProperties().getProperty("RMI_REGISTRY_PORT"));
            AS2Trace.trace(AS2Trace.W, "Start init RMI Transport RMI_REGISTRY_PORT = " + port);
            return port;
        }
        // Fallback choice is the default registry port (1099)
        catch (NumberFormatException e) {
            AS2Trace.trace(AS2Trace.W, "Start init RMI Transport RMI_REGISTRY_PORT = " + Registry.REGISTRY_PORT);
            return Registry.REGISTRY_PORT;
        }
    }
    /**
     * Returns the name under which the server should be bound in the registry.
     * By default the name is the server's class name. This can be overridden
     * with the <tt>RMI_REGISTRY_NAME</tt> J2EEContext parameter.
     * 
     * @return the name under which the server should be bound in the registry
     */
    protected String getRegistryName() {
        // First name choice is the "RMI_REGISTRY_NAME" init parameter
        String name = AS2Context.getInstance().getProperties().getProperty("RMI_REGISTRY_NAME");
        AS2Trace.trace(AS2Trace.W, "Start init RMI Transport RMI_REGISTRY_NAME = " + name);
		return name;
    }

	@Override
	public Object serviceAS2Record(AS2Record request) throws RemoteException,
			AS2Exception, Exception {
        Object response = null;
        long mStart = System.currentTimeMillis();
        try {
            AS2Trace.trace(AS2Trace.I, "J2EETransportServerRMI.service start");
            StringBuffer tmp = new StringBuffer();
            tmp.append(new java.util.Date() + " " + Thread.currentThread());
            tmp.append(" +++ RMI Start Service Call from ");
            tmp.append(getClientHost());
            tmp.append(" Service = ");
            try {
                tmp.append(request.getRemoteObject());
                tmp.append(".");
                tmp.append(request.getRemoteMethod());
            } catch (Exception e0) {
                tmp.append("UNKNOWN Service");
            }
            AS2Trace.traceStringOut(AS2Trace.W, tmp.toString());
            AS2Record as2_request = null;
            as2_request = new AS2Record();
			as2_request.setDummy(request.isDummy());
			as2_request.setRemoteObject(request.getRemoteObject());
			as2_request.setRemoteMethod(request.getRemoteMethod());
			as2_request.setProperties(request.getProperties());
			// Invocation Context
			AS2InvocationContext as2_context = new AS2InvocationContext();
			as2_context.setThreadId(Thread.currentThread().getName());
			as2_context.setRemoteObject(as2_request.getRemoteObject());
			as2_context.setRemoteMethod(as2_request.getRemoteMethod());
			as2_context.setRemoteAddr(getClientHost());
			response = 	dispatchRequestToInvoker(as2_request, as2_context);
            //response = callApplicationController(request);// TODO test
            // response =
            // J2EEApplicationControllerFactory.getInstance().getApplicationController().executeRequest(request);
            // //Execute request
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, " J2EETransportServerRMI.service " + e.getMessage());
            throw e;
        } finally {
            StringBuffer tmp = new StringBuffer();
            tmp.append(new java.util.Date() + " " + Thread.currentThread());
            tmp.append(" --- RMI End Service Call #: ");
            tmp.append(rmiTransportCallCounter++);
            tmp.append(" from ");
            tmp.append(getClientHost());
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
        AS2Trace.trace(AS2Trace.I, " J2EETransportServerRMI.service end");
        return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object serviceHashtable(@SuppressWarnings("rawtypes") Hashtable request)//TODO dodati <> za hashtable
			throws RemoteException, Exception {
		//TODO kad primamo AS2RecordList sa klijenta
		AS2Record as2_request = new AS2Record();
		Object rs = request.get("@@ROWS");
		if( rs != null && rs instanceof Vector){
			AS2RecordList as2_rs = new AS2RecordList(); //Override existing AS2Record with AS2RecordList
			Vector<Hashtable<String,Object>> rows = (Vector<Hashtable<String,Object>>)rs;
			Iterator<Hashtable<String,Object>> E = rows.iterator();
			while (E.hasNext()) {//Convert each row to the new AS2Record
				Hashtable<String,Object> vo  = E.next();
				AS2Record as2_vo = new AS2Record();
				as2_vo.fromHashtable(vo);
				as2_rs.addRow(as2_vo);
			}
			as2_request = as2_rs;//Replace existing AS2Record object
			request.remove("@@ROWS");
		}
		AS2User as2_user = new AS2User();
		as2_user.fromHashtable((Hashtable<String,Object>)request.get(AS2Constants.USER_OBJ));
		request.remove(AS2Constants.USER_OBJ);
		as2_request.set(AS2Constants.USER_OBJ,as2_user);
		as2_request.fromHashtable(request);
		as2_request.setRemoteObject((String)request.get("@@remote_object"));
		as2_request.setRemoteMethod((String)request.get("@@remote_method"));
		//TODO ostale atribute USER, value info
		Object response = serviceAS2Record(as2_request);
		/* Convert back to Hashtable. */
		if(response instanceof AS2Record){
			AS2Record res = (AS2Record)response;
			AS2Record data = (AS2Record)res.getAsObject(AS2Record._RESPONSE);
			res.delete(AS2Record._RESPONSE);
			if(data instanceof AS2RecordList){
				Hashtable<String, Object> response_ht = res.toHashtable();
				Hashtable<String, Object> data_ht = data.toHashtable();
				convertFromAS2RecordListToVector(data,data_ht,"@@ROWS");
				response_ht.put(AS2Record._RESPONSE, data_ht);
				response = response_ht;
			}else{//AS2Record
				Hashtable<String, Object> response_ht = res.toHashtable();
				Hashtable<String, Object> data_ht = data.toHashtable();
				AS2RecordList functions = (AS2RecordList)data.getAsObject(AS2User.FUNCTIONS);
				if(functions != null && functions instanceof AS2RecordList){
					data.delete(AS2User.FUNCTIONS);
					convertFromAS2RecordListToVector(functions,data_ht,AS2User.FUNCTIONS);
				}
				response_ht.put(AS2Record._RESPONSE, data_ht);
				response = response_ht;
			}
		}//else AS2Exception
		return response;
	}
	private void convertFromAS2RecordListToVector(AS2Record data, Hashtable<String, Object> data_ht, String key){
		Vector<Hashtable<String, Object>> rows_out = new Vector<Hashtable<String, Object>>();
		ArrayList<AS2Record> rows = ((AS2RecordList)data).getRows();
		Iterator<AS2Record> E = rows.iterator();
		while(E.hasNext()){
			AS2Record vo = E.next();
			Hashtable<String, Object> row = vo.toHashtable();
			rows_out.add(row);
		}
		data_ht.put(key, rows_out);
	}
	public Object dispatchRequestToInvoker(AS2Record request, AS2InvocationContext context) throws AS2Exception {
		Object res = AS2InvokerFactory.getInstance().getInvoker(AS2InvokerFactory.INVOKER_DEFAULT).invoke(request, context);
		return res;
	}
	public static void main(String[] args) {
		AS2ServerRequestHandlerRmi.getInstance();
	}
}