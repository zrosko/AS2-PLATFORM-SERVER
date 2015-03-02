package hr.as2.inf.server.exceptions;

import hr.as2.inf.common.exceptions.AS2Exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.Signature;
//TODO //AS2Logger.AS2_GENERAL.error("",ex);
public aspect AS2ExceptionAspect {
	Logger _logger = Logger.getLogger("AS2Exception");
	
	AS2ExceptionAspect() {
		_logger.setLevel(Level.ALL);
	}
	
	pointcut exceptionFACADE()//Radi kad se promini CALL u EXECUTION
	: (execution(* hr.adriacomsoftware.app.server.*.facade.*.*(..))
	|| execution(* hr.adriacomsoftware.app.server.*.*.facade.*.*(..))
	|| execution(* hr.as2.inf.server.*.facade.*.*(..)))
	&& !within(AS2ExceptionAspect);

	pointcut exceptionDAO()
	: (execution(* hr.adriacomsoftware.app.server.*.da.*.*.*(..))
	|| execution(* hr.as2.inf.server.*.da.*.*.*(..))
	|| execution(* hr.adriacomsoftware.app.server.*.*.da.*.*.*(..))) 
	&& !within(AS2ExceptionAspect);
	
	pointcut exceptionBO()
	: (execution(* hr.adriacomsoftware.app.server.*.bl.*.*(..))
	|| execution(* hr.as2.inf.server.*.bl.*.*(..))
	|| execution(* hr.adriacomsoftware.app.server.*.*.bl.*.*(..))) 
	&& !within(AS2ExceptionAspect);

	after() throwing(Throwable ex) : exceptionFACADE() {
		if(ex instanceof AS2Exception){
			if (_logger.isLoggable(Level.WARNING)) {
				Signature sig = thisJoinPointStaticPart.getSignature();
				_logger.logp(Level.WARNING, sig.getDeclaringType().getName(),
						sig.getName(), "AS2ExceptionAspectFACADE ", ex);				
			}
		} else {
			AS2Exception e = new AS2Exception("150");
			e.addCauseException(ex);
			throw e;
		}
	}
	//TODO TESTIRATI!!
	after() throwing(Throwable ex) : exceptionDAO() {
		if(ex instanceof AS2Exception){
			if (_logger.isLoggable(Level.WARNING)) {
				Signature sig = thisJoinPointStaticPart.getSignature();
				_logger.logp(Level.WARNING, sig.getDeclaringType().getName(),
						sig.getName(), "AS2ExceptionAspectDAO ", ex);
			}
		} else {
			AS2Exception e = new AS2Exception("151");
			e.addCauseException(ex);
			throw e;
		}
	}
	after() throwing(Throwable ex) : exceptionBO() {
		if(ex instanceof AS2Exception){
			if (_logger.isLoggable(Level.WARNING)) {
				Signature sig = thisJoinPointStaticPart.getSignature();
				_logger.logp(Level.WARNING, sig.getDeclaringType().getName(),
						sig.getName(), "AS2ExceptionAspectBO ", ex);
			}
		} else {
			AS2Exception e = new AS2Exception("152");
			e.addCauseException(ex);
			throw e;
		}
	}
}
