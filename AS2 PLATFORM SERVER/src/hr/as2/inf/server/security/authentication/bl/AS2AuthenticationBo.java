package hr.as2.inf.server.security.authentication.bl;

import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.security.authorization.da.jdbc.J2EEPrijavaKorisnikaJdbc;
import hr.as2.inf.server.security.authorization.da.jdbc.J2EERBACKorisnikJdbc;

public class AS2AuthenticationBo {

	public AS2User logIn(AS2User value) throws AS2Exception {
		J2EERBACKorisnikJdbc _dao = new J2EERBACKorisnikJdbc();
		AS2User _user = _dao.daoLogIn(value);
		if (_user.getProperties().size() > 0) {
			J2EEPrijavaKorisnikaJdbc session_dao = new J2EEPrijavaKorisnikaJdbc();
			session_dao.daoLogIn(_user);
		}
		return _user;
	}

	public AS2User logOut(AS2User value) throws AS2Exception {
		J2EEPrijavaKorisnikaJdbc session_dao = new J2EEPrijavaKorisnikaJdbc();
		return session_dao.daoLogOut(value);
	}

	public AS2User changePassword(AS2User value) throws AS2Exception {
		J2EERBACKorisnikJdbc user_dao = new J2EERBACKorisnikJdbc();
		return user_dao.daoChangePassword(value);
	}
}
