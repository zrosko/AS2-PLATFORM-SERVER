package hr.as2.inf.server.exceptions;

import hr.as2.inf.common.exceptions.AS2Exception;

import java.util.Date;

@SuppressWarnings("serial")
public class AS2RemotingError extends AS2Exception {
	String _json;
	String _xml;
	String _status;
	String _error_code;

	public AS2RemotingError(String errorCode, String resourceBundle,
			String technicalErrorDescription, int severity,
			String recoveryAction, Date occuredDate) {
		super(errorCode);
		setResourceBundle(resourceBundle);
		setErrorCode(errorCode);
		setTechnicalErrorDescription(technicalErrorDescription);
		setSeverity(severity);
		setRecoveryAction(recoveryAction);
		setOccuredDate(occuredDate);
	}
}
