package hr.as2.inf.server.invokers;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.server.interceptors.AS2InterceptorDispatcher;

import java.util.LinkedHashMap;

public class AS2InvokerFactory {
	/* Constants */
	public static final String INVOKER_DEFAULT = "DEFAULT";
	public static final String INVOKER_CTS = "CTS";
	/* Singleton */
	private static AS2InvokerFactory _instance = null;
	/* Cache */
	private LinkedHashMap<String, AS2Invoker> _invokers = new LinkedHashMap<String, AS2Invoker>();

	/* Singleton constructor */
	private AS2InvokerFactory() {
    	AS2InterceptorDispatcher dispatcher = AS2InterceptorDispatcher.getInstance();
		dispatcher.registerInterceptor("hr.as2.inf.server.interceptors.AS2InterceptorAudit");	
		dispatcher.registerInterceptor("hr.as2.inf.server.interceptors.AS2InterceptorLogging");	
		dispatcher.registerInterceptor("hr.as2.inf.server.interceptors.AS2InterceptorAuthorization");	
		dispatcher.registerInterceptor("hr.as2.inf.server.interceptors.AS2InterceptorTransaction");	
		AS2Context.setSingletonReference(this);		
	}

	public static AS2InvokerFactory getInstance() {
		if (_instance == null) {
			_instance = new AS2InvokerFactory();
		}
		return _instance;
	}

	public AS2Invoker getInvoker(String type) {
		AS2Invoker invoker = _invokers.get(type);
		if (invoker == null)
			invoker = create(type);
		return invoker;
	}

	private AS2Invoker create(String type) throws AS2Exception {
		AS2Invoker invoker = null;
		if (type != null) {
			if (type.equals(INVOKER_DEFAULT))
				invoker = new AS2InvokerDefault();
			else if (type.equals(INVOKER_CTS))
				invoker = new AS2InvokerCST();
		} else {
			invoker = new AS2InvokerDefault();
		}
		_invokers.put(type, invoker);
		return invoker;
	}
}
