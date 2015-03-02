package hr.as2.inf.server.security.authentication.valves;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
//http://www.oxxus.net/tutorials/tomcat/tomcat-valve
//http://webhelp.esri.com/geoportal_extension/9.3.1/index.htm#gpt_single_signon.htm
public class AS2VersionCheck extends org.apache.catalina.valves.ValveBase {

	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {
		System.out.println(getContainer());
		System.out.println(getDomain());
		System.out.println(getDomainInternal());
		System.out.println(getObjectName());
		final String originalRemoteAddr = request.getRemoteAddr();
		final String originalRemoteHost = request.getRemoteHost();
		final String originalScheme = request.getScheme();
		final boolean originalSecure = request.isSecure();
		final int originalServerPort = request.getServerPort();
	}

}

