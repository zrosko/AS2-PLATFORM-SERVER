package hr.as2.inf.server.security.authentication.servlets;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
//TODO napraviti hashmapu parametara i njihovih vrijednosti
//klasu staviti u neki drugi package
public class AS2FilteredRequest extends HttpServletRequestWrapper {

	public AS2FilteredRequest(ServletRequest request) {
		super((HttpServletRequest) request);
	}

	public String sanitize(String input) {
		String result = "1";//default password 
		return result;
	}

	public String getParameter(String paramName) {
		String value = super.getParameter(paramName);
		if ("j_password".equals(paramName)) {
			value = sanitize(value);
		}
		return value;
	}

	public String[] getParameterValues(String paramName) {
		String values[] = super.getParameterValues(paramName);
		if ("j_password".equals(paramName)) {
			for (int index = 0; index < values.length; index++) {
				values[index] = sanitize(values[index]);
			}
		}
		return values;
	}
}
