package hr.as2.inf.server.security.authentication.da;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;

public interface AS2AuthenticationDao {

	public AS2Record login(AS2Record value) throws AS2Exception;

	public AS2Record logout(AS2Record value) throws AS2Exception;

	public AS2Record changePassword(AS2Record value) throws AS2Exception;
}
