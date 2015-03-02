package hr.as2.inf.server.jsp;

import hr.as2.inf.common.data.AS2Record;

public class J2EEBean extends AS2Record {

	private static final long serialVersionUID = 1L;

	public J2EEBean() {
		super();
	}

	public J2EEBean(AS2Record value) {
		super(value.getProperties());
	}
}
