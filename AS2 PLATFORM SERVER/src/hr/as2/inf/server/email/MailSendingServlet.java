package hr.as2.inf.server.email;

import java.io.IOException;
import java.util.Date;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//TODO ApacheTomcat 7 book str 244
@SuppressWarnings("serial")
public class MailSendingServlet extends HttpServlet {
	private Session session;

	public void init() throws ServletException {
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			this.session = (Session) envContext.lookup("mail/testEmailSession");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String emailRecipient = request.getParameter("sendTo");
		try {
			Message msg = new MimeMessage(this.session);
			msg.setFrom(new InternetAddress("no-reply@tomcat7.apress.com"));
			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(emailRecipient, false));
			msg.setSubject("Test email");
			msg.setText("Hello and welcome to apress mailing list!");
			msg.setSentDate(new Date());
			Transport.send(msg);
			System.out.println("Message sent OK.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
