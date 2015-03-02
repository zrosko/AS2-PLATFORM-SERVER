package hr.as2.inf.server.requesthandlers.http;

import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.file.AS2FileUtility;
import hr.as2.inf.server.requesthandlers.AS2ServerRequestHandler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

//@WebServlet("/json_servlet_fileupload")
public class AS2FileUploadServlet extends AS2ServerRequestHandler { 

	private static final long serialVersionUID = -1247005005024543934L;
	// private boolean isMultipart;
	private File tmpDir;
	//Parameters
	private static final String FILENAME = "naziv_privitka";
	private static final String FILEDISPOSITION = "reportDisposition";/** eg. inline, attachment **/
	private static final String DOCUMENT_NAME = "privitak";
	private String FORBIDDEN_EXTENSIONS = "";
	private long FILE_SIZE_LIMIT = 500 * 1024 * 1024; // 500 MB
	private int MAX_MEM_SIZE = 4 * 1024;
	// Defaults
	private static final String DEFAULT_FILENAME = "Dokument";
	private static final String DEFAULT_FILEDISPOSITION = "inline";
	private static final String FILEUPLOAD_ID = "@@FileUpload_ID";
	// Facade
	public static final String COMPONENT = "Component";
	public static final String SERVICE = "Service";
	// Other
	protected boolean _use_relative_URI = true;
	protected boolean _use_dummy_parameter = true;
	// Test
//	private String _component = "hr.adriacomsoftware.app.server.karticno.gr.facade.KarticnoFacadeServer";
//	private String _service = "dodajPrivitak";

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		tmpDir = new File(((File) getServletContext().getAttribute(
				"javax.servlet.context.tempdir")).toString());
		if (!tmpDir.isDirectory()) {
			throw new ServletException(tmpDir.toString() + " nije direktorij");
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request,response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		AS2Record facade_request = readRequestFromURL(request);
		prepareRequestForFacade(facade_request);
		if(facade_request.get("servlet_service").equalsIgnoreCase("upload")){
			this.serviceFileUpload(request, response,facade_request);
		}
		else
			this.serviceFileDownload(request, response,facade_request);
	}

