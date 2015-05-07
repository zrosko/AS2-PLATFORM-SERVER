/**
 * (C) Copyright 2013, Adriacom Software d.o.o.
 *	   Report service class for Jasper/Pentaho/Cristal,... reports.
 */
//TODO net.sf.jasperreports.awt.ignore.missing.font=true
//http://stackoverflow.com/questions/3987804/jasper-stops-finding-one-font
package hr.as2.inf.server.requesthandlers.reports;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.reports.AS2ReportConstants;
import hr.as2.inf.common.reports.AS2ReportRenderer;
import hr.as2.inf.common.reports.AS2ReportRendererFactory;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.requesthandlers.AS2ServerRequestHandler;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 UNIT TEST
	http://127.0.0.1:8888/application/report?reportFilename=banke&reportFormat=pdf
 HELP
 	u jasperreport.jar-defaultProperties promjeniti net.sf.jasperreports.awt.ignore.missing.font=true
 	http://www.metatags.info/meta_http_equiv_content_disposition
 */
@SuppressWarnings("serial")
@WebServlet("/module/report")
public class AS2ReportServletNOVO extends AS2ServerRequestHandler implements AS2ReportConstants {
//	Parameters
//  private static final String REPORT_FILENAME = "reportFilename";
//  private static final String REPORT_FORMAT = "reportFormat"; /** eg. pdf, doc, docx, xls, xlsx, pptx,...**/
//  private static final String REPORT_TYPE = "reportType"; /** eg. jasper, pentaho, crystal,...**/
//  private static final String REPORT_DISPOSITION = "reportDisposition"; /** eg. inline, attachment **/
  //Defaults
  private static final String DEFAULT_REPORTS_FILENAME = "TODO";//TODO Error.jsp or ?
  private static final String DEFAULT_REPORTS_FORMAT = "pdf";
  private static final String DEFAULT_REPORTS_TYPE = ".jasper";
  private static final String DEFAULT_REPORTS_DISPOSITION = "inline";
  private static final String DEFAULT_REPORTS_SERVICE_PATH = "module/reports/"; /* war...*/
  //Facade

  public static final String COMPONENT = "Component";
  public static final String SERVICE = "Service";
  //Other
  protected boolean _use_relative_URI = true;
  protected boolean _use_dummy_parameter = true;
  //Test
//  private String _component = "hr.adriacomsoftware.app.server.jb.facade.BankaFacadeServer";
  private String _component = "hr.adriacomsoftware.app.server.karticno.gr.facade.KarticnoFacadeServer";
//  private String _service = "izvjestajBanke";
  private String _service = "izvjestaji";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    this.serviceReport(request, response);
  }
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    this.serviceReport(request, response);
  }

  public void serviceReport(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		byte[] byteArray = null;
		AS2Record facade_request = null;
		AS2Record response_vo = null;
		try {
			facade_request = readRequestParameters(request);
			String reportFilename = facade_request.getAsString(REPORT_FILENAME, DEFAULT_REPORTS_FILENAME);
			String reportFormat = facade_request.getAsString(REPORT_FORMAT, DEFAULT_REPORTS_FORMAT);
			String reportType = facade_request.getAsString(REPORT_TYPE, DEFAULT_REPORTS_TYPE);
			String reportDisposition = facade_request.getAsString(REPORT_DISPOSITION, DEFAULT_REPORTS_DISPOSITION);

			String resourceName = DEFAULT_REPORTS_SERVICE_PATH + reportFilename + reportType;

			ServletContext context = this.getServletConfig().getServletContext();
			File reportFile = new File(context.getRealPath(resourceName));
			//TODO kako iz facade_request popuniti parametre za report
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			if(facade_request.getRemoteObject()==null)
				facade_request.setRemoteObject(_component);
			if(facade_request.getRemoteMethod()==null)
				facade_request.setRemoteMethod(_service);
			//TODO user retrieve from client
			AS2User user = new AS2User();
			facade_request.set(AS2Constants.USER_OBJ, user);
//			//TODO dummy ako nema parametara na facadi
//			facade_request.setDummyDto(_use_dummy_parameter);

			/***DODANO 07.11.20.13****/
			if( facade_request.get("reportSelected")==null)
				facade_request.set("@@report_selected",facade_request.get(REPORT_FILENAME));
			else
				facade_request.set("@@report_selected", facade_request.get("reportSelected"));
			facade_request.set("@@transform_to",facade_request.get("transform_to"));
//			facade_request.set("@@transform_to","hr.adriacomsoftware.app.common.karticno.gr.dto.McardGrZahtjevVo");
//			facade_request.set("@@service","izvjestaji");
//			facade_request.set("@@component","hr.adriacomsoftware.app.server.karticno.gr.facade.KarticnoFacadeServer");
//			facade_request.set("@@oib","hr.adriacomsoftware.app.server.karticno.gr.facade.KarticnoFacadeServer");
			/***DODANO 07.11.20.13****/
			try{
				Object facade_response = dispatchRequestToInvoker(facade_request, new AS2InvocationContext());
				//Object facade_response = J2EEApplicationControllerFactory.getInstance().getApplicationController().executeRequest(facade_request);

				if (facade_response instanceof Throwable){
					request.setAttribute("error", facade_response);
					callPage("/jsp/Error.jsp", request,response);
				}else if (facade_response instanceof AS2Record){
					response_vo = (AS2Record) facade_response;
					Object data = response_vo.getProperty(AS2Record._RESPONSE);

					if(data instanceof AS2RecordList){
						AS2RecordList j2ee_rs = (AS2RecordList)data;
						AS2ReportRenderer renderer = AS2ReportRendererFactory.getInstance().createRenderer(reportType);
						byteArray =	renderer.renderReport(	reportFile.getPath(),parameters,j2ee_rs,reportFormat);
						response.setContentType("application/" + reportFormat);
						response.setContentLength(byteArray.length);
						response.setHeader("Content-disposition",reportDisposition +"; filename=\"" + reportFilename +"."+ reportFormat + "\"");
						ServletOutputStream ouputStream = response.getOutputStream();
						ouputStream.write(byteArray, 0, byteArray.length);
						ouputStream.flush();
						ouputStream.close();
					}
				}
			}catch(Exception e){
				request.setAttribute("error", e);
				callPage("/jsp/Error.jsp",request,response);
			}
		}catch(Exception e){
			request.setAttribute("error", e);
			callPage("/jsp/Error.jsp",request,response);
		}
	}


