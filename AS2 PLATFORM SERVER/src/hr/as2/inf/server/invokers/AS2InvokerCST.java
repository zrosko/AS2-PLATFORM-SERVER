package hr.as2.inf.server.invokers;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.session.AS2SessionFactory;
import hr.as2.inf.server.transaction.AS2Transaction;
/*
 * http://soapatterns.org/design_patterns/compensating_service_transaction
 */
public class AS2InvokerCST extends AS2Invoker {
	//TODO
	@Override
	public Object invoke(AS2Record request, AS2InvocationContext context) throws AS2Exception {
		 AS2Trace.trace(AS2Trace.I, request, " AS2InvokerDefault start ");
//	     boolean exception_occured = false;
//	     boolean before_commit = false;
//	     AS2Exception j2ee_exception = null;
//	     AS2Record response = new AS2Record();
	     try {
	    	 AS2Transaction.begin(AS2Context.getInstance().TXNTIMEOUT);
	         setDefaultUserInCaseNotDefined(request);
	         AS2SessionFactory.getInstance().createSession(request);//TODO brisi
	         return null;
	     } catch (Exception e){
	    	 e.printStackTrace();
	     }
	     return null;
	}
}