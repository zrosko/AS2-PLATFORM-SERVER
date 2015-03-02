package hr.as2.inf.server.security.authentication.facade;

import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.security.authentication.facade.AS2AuthenticationFacade;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.security.authentication.bl.AS2AuthenticationBo;

public class AS2AuthenticationFacadeServer implements AS2AuthenticationFacade {
	private static AS2AuthenticationFacadeServer instance = null;

	private AS2AuthenticationFacadeServer() {
	}

	public static AS2AuthenticationFacadeServer getInstance() {
		if (instance == null)
			instance = new AS2AuthenticationFacadeServer();
		return instance;
	}

	public AS2User logIn(AS2User value) throws AS2Exception {
		return new AS2AuthenticationBo().logIn(value);
	}

	public AS2User logOut(AS2User value) throws AS2Exception {
		return new AS2AuthenticationBo().logOut(value);
	}

	public AS2User changePassword(AS2User value) throws AS2Exception {
		return new AS2AuthenticationBo().changePassword(value);
	}
}