//  /**AS dodano!**/
//  public void postaviFacadeAtribute(HashMap<String,String> atributi){
//		facade_request.set("@@report_selected",atributi.get("@@report_selected"));
//		facade_request.set("oib",atributi.get("oib"));
//		facade_request.set("jmbg",atributi.get("jmbg"));
//		facade_request.set("broj_zahtjeva",atributi.get("broj_zahtjeva"));
//
//  }


  protected AS2Record readRequestParameters(HttpServletRequest req) {
		AS2Record inputFields = new AS2Record();
		String service = null;
		String component = null;

		try {
			component = req.getParameterValues(COMPONENT)[0];	//(3) component
			service = req.getParameterValues(SERVICE)[0];		//(4) service
		} catch (Exception e) {
			// No need to handle the NullPointerException
			// in the case service or component are not passed in.
		}
		inputFields.setRemoteMethod(service);
		inputFields.setRemoteObject(component);
		inputFields.set("@@log_in_host_name", req.getRemoteAddr());
		Enumeration<String> E = req.getParameterNames();
		while (E.hasMoreElements()) {
			String name = E.nextElement();
			String value = req.getParameter(name);
			//TODO pretvaramo String "null" u "" - Pogreška se javlja prilikom ažuriranja dataBound componente
			if(value.equals("null"))
				value="";
			inputFields.set(name, value);
			// Ignoriraj neke parametre medu poslovnim podacima.
			// Ova polja su sistemska pomocna polja.
			if (!name.equals(SERVICE) &&
				!name.equals(COMPONENT)&&
				!name.startsWith("@@"))
				inputFields.set(name, value);
		}
		return inputFields;
	}
	public void callPage(String s, HttpServletRequest httpservletrequest,
			HttpServletResponse httpservletresponse) throws IOException,
			ServletException {
		callURI(s, httpservletrequest, httpservletresponse);
	}

	public void callURI(String s, HttpServletRequest httpservletrequest,
			HttpServletResponse httpservletresponse) throws IOException,
			ServletException {
		ServletContext servletcontext;
		String str = null;
		if (_use_relative_URI) {
			servletcontext = getServletContext();
			str = s;
		} else {
			servletcontext = getServletContext().getContext(s);
		}
		RequestDispatcher requestdispatcher = servletcontext.getRequestDispatcher(str);
		requestdispatcher.forward(httpservletrequest, httpservletresponse);
	}
}