	/**********************************UPLOAD FILE**************************/
	public void serviceFileUpload(HttpServletRequest request,
			HttpServletResponse response,AS2Record as2_request ) {
		response.setContentType("text/html; charset=UTF-8");
		File file;
		FileItem item = null;

		// AS2Record response_vo = null;
		// define response
		response.setContentType("text/html");
		response.setHeader("Pragma", "No-cache");
		response.setDateHeader("Expires", 0);
		response.setHeader("Cache-Control", "no-cache");
		// Check that we have a file upload request
		// isMultipart = ServletFileUpload.isMultipartContent(req);
		// if( !isMultipart ){
		// out.println("<html>");
		// out.println("<head>");
		// out.println("<title>Dodavan</title>");
		// out.println("</head>");
		// out.println("<body>");
		// out.println("<p>Privitak nije dodan</p>");
		// out.println("</body>");
		// out.println("</html>");
		// return;
		// }
		try {
			// create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// maximum size that will be stored in memory
			factory.setSizeThreshold(MAX_MEM_SIZE);
			// Location to save data that is larger than maxMemSize.
			factory.setRepository(tmpDir);
			// create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// maximum file size to be uploaded.
			upload.setSizeMax(FILE_SIZE_LIMIT);
			// PrintWriter
			PrintWriter out = response.getWriter();
			try {
				// Parse the request to get file items.
				List<?> items = upload.parseRequest(request);
				// process the uploaded items
				Iterator<?> itr = items.iterator();
				while (itr.hasNext()) {
					item = (FileItem) itr.next();
					if (!item.isFormField()) {
						if (item.getSize() > FILE_SIZE_LIMIT) {
							responseFileUpload(item.getName(), ""," Privitak je prevelik",
									out);
							// res.sendError(
							// HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
							// "Datoteka je prevelika");
							return;
						}
						if (item.getFieldName().equals("privitak")) {
							if (FORBIDDEN_EXTENSIONS
									.contains(getFileExtension(item.getName()))) {
								responseFileUpload(as2_request.get(FILENAME),"",
										"Format privitika nije dozvoljen!", out);
								return;
							}
							// Get the uploaded file parameters
							String fileName = item.getName();
							/** UPLOAD FILE TO DATABASE **/
							Date date = new Date();
							file = new File(tmpDir, date.getTime() + "");
							item.write(file);
							as2_request.set(FILENAME,fileName.replace(" ", "_"));
							as2_request.set(DOCUMENT_NAME,AS2FileUtility.readFileToBytes(file));
							/** IF WE WANT TO UPLOAD FILE TO SERVER **/
							// // Get the uploaded file parameters
							// String fieldName = item.getFieldName();
							// String fileName = item.getName();
							// String contentType = item.getContentType();
							// boolean isInMemory = item.isInMemory();
							// long sizeInBytes = item.getSize();
							// // Write the file
							// if( fileName.lastIndexOf("\\") >= 0 ){
							// file = new File( filePath +
							// fileName.substring( fileName.lastIndexOf("\\")))
							// ;
							// }else{
							// file = new File( filePath +
							// fileName.substring(fileName.lastIndexOf("\\")+1))
							// ;
							// }
							// item.write( file ) ;
							// out.println("Uploaded Filename: " + fileName +
							// "<br>");
						}

						// Typically here you would process the file in some
						// way:
						// InputStream in = item.getInputStream();
						// ...

						if (!item.isInMemory())
							item.delete();

					} else{
						if(item.getFieldName().equals("transform_to"))
							as2_request.set("@@transform_to",item.getString());
						else
							as2_request.set(item.getFieldName(),item.getString());
					}
				}

			} catch (FileUploadException e) {
				responseFileUpload(as2_request.get(FILENAME),"",
						"Privitak nije moguće dodati!", out);
			}
			String pk ="";
			try {
				//TODO ispis brojača i poziva
				prepareRequestForFacade(as2_request);
				String srvRemoteAddr = null;
				srvRemoteAddr = request.getRemoteAddr();
				// Invocation Context
				AS2InvocationContext as2_context = prepareInvocationContext(as2_request, srvRemoteAddr);
				// Dispatch Request
				Object res = dispatchRequestToInvoker(as2_request, as2_context);
				
				if (res instanceof Throwable) {
					responseFileUpload(as2_request.get(FILENAME),"",
							res.toString(), out);
//					request.setAttribute("error", facade_response);
//					callPage("/jsp/Error.jsp", request, response);
				}else if (res instanceof AS2Record){
					Object data = ((AS2Record) res).getProperty(AS2Record._RESPONSE);
					if(data instanceof AS2Record){
						AS2Record as2_response = (AS2Record) data;
						pk = as2_response.get(FILEUPLOAD_ID);
					}
				}
				// else if (facade_response instanceof AS2Record) {
				// response_vo = (AS2Record) facade_response;
				// Object data =
				// response_vo.getProperty(AS2Record._RESPONSE);
				// }

			} catch (Exception e) {
				// req.setAttribute("error", e);
				// callPage("/jsp/Error.jsp",req,res);
				responseFileUpload(as2_request.get(FILENAME),"",
						"Pogrešni parametri za dodavanje privitka", out);
			}
			responseFileUpload(as2_request.get(FILENAME), pk,"success", out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void prepareRequestForFacade(AS2Record vo) {
		if(!vo.get("transform_to").equals("")){
			vo.set("@@transform_to",vo.get("transform_to"));
		}
		if(!vo.get("Component").equals("")){
			vo.setRemoteObject(vo.get("Component"));//TODO imena varijabli
		}
		if(!vo.get("Service").equals("")){
			vo.setRemoteMethod(vo.get("Service"));
		}
		if(!vo.get("remoteobject").equals("")){
			vo.setRemoteObject(vo.get("remoteobject"));//TODO imena varijabli
		}
		if(!vo.get("remotemethod").equals("")){
			vo.setRemoteMethod(vo.get("remotemethod"));
		}		
	}

	private void responseFileUpload(String fileName, String id_dokumenta,String reason, PrintWriter out) {
		out.println("<!doctype html>");
		out.println("<html>");
		out.println("<head>");
		out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		out.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\" >");
		out.println("<title>Adriacom Software inc.</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<script type=\"text/javascript\">");
		// out.println("function foo() {");
		// out.println("window.top.uploadComplete('"+ fileName+ "');");
		// out.println("}");
		if (reason.contains("success")) {
			out.println("if (parent.uploadComplete) parent.uploadComplete('"
					+ fileName + "','" + id_dokumenta + "');");

		} else
			out.println("if (parent.uploadFailed) parent.uploadFailed('"
					+ fileName + "','" + reason + "');");

		// out.println("if (parent.uploadComplete) parent.uploadComplete('"+
		// facade_request.get("naziv_dokumenta") + "');");
		out.println("</script>");
		out.println("</body>");
		out.println("</html>");
		out.flush();

	}

//	/**********************************UPLOAD FILE**************************/
//	public JSONObject serviceFileUpload(HttpServletRequest request,
//			HttpServletResponse response,AS2Record facade_request ) {
//		File file;
//		FileItem item = null;
//		JSONObject res_data = new JSONObject();
//		try {
//			// create a factory for disk-based file items
//			DiskFileItemFactory factory = new DiskFileItemFactory();
//			// maximum size that will be stored in memory
//			factory.setSizeThreshold(MAX_MEM_SIZE);
//			// Location to save data that is larger than maxMemSize.
//			factory.setRepository(tmpDir);
//			// create a new file upload handler
//			ServletFileUpload upload = new ServletFileUpload(factory);
//			// maximum file size to be uploaded.
//			upload.setSizeMax(FILE_SIZE_LIMIT);
//			// PrintWriter
//			try {
//				// Parse the request to get file items.
//				List<?> items = upload.parseRequest(request);
//				// process the uploaded items
//				Iterator<?> itr = items.iterator();
//				while (itr.hasNext()) {
//					item = (FileItem) itr.next();
//					if (!item.isFormField()) {
//						if (item.getSize() > FILE_SIZE_LIMIT) {
//							return res_data=TransportServletJSON.getErrorResponseObject("Problem prilikom dodavanja privitka. Privitak "+item.getName()+" je prevelik","-1");
//						}
//						if (item.getFieldName().equals("dokument")) {
//							if (FORBIDDEN_EXTENSIONS.contains(getFileExtension(item.getName()))) {
//								return res_data=TransportServletJSON.getErrorResponseObject("Problem prilikom dodavanja privitka. Format privitika "+item.getName()+" nije dozvoljen!","-1");
//							}
//							// Get the uploaded file parameters
//							String fileName = item.getName();
//							/** UPLOAD FILE TO DATABASE **/
//							Date date = new Date();
//							file = new File(tmpDir, date.getTime() + "");
//							item.write(file);
//							facade_request.set("naziv_dokumenta",fileName.replace(" ", "_"));
//							facade_request.set("dokument",AS2File.readFileToBytes(file));
//							/** IF WE WANT TO UPLOAD FILE TO SERVER **/
//							// // Get the uploaded file parameters
//							// String fieldName = item.getFieldName();
//							// String fileName = item.getName();
//							// String contentType = item.getContentType();
//							// boolean isInMemory = item.isInMemory();
//							// long sizeInBytes = item.getSize();
//							// // Write the file
//							// if( fileName.lastIndexOf("\\") >= 0 ){
//							// file = new File( filePath +
//							// fileName.substring( fileName.lastIndexOf("\\")))
//							// ;
//							// }else{
//							// file = new File( filePath +
//							// fileName.substring(fileName.lastIndexOf("\\")+1))
//							// ;
//							// }
//							// item.write( file ) ;
//							// out.println("Uploaded Filename: " + fileName +
//							// "<br>");
//						}
//
//						// Typically here you would process the file in some
//						// way:
//						// InputStream in = item.getInputStream();
//						// ...
//
//						if (!item.isInMemory())
//							item.delete();
//
//					} else{
//						if(item.getFieldName().equals("transform_to"))
//							facade_request.set("@@transform_to",item.getString());
//						else
//							facade_request.set(item.getFieldName(),item.getString());
//					}
//				}
//
//			} catch (FileUploadException e) {
//				return res_data=TransportServletJSON.getErrorResponseObject("Problem prilikom dodavanja privitka. Privitak nije moguće dodati! Razlog:\n" + e.getMessage(),"-1");
//			}
//			try {
//				return res_data = TransportServletJSON.callFacadeServer(facade_request, request, response);
////				Object facade_response = J2EEApplicationControllerFactory.getInstance().getApplicationController()
////						.executeRequest(facade_request);
////				if (facade_response instanceof Throwable) {
////					responseFileUpload(facade_request.get("naziv_dokumenta"),
////							facade_response.toString(), out);
////				}
//
//			} catch (Exception e) {
//				return res_data=TransportServletJSON.getErrorResponseObject("Problem prilikom dodavanja privitka. Pogrešni parametri za dodavanje privitka","-1");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return res_data;
//	}

	private String getFileExtension(String fileName) {
		String extension = ".default";
		if (fileName.lastIndexOf('.') != -1)
			extension = fileName.substring(fileName.lastIndexOf('.'),
					fileName.length());
		return extension;

	}

	/**********************************DOWNLOAD FILE**************************/
	public void serviceFileDownload(HttpServletRequest request, HttpServletResponse response,AS2Record as2_request ) {
		byte[] byteArray = null;
		AS2Record response_vo = null;
		response.setContentType("text/html; charset=UTF-8");
		try{
			//Parse the request for parameters
			String fileName = as2_request.getAsString(FILENAME, DEFAULT_FILENAME);
			String fileDisposition = as2_request.getAsString(FILEDISPOSITION, DEFAULT_FILEDISPOSITION);
			try{
				String srvRemoteAddr = null;
				srvRemoteAddr = request.getRemoteAddr();
				// Invocation Context
				AS2InvocationContext as2_context = prepareInvocationContext(as2_request, srvRemoteAddr);
				// Dispatch Request
				Object res = dispatchRequestToInvoker(as2_request, as2_context);
				if (res instanceof Throwable){
					request.setAttribute("error", res);
//					callPage("/jsp/Error.jsp", request,response);
					errorResponse("Problem u bazi" + as2_request.toString(),response.getWriter());
				}else if (res instanceof AS2Record){
					response_vo = (AS2Record) res;
					Object data = response_vo.getProperty(AS2Record._RESPONSE);
					if(data instanceof AS2Record){
						byteArray = ((AS2Record) data).getAsBytes(DOCUMENT_NAME);
						Date date = new Date();
						String timeFileName = date.getTime() + "";
						File file = new File(tmpDir, timeFileName);
						AS2FileUtility.writeFileFromBytes(file, byteArray);
						//TODO problem - rješen u smartgwt.mobile
//						response.setContentType();
						response.setContentType(new MimetypesFileTypeMap().getContentType(file) + "; charset=UTF-8");
						response.setContentLength(byteArray.length);
						response.setCharacterEncoding("UTF-8");
						response.setHeader("X-UA-Compatible","IE=edge,chrome=1");
						response.setHeader("Content-disposition",fileDisposition +"; filename=\"" + /*URLEncoder.encode(*/fileName/*, "UTF-8")*/+"\"");
						ServletOutputStream ouputStream = response.getOutputStream();
						ouputStream.write(byteArray, 0, byteArray.length);
						ouputStream.flush();
						AS2FileUtility.deleteFile(timeFileName);
						ouputStream.close();
					}
				}

			}catch(Exception e){
				request.setAttribute("error", e);
				errorResponse(e.getMessage(),response.getWriter());
//				callPage("/jsp/Error.jsp",request,response);
//				 e.printStackTrace();

			}
      } catch (Exception e) {
    	  e.printStackTrace();
      }
	}
	
	private void errorResponse(String reason, PrintWriter out) {
		out.println("<html>");
		out.println("<head><title>Pogreška</title></head>");
		out.println("<body>");
		out.println("<h>Pogreška prilikom dohvaćanja privitka!</h>");
		out.println("<p>Razlog:<br> "+reason+"</h>");
		out.println("</body>");
		out.println("</html>");
		out.flush();
	}

//	/**********************************DOWNLOAD FILE**********************************/
//	protected AS2Record readRequestParameters(HttpServletRequest req) {
//		AS2Record inputFields = new AS2Record();
//		String service = null;
//		String component = null;
//
//		try {
//			component = req.getParameterValues(COMPONENT)[0]; // (3) component
//			service = req.getParameterValues(SERVICE)[0]; // (4) service
//		} catch (Exception e) {
//			// No need to handle the NullPointerException
//			// in the case service or component are not passed in.
//		}
//		inputFields.setService(service);
//		inputFields.setComponent(component);
////		inputFields.setProperty("dokument", "");
////		inputFields.setProperty("naziv_dokumenta", "");
//		inputFields.setProperty("@@log_in_host_name", req.getRemoteAddr());
//		@SuppressWarnings("unchecked")
//		Enumeration<String> E = req.getParameterNames();
//		while (E.hasMoreElements()) {
//			String name = E.nextElement();
//			String value = req.getParameterValues(name)[0];
//			// Ignoriraj neke parametre medu poslovnim podacima.
//			// Ova polja su sistemska pomocna polja.
//			if (!name.equals(SERVICE) && !name.equals(COMPONENT)
//					&& !name.startsWith("@@"))
//				inputFields.setProperty(name, value);
//		}
//		return inputFields;
//	}

//	public void callPage(String s, HttpServletRequest httpservletrequest,
//			HttpServletResponse httpservletresponse) throws IOException,
//			ServletException {
//		callURI(s, httpservletrequest, httpservletresponse);
//	}

//	public void callURI(String s, HttpServletRequest httpservletrequest,
//			HttpServletResponse httpservletresponse) throws IOException,
//			ServletException {
//		ServletContext servletcontext;
//		String str = null;
//		if (_use_relative_URI) {
//			servletcontext = getServletContext();
//			str = s;
//		} else {
//			servletcontext = getServletContext().getContext(s);
//		}
//		RequestDispatcher requestdispatcher = servletcontext
//				.getRequestDispatcher(str);
//		requestdispatcher.forward(httpservletrequest, httpservletresponse);
//	}

	// // Returns the contents of the file in a byte array.
	// public static byte[] getBytesFromFile(File file) throws IOException {
	// InputStream is = new FileInputStream(file);
	// // Get the size of the file
	// long length = file.length();
	//
	// // You cannot create an array using a long type.
	// // It needs to be an int type.
	// // Before converting to an int type, check
	// // to ensure that file is not larger than Integer.MAX_VALUE.
	// if (length > Integer.MAX_VALUE) {
	// // File is too large
	// }
	//
	// // Create the byte array to hold the data
	// byte[] bytes = new byte[(int)length];
	//
	// // Read in the bytes
	// int offset = 0;
	// int numRead = 0;
	// while (offset < bytes.length
	// && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	// offset += numRead;
	// }
	//
	// // Ensure all the bytes have been read in
	// if (offset < bytes.length) {
	// throw new IOException("Could not completely read file "+file.getName());
	// }
	//
	// // Close the input stream and return bytes
	// is.close();
	// return bytes;
	// }
	//
	//
	// public byte[] stringToByteArray (String s) {
	// byte[] byteArray = new byte[s.length()];
	// for (int i = 0; i < s.length(); i++) {
	// byteArray[i] = (byte) s.charAt(i);
	// }
	// return byteArray;
	// }
	// //EXTRA
	// public static byte[] getBytesFromInputStream(InputStream is)
	// {
	// ByteArrayOutputStream os = new ByteArrayOutputStream();
	// try
	// {
	// byte[] buffer = new byte[0xFFFF];
	//
	// for (int len; (len = is.read(buffer)) != -1;)
	// os.write(buffer, 0, len);
	//
	// os.flush();
	//
	// return os.toByteArray();
	// }
	// catch (IOException e)
	// {
	// return null;
	// }
	// }
}