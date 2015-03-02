package hr.as2.inf.server.security.authentication.da.ldap;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.server.security.authentication.da.AS2AuthenticationDao;

public class AS2AuthenticationJdbc implements AS2AuthenticationDao {

	public AS2Record login(AS2Record value) throws AS2Exception {
		AS2Record ret = new AS2Record();
		ret.set("valid", "ok");
		ret.set("role", "administrator");
		ret.set("function", "maintenance");
		return ret;
	}

	public AS2Record logout(AS2Record value) throws AS2Exception {
		return new AS2Record();
	}

	public AS2Record changePassword(AS2Record value) throws AS2Exception {
		return new AS2Record();
	}
}
