package hr.as2.inf.server.invokers;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.session.AS2SessionFactory;

public abstract class AS2Invoker { 
	public abstract Object invoke(AS2Record request, AS2InvocationContext context) throws AS2Exception;
	
	public AS2Record callFacade(AS2Record request) throws Exception {
		if(request!=null && request.getRemoteObject().length()>0 && request.getRemoteMethod().length()>0)
			return request.execute();
		else
			throw new AS2Exception("999");//TODO
	}
	protected void setDefaultUserInCaseNotDefined(AS2Record request){
		AS2User user = (AS2User)request.getAsObject(AS2Constants.USER_OBJ);
		if(user==null){
			user = new AS2User();
			user.setUserName("Dummy");
			user.setUserId("1");
			user.setValid(true);
			user.setApplication("as2");
			request.set(AS2Constants.USER_OBJ, user);
		}
	}
	protected void startSession(AS2Record request) throws Exception {
		AS2SessionFactory.getInstance().createSession(request);
		//AS2SessionFactory.getInstance().setSessionValue("ime","zdravko");
	}
	protected void endSession(AS2Record request, AS2Record response) {
		try{
        	AS2SessionFactory.getInstance().invalidateSession(request, response);
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
}

