package hr.as2.inf.server.invokers;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.metrics.AS2ArmService;
import hr.as2.inf.common.types.AS2Date;
import hr.as2.inf.server.exceptions.AS2ExceptionService;
import hr.as2.inf.server.interceptors.AS2InterceptorDispatcher;
import hr.as2.inf.server.security.AS2SecurityService;
import hr.as2.inf.server.session.AS2SessionFactory;
import hr.as2.inf.server.transaction.AS2Transaction;

import java.lang.reflect.InvocationTargetException;


public class AS2InvokerDefault extends AS2Invoker {

	@Override
	public Object invoke(AS2Record request, AS2InvocationContext context) throws AS2Exception {
        AS2Trace.trace(AS2Trace.I, request, " AS2InvokerDefault start ");
        boolean exception_occured = false;
        boolean before_commit = false;
        AS2Exception j2ee_exception = null;
        AS2Record response = new AS2Record();
        try {
            AS2Transaction.begin(AS2Context.getInstance().TXNTIMEOUT);
            setDefaultUserInCaseNotDefined(request);
            startSession(request); //session start
            AS2SecurityService.getInstance().checkServiceSecurity(request);
            AS2Record log_request = AS2SecurityService.getInstance().prepareLogData(request);
            //check if user can execute this service
            AS2ArmService.armGetId(request);
            AS2ArmService.armStart(AS2ArmService.BUSINESS_LOGIC);
            //TODO interceptor za data setup
            request.set("datum_unosa", AS2Date.getCurrentTimeAsString());
            request.set("datum_promjene", AS2Date.getCurrentDate());
            long _start_time = System.currentTimeMillis();
            //MAIN CALL BEGIN
            //AS2InterceptorDispatcher.getInstance().dispatchBefore(request, context);//TODO test
            response = callFacade(request);
            //AS2InterceptorDispatcher.getInstance().dispatchAfter(request, context);//TODO test
            //MAIN CALL END
            long _end_time = System.currentTimeMillis();
            log_request.set("response_time", _end_time-_start_time);
            AS2SecurityService.getInstance().daoLogActivity(log_request);
            before_commit = true;
            AS2Transaction.commit();
            before_commit = false;
        } catch (IllegalAccessException e) {
            exception_occured = true;
            j2ee_exception = new AS2Exception("10");
            j2ee_exception.addCauseException(e);
        } catch (IllegalArgumentException e) {
            exception_occured = true;
            j2ee_exception = new AS2Exception("11");
            j2ee_exception.addCauseException(e);
        } catch (InvocationTargetException e) {
            exception_occured = true;
            Throwable te = (Throwable) e.getTargetException();
            te.printStackTrace();
            if (te instanceof AS2Exception) {
                j2ee_exception = (AS2Exception) e.getTargetException();
            } else {
                j2ee_exception = new AS2Exception("12");
                if (te instanceof Exception)
                    j2ee_exception.addCauseException((Exception) te);
            }
        } catch (AS2Exception e) {
            exception_occured = true;
            j2ee_exception = e;
        } catch (NoSuchMethodException e) {
            exception_occured = true;
            j2ee_exception = new AS2Exception("14");
            j2ee_exception.addCauseException(e);
        } catch (ClassNotFoundException e) {
            exception_occured = true;
            j2ee_exception = new AS2Exception("15");
            j2ee_exception.addCauseException(e);
        } catch (Exception e) {
            exception_occured = true;
            j2ee_exception = new AS2Exception("13");
            j2ee_exception.addCauseException(e);
        } catch (Error e) {
            exception_occured = true;
            j2ee_exception = new AS2Exception("16");
            j2ee_exception.addCauseException(e);
        } finally {
            if (exception_occured == true) { //log the exception
                AS2Trace.trace(AS2Trace.I, "AS2InvokerDefault exception happend while invoking the service");
                try {
                    if (before_commit == false){
                    	AS2InterceptorDispatcher.getInstance().dispatchOnError(request, context);//TODO test
                        AS2Transaction.rollback();
                    }
                } catch (Exception rolException) {
                    AS2Trace.trace(AS2Trace.E, rolException, "AS2InvokerDefault exception happend while rollback the service");
                }
                AS2ExceptionService.getInstance().processExceptions(request, j2ee_exception);
                return j2ee_exception;//return J2EEException as a response
            }//log the exception
        }//finnaly
        endSession(request,response); //session end
        AS2ArmService.armStop(AS2ArmService.BUSINESS_LOGIC);
        AS2ArmService.armEnd();
        AS2Trace.trace(AS2Trace.I, response, "AS2InvokerDefault returning the message");
        return response;
	}
}