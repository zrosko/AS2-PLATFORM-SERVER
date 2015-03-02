package hr.as2.inf.server.interceptors;

import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;

import java.util.ArrayList;
import java.util.List;
/*
 * http://code.google.com/p/java-interceptor/wiki/Documentation
 */
public class AS2InterceptorDispatcher {
	private static AS2InterceptorDispatcher _instance = null;
	private static List<AS2Interceptor> _interceptors = new ArrayList<AS2Interceptor>();

	private AS2InterceptorDispatcher() {
	}
	public static AS2InterceptorDispatcher getInstance() {
		if(_instance==null)
			_instance = new AS2InterceptorDispatcher();
		return _instance;
	}

	public void registerInterceptor(AS2Interceptor value) {
		_interceptors.add(value);
	}
	public AS2Interceptor registerInterceptor(String className) {
		try {
			Object ob =  Class.forName(className).newInstance();
			_interceptors.add((AS2Interceptor) ob);
			return (AS2Interceptor) ob;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void remove(AS2Interceptor value) {
		_interceptors.remove(value);
	}

	public void dispatchBefore(AS2Record value, AS2InvocationContext ctx) throws Exception {
		for (AS2Interceptor interceptor : _interceptors) {
			interceptor.beforeInvoke(value, ctx);
		}
	}
	
	public void dispatchAfter(AS2Record value, AS2InvocationContext ctx) throws Exception {
		for (AS2Interceptor interceptor : _interceptors) {
			interceptor.afterInvoke(value, ctx);
		}
	}

	public void dispatchOnError(AS2Record value, AS2InvocationContext ctx) throws Exception {
		for (AS2Interceptor interceptor : _interceptors) {
			interceptor.onInvokeError(value, ctx);
		}
	}
	
}
