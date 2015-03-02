package hr.as2.inf.server.interceptors;

import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;

public interface AS2Interceptor {
	public void beforeInvoke(AS2Record value, AS2InvocationContext ctx) throws Exception;
	public void afterInvoke(AS2Record value, AS2InvocationContext ctx) throws Exception;
	public void onInvokeError(AS2Record value, AS2InvocationContext ctx) throws Exception;
}
