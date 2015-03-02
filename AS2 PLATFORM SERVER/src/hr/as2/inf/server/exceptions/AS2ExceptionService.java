package hr.as2.inf.server.exceptions;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.datasources.J2EEDefaultJDBCService;
import hr.as2.inf.server.da.jdbc.AS2ResultSetUtilityJdbc;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;

public final class AS2ExceptionService {
	private AS2RecordList _exceptionLog;
	private static AS2ExceptionService _instance = null;
private AS2ExceptionService(){
	_exceptionLog = new AS2RecordList();
	AS2Context.setSingletonReference(this);
}
public void addLog(AS2Exception newException, String aUser, String aService) {
	LinkedHashMap<String, Object> aRow = new LinkedHashMap<String, Object>();
    aRow.put("User", aUser);
    aRow.put("Service", aService);
    aRow.put("OccuredDate", newException.getOccuredDate().toString());
    aRow.put("ErrorCode", newException.getErrorCode());
    aRow.put("ErrorDescription", newException.getErrorDescription());
    aRow.put("TechnicalErrorDescription",newException.getTechnicalErrorDescription());
    aRow.put("CauseExceptions", newException.getCauseExceptionsAsString());
    String stackTrace = null;
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    PrintWriter pr = new PrintWriter(b);
    newException.printStackTrace(pr);
    pr.flush();
    stackTrace = b.toString();
    aRow.put("StackTrace", stackTrace);
    _exceptionLog.addRow(new AS2Record(aRow));
}
public AS2RecordList getExceptionLog() {
	return _exceptionLog;

}
public static AS2ExceptionService getInstance() {
	if (_instance == null) {
		_instance = new AS2ExceptionService();
	}
	return _instance;
}
public AS2RecordList getMessageDetails(AS2Record req) throws AS2Exception {
	J2EEConnectionJDBC co = null;
	AS2RecordList mmrs = null;
	try {
		co = (J2EEConnectionJDBC) J2EEDefaultJDBCService.getInstance().getConnection();
		Connection jco = co.getJdbcConnection();
		PreparedStatement pstmt = 
			jco.prepareStatement(
				"select * from mm_message where code ="
					+ req.getAsString("@@error_code")); 
		pstmt.setMaxRows(co.getMaxRows());
		ResultSet rs = pstmt.executeQuery();
		mmrs = AS2ResultSetUtilityJdbc.transformResultSetOneRow(rs);
		pstmt.close();
		return mmrs;
	} catch (Exception e) {
		AS2Trace.trace(AS2Trace.E, e);
		AS2DataAccessException ex = new AS2DataAccessException("153");
		ex.addCauseException(e);
		throw ex;
	}
}
public static void main(String[] args) {

}
public void processExceptions(AS2Record req, AS2Exception fme) {
    try {
        AS2User aUser = (AS2User) req.getAsObject(AS2Constants.USER_OBJ);
        String aUserName;
        if (aUser == null)
            aUserName = "";
        else
            aUserName = aUser.getUserId();
        AS2Trace.trace(
            AS2Trace.E,
            "*********** Exception Happend - output start ++++++++++++");
        AS2Trace.traceString(AS2Trace.E, "*********** Remote Object : " + req.getRemoteObject());
        AS2Trace.traceString(AS2Trace.E, "*********** Remote Method   : " + req.getRemoteMethod());
        AS2Trace.traceString(AS2Trace.E, "*********** User      : " + aUserName);
        AS2Trace.traceString(AS2Trace.E, "*********** Exception : " + fme);
		AS2Trace.traceString(AS2Trace.E, "*********** Caused Exception : ");
		fme.printStackTrace();
        AS2Trace.trace(AS2Trace.E,"*********** Exception Happend - output end ------------");
        AS2ExceptionService.getInstance().addLog(fme, aUserName, req.getRemoteMethod());
        //set Vector of nested exception to null, otherwise it cause
        //instantiation exception on clinet (misslig server eg. DB2 classes).
        fme.setCauseExceptions(null);
    } catch (Exception e) {
        AS2Trace.trace(AS2Trace.W, "Problem processing Exception for service: " + e);
    }
}
public void setExceptionLog(AS2RecordList newExceptionsLog) {
	synchronized (_exceptionLog) {
		_exceptionLog = newExceptionsLog;
	}
}
}
