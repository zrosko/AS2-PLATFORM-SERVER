package hr.as2.inf.server.interceptors;

import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;

public class AS2InterceptorLogging implements AS2Interceptor {

	@Override
	public void beforeInvoke(AS2Record value, AS2InvocationContext ctx) throws Exception {
		System.out.println("AS2LoggingInterceptor.before");
	}


	@Override
	public void afterInvoke(AS2Record value, AS2InvocationContext ctx) throws Exception {
		System.out.println("AS2LoggingInterceptor.afer");
	}
	
	@Override
	public void onInvokeError(AS2Record value, AS2InvocationContext ctx) throws Exception {
		System.out.println("AS2LoggingInterceptor.error");
	}
}
