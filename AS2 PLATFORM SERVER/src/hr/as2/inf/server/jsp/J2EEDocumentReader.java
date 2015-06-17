package hr.as2.inf.server.jsp;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.types.AS2Date;
import hr.as2.inf.server.da.jdbc.J2EEDataAccessObjectJdbc;
import hr.as2.inf.server.transaction.AS2Transaction;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class J2EEDocumentReader extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//private String serverHost = "localhost";
	//private String severName = "ServletServer";
    //private int timeout = 60000;
    //private static int count = 0;
    //private int number;
	//private static long _httpTransportCallCounter = 0;
	//private static AS2Context CONTEXT_KEEPER;
	/** 
	 * Call all the tasks found in J2EEContextDestroy file. 
	 */
	public void destroy() {
		AS2Context.getInstance().destroy();
	}
	/** 
	 * Initialize transport server and executes the tasks from J2EEContextInit file. 
	 */
    public void init(ServletConfig servletConfig) throws ServletException {
    	super.init(servletConfig);
    }
	public void doGet(HttpServletRequest req, HttpServletResponse response)throws IOException, ServletException {
		AS2Trace.traceStringOut(AS2Trace.W, "J2EEStartupHttp doGet " );
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		String title = "Pogele u bazu procesa ---";
		out.println("<title>" + title + "</title>");
		out.println("</head>");
		out.println("<body bgcolor=\"white\">");
		System.out.println("J2EEStartupHttp begin 000" );
		AS2Record _request = new AS2Record();
		AS2RecordList rs = null;
		J2EEDataAccessObjectJdbc dao = new J2EEDataAccessObjectJdbc();
    	try{
    		System.out.println("J2EEStartupHttp begin 100" );
    		AS2Transaction.begin();	
    		System.out.println("J2EEStartupHttp begin 200" );
    		rs = dao.daoFindFromView(_request, "bi_valuta"); //ret = ??
    		System.out.println("J2EEStartupHttp begin 300" );
    		AS2Transaction.commit();
    	}catch(Exception e){
    		System.out.println("J2EEStartupHttp begin 400" );
    		System.out.println(e);;
    		//ret = dao.daoStopSQLJob(_service);
			AS2Transaction.rollback();
    	}
		if (rs!=null){//OK
			for(AS2Record vo : rs.getRows()){
				out.println("<h1>"+ vo.get("naziv_drzave")+ "</h1>");
			}
			out.println("<h1>"+ "Kraj podataka"+ "</h1>");
		}else
			out.println("<h1>"+ "Nema podataka!!!"	+ "</h1>");
		
		out.println("<h1>" + AS2Date.getCurrentDateAsString() + "</h1>");
		out.println("</body>");
		out.println("</html>");
	}
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }
}