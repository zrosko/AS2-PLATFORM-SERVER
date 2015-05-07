package hr.as2.inf.server.requesthandlers.rest.mobile;


import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2InvocationContext;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.reports.AS2ReportConstants;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.server.converters.doctohtml.AS2MDoc2HtmlConverter;
import hr.as2.inf.server.requesthandlers.AS2ServerRequestHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javaxt.io.File;
import javaxt.utils.Base64;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.converter.core.XWPFConverterException;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class AS2MServlet extends AS2ServerRequestHandler implements AS2ReportConstants {
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	private static final long serialVersionUID = -7449311593970677641L;
	private HttpServletResponse response;

	protected AS2Record readRequestParameters(HttpServletRequest req) {
		AS2Record inputFields = new AS2Record();
		inputFields.set("@@log_in_host_name", req.getRemoteAddr());
		Enumeration<String> E = req.getParameterNames();
		while (E.hasMoreElements()) {
			String name = E.nextElement();
			String value = req.getParameterValues(name)[0];
			inputFields.set(name, value);
		}
		return inputFields;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		response = resp;
		response.setCharacterEncoding("UTF-8");
		AS2Record facade_request = readRequestParameters(req);
		System.out.println("SERVER WAS CALLED!!");
		List<LinkedHashMap<String,String>> res_data = new ArrayList<LinkedHashMap<String,String>>();
		if (facade_request.getProperty("service") != null && (facade_request.getProperty("service").equals("listajSjednice"))) {
			res_data = serviceListajSjednice(facade_request, req, response);
			prepareJSONresponse(response, res_data, new JSONArray(), null);
		}else if (facade_request.getProperty("service") != null && (facade_request.getProperty("service").equals("searchSjednice"))) {
			res_data = serviceSearchSjednice(facade_request, req, response);
			prepareJSONresponse(response, res_data, new JSONArray(), null);
		}else if (facade_request.getProperty("service") != null && ((facade_request.getProperty("service").equals("serviceFolders") || facade_request.getProperty("service").equals("listajDnevniRed")))) {
			res_data = serviceListajDnevniRed(facade_request, req, response);
			prepareJSONresponse(response, res_data, new JSONArray(), null);
		}else if (facade_request.getProperty("service") != null && facade_request.getProperty("service").equals("showFile")) {
			serviceShowFile(facade_request, req, response);
		}
		else if (facade_request.getProperty("service") != null && facade_request.getProperty("service").equals("showFileByteArray")) {
			res_data = serviceShowFileByteArray(facade_request, req, response);
			prepareJSONresponse(response, res_data, new JSONArray(), null);
		}
		else if (facade_request.getProperty("service") != null && facade_request.getProperty("service").equals("tecajna_lista_kratka")) {
			serviceTecajnaLista(facade_request, req, response);
		}
	}


	private List<LinkedHashMap<String,String>>  serviceListajSjednice(AS2Record facade_request,
			HttpServletRequest req, HttpServletResponse resp){
		javaxt.io.Directory folder = new javaxt.io.Directory(facade_request.get("url"));
		List<LinkedHashMap<String,String>> result = new ArrayList<LinkedHashMap<String,String>>();
		if (folder.exists()) {
			result = listSjedniceFromDocx(folder);
		} else {
			LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
			error.put("error", "Tražena datoteka ne postoji");
			result.add(error);
		}
		return result;
	}

	public List<LinkedHashMap<String,String>> listSjedniceFromDocx(javaxt.io.Directory folder){
		System.out.println("ROSKO listDatumFromDnevniRed xxx1="
				+ folder.toString());
		List<LinkedHashMap<String,String>> sjednice = new ArrayList<LinkedHashMap<String,String>>();
		LinkedHashMap<String,String> sjednica;
		for (javaxt.io.Directory year : folder.getSubDirectories()) {
			for (javaxt.io.Directory month : year.getSubDirectories()) {
				if (month.getName().equalsIgnoreCase("Dnevni red")) {
					for (javaxt.io.File file : month.getFiles()) {
						sjednica = new LinkedHashMap<String,String>();
						sjednica.put("datum",getDatumFromDocx(file.getName()));
						sjednica.put("sjednica",getSjednicaFromDocx(file.getName()));
						sjednica.put("sjednicaFileName", file.getName());
						//Izgled foldera
						sjednica.put("fileType", "folder");
						sjednice.add(sjednica);
					}
				}
			}
		}
		return sjednice;
	}

//Staro
//	public JSONArray listSjedniceFromDocx(javaxt.io.Directory folder)
//			throws JSONException {
//		System.out.println("ROSKO listDatumFromDnevniRed xxx1="
//				+ folder.toString());
//		JSONArray sjednice = new JSONArray();
//		JSONObject sjednica;
//		for (javaxt.io.Directory year : folder.getSubDirectories()) {
//			for (javaxt.io.Directory month : year.getSubDirectories()) {
//				if (month.getName().equalsIgnoreCase("Dnevni red")) {
//					for (javaxt.io.File file : month.getFiles()) {
//						sjednica = new JSONObject();
//						sjednica.put("datum",getDatumFromDocx(file.getName()));
//						sjednica.put("sjednica",getSjednicaFromDocx(file.getName()));
//						sjednica.put("sjednicaFileName", file.getName());
//						//Izgled foldera
//						sjednica.put("fileType", "folder");
//						sjednice.put(sjednica);
//					}
//				}
//			}
//		}
//		return sjednice;
//	}


	private List<LinkedHashMap<String,String>> serviceListajDnevniRed(AS2Record facade_request,
			HttpServletRequest req, HttpServletResponse resp){
		List<LinkedHashMap<String,String>> result = new ArrayList<LinkedHashMap<String,String>>();
		javaxt.io.Directory folder = new javaxt.io.Directory(facade_request.get("url"));
		if (folder.exists()) {
			LinkedHashMap<String,String> object;
			if (facade_request.get("service") != null && facade_request.get("service").equals("listajDnevniRed")) {
				//Nema datoteke u \godina\Dnevni red
				if (folder.exists()) {
					if (folder.getChildren().isEmpty()) {
						return result;
					}
				}
				result = new ArrayList<LinkedHashMap<String,String>>();
				object = new LinkedHashMap<String,String>();
				object.put("fileName", "Dnevni red");
				object.put("fileType", "folder");
				result.add(object);
				object = new LinkedHashMap<String,String>();
				object.put("fileName", "Zapisnik sa sjednice");
				object.put("fileNameFull", "Zapisnik sa sjednice.docx");
				object.put("fileExtension", "docx");
				object.put("fileType", "file");
				result.add(object);
			}else{
				result = serviceFolders(facade_request, req, resp);
			}

		} else {
			LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
			error.put("error", "Tražena datoteka ne postoji");
			result.add(error);
		}
		return result;
	}


	private List<LinkedHashMap<String,String>>  serviceFolders(AS2Record facade_request,
			HttpServletRequest req, HttpServletResponse resp){
		List<LinkedHashMap<String,String>> result = new ArrayList<LinkedHashMap<String,String>>();

		javaxt.io.Directory folder = new javaxt.io.Directory(
				facade_request.get("url"));
		if (folder.exists()) {
			LinkedHashMap<String,String> object;
			for (Object file : folder.getChildren()) {
				object = getFilesFromFolder(file);
				if(object.size()!=0)
					result.add(object);
			}
		} else {
			LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
			error.put("error", "Tražena datoteka ne postoji");
			result.add(error);
		}
		return result;

	}


	private LinkedHashMap<String, String> getFilesFromFolder(Object file) {
		LinkedHashMap<String, String> object = new LinkedHashMap<String, String>();
		if (file instanceof javaxt.io.Directory) {
			javaxt.io.Directory xtDirectory = (javaxt.io.Directory) file;
			object.put("fileName", xtDirectory.getName());
			object.put("fileType", "folder");

		} else {
			javaxt.io.File xtFile = (javaxt.io.File) file;
			if (!xtFile.getName().startsWith("dnevno_izvje")) {
				object.put("fileName", getFileNameWithoutExtension(xtFile));
				object.put("fileNameFull", xtFile.getName());
				object.put("fileExtension", xtFile.getExtension());
				object.put("fileType", "file");
			}
		}
		return object;
	}

	//Staro
//	private JSONObject getFilesFromFolder(Object file) {
//		JSONObject object = new JSONObject();
//		if (file instanceof javaxt.io.Directory) {
//			javaxt.io.Directory xtDirectory = (javaxt.io.Directory) file;
//			try {
//				object.put("fileName", xtDirectory.getName());
//				object.put("fileType", "folder");
//			} catch (JSONException e) {
//			}
//
//		} else {
//			javaxt.io.File xtFile = (javaxt.io.File) file;
//			if (!xtFile.getName().startsWith("dnevno_izvje")) {
//				try {
//					object.put("fileName", getFileNameWithoutExtension(xtFile));
//					object.put("fileNameFull", xtFile.getName());
//					object.put("fileExtension", xtFile.getExtension());
//					object.put("fileType", "file");
//				} catch (JSONException e) {
//				}
//			}
//		}
//		return object;
//	}

//	private Boolean hasFiles(AS2Record facade_request,javaxt.io.Directory folder) throws JSONException {
//		if (folder.exists()) {
//			if(folder.getChildren()!=null) {
//				return true;
//			}
//		}
//		return false;
//
//
//	}

	private void serviceTecajnaLista(AS2Record vo, HttpServletRequest req, HttpServletResponse resp) {
		String srvRemoteAddr = null;
		try {
			vo.set("@@transform_to",
					"hr.adriacomsoftware.app.common.jb.dto.OsnovniVo");
			vo.setRemoteObject("hr.adriacomsoftware.app.server.jb.facade.BankaFacadeServer");
			vo.setRemoteMethod("izvjestajTecajnaLista");
			AS2User user = new AS2User();
			vo.set(AS2Constants.USER_OBJ, user);
			vo.set("datum", vo.getProperty("datum"));
			vo.set("upit", vo.getProperty("service"));
			srvRemoteAddr = req.getRemoteAddr();
			// Invocation Context
			AS2InvocationContext as2_context = prepareInvocationContext(vo, srvRemoteAddr);
			// Dispatch Request
			ServletContext context = this.getServletConfig().getServletContext();
			vo.set("war_path",	context.getRealPath(""));
			Object res = dispatchRequestToInvoker(vo, as2_context);
			
//			res = J2EEApplicationControllerFactory.getInstance()
//					.getApplicationController().executeRequest(vo); 
			if (res instanceof AS2Record) {
				AS2Record j2eevo = (AS2Record) res;
				Object data = j2eevo.getProperty(AS2Record._RESPONSE);
				if (data instanceof AS2RecordList) {
					AS2RecordList j2eers = (AS2RecordList) data;
					JSONArray res_meta = new JSONArray(
							j2eers.getMetaDataForJSON());
					List<LinkedHashMap<String,Object>> result = j2eers.getRowsForJSON();
					prepareJSONresponseObject(resp, result, res_meta, j2eers);
				} else {
					prepareERRORresponse("Nema Podataka");
				}
			}
		} catch (Exception e) {
			prepareERRORresponse("Problem: "+e.getMessage());
		}
	}


	private List<LinkedHashMap<String,String>> serviceSearchSjednice(AS2Record facade_request,
			HttpServletRequest req, HttpServletResponse resp){
		javaxt.io.Directory folder = new javaxt.io.Directory(facade_request.get("url"));
		String searchValue = facade_request.get("searchValue");
		List<LinkedHashMap<String,String>> result = new ArrayList<LinkedHashMap<String,String>>();
		boolean contains=false;
		if (folder.exists()) {
			List<LinkedHashMap<String,String>> data = searchListSjedniceFromDocx(folder);
			for(LinkedHashMap<String,String> map : data){
				for(String value: map.values()){
					if(value.contains(searchValue)){
						contains=true;
					}
				}
				if(contains){
					result.add(map);
					contains=false;
				}
			}
		} else {
			LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
			error.put("error", "Tražena datoteka ne postoji");
			result.add(error);
		}
		return result;
	}

	public List<LinkedHashMap<String,String>>  searchListSjedniceFromDocx(javaxt.io.Directory folder){
		System.out.println("ROSKO listDatumFromDnevniRed xxx1="
				+ folder.toString());
		LinkedHashMap<String,String> sjednica;
		List<LinkedHashMap<String,String>> sjednice = new ArrayList<LinkedHashMap<String,String>>();
		for (javaxt.io.Directory year : folder.getSubDirectories()) {
			for (javaxt.io.Directory month : year.getSubDirectories()) {
				if (month.getName().equalsIgnoreCase("Dnevni red")) {
					for (javaxt.io.File file : month.getFiles()) {
						sjednica = new LinkedHashMap<String,String>();
						sjednica.put("datum",getDatumFromDocx(file.getName()));
						sjednica.put("sjednica",getSjednicaFromDocx(file.getName()));
						sjednica.put("sjednicaFileName", file.getName());
						sjednica.put("fileType", "folder");
						sjednice.add(sjednica);
					}
				}
			}
		}
		return sjednice;
	}

	private List<LinkedHashMap<String,String>>  serviceShowFileByteArray(AS2Record facade_request,
			HttpServletRequest req, HttpServletResponse response) {
		 List<LinkedHashMap<String,String>> result = new ArrayList<LinkedHashMap<String,String>>();
		if (facade_request.get("fileName").contains("Zapisnik")) {
			facade_request.set("url", getZapisnikFolderPath(facade_request)
					+ "\\" + facade_request.get("sjednicaFileName"));
		}
		if (facade_request.get("fileName").contains("Dnevni")) {
			facade_request.set("url", getDnevniRedFolderPath(facade_request)
					+ "\\" + facade_request.get("sjednicaFileName"));
		}
		File file = new javaxt.io.File(facade_request.get("url"));
		if (file.exists()) {
			LinkedHashMap<String,String> fileMap = new LinkedHashMap<String,String>();
			fileMap.put("contentType","application/pdf");
			byte[] fileByteArray = new byte[0];
//			String encodedFile="";
			if (file.getExtension().equals("docx")|| file.getExtension().equals("xlsx"))
				try {
					fileByteArray=showFileByteArrayDOCX(response, file);
				} catch (XWPFConverterException e) {
					LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
					error.put("error", "Problem: "+ e.getMessage());
					result.add(error);
				} catch (IOException e) {
					LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
					error.put("error", "Problem: "+ e.getMessage());
					result.add(error);
				}
			else if (file.getExtension().equals("doc")) {
				String converter = facade_request.get("converter");
				if(converter!=null && converter.equals("doc2Html")){
					// converting doc to html
					fileMap.put("contentType","text/html");
					AS2MDoc2HtmlConverter msDocument=null;
					try{
						msDocument = new AS2MDoc2HtmlConverter(new FileInputStream(file.toFile()));
						fileByteArray = msDocument.extractParagraphTexts(file, response);
					}catch (IOException e) {
						LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
						error.put("error", "Problem: "+ e.getMessage());
						result.add(error);
					}
				}
				else{
					// converting doc to pdf
					try {
						fileByteArray=showFileByteArrayDOC2Pdf(response, file);
					} catch (DocumentException e) {
						LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
						 error.put("error", "Problem: "+ e.getMessage());
						 result.add(error);
					}catch (IOException e) {
						LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
						error.put("error", "Problem: "+ e.getMessage());
						result.add(error);
					}
				}
			} else{
				fileByteArray=file.getBytes().toByteArray();
			}
			String encodedFile = Base64.encodeBytes(fileByteArray);
			fileMap.put("fileByteArray",encodedFile);
			result.add(fileMap);
		}else {
			LinkedHashMap<String,String> error = new LinkedHashMap<String,String>();
			error.put("error", "Tražena datoteka ne postoji");
			result.add(error);
		}
		return result;
	}

	private byte[] showFileByteArrayDOCX(HttpServletResponse response, File file) throws XWPFConverterException, IOException {
		// 1) Load DOCX into XWPFDocument
		XWPFDocument document = null;
		try {
			document = new XWPFDocument(file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			prepareERRORresponse(e.getMessage());
		}
		// 2) Prepare Pdf options
		PdfOptions options = PdfOptions.create().fontEncoding("cp1250");
		// 3) Convert XWPFDocument to Pdf
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PdfConverter.getInstance().convert(document,bos, options);
		return bos.toByteArray();

	}

	private byte[] showFileByteArrayDOC2Pdf(HttpServletResponse response, File file)
			throws DocumentException, FileNotFoundException, IOException {
		POIFSFileSystem fs = null;
		Document document = new Document();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		fs = new POIFSFileSystem(new FileInputStream(file.toFile()));
		HWPFDocument doc = new HWPFDocument(fs);
		WordExtractor we = new WordExtractor(doc);
		PdfWriter writer = PdfWriter.getInstance(document, bos);
		Range range = doc.getRange();
		document.open();
		writer.setPageEmpty(true);
		document.newPage();
		writer.setPageEmpty(true);
		String[] paragraphs = we.getParagraphText();
		for (int i = 0; i < paragraphs.length; i++) {
			org.apache.poi.hwpf.usermodel.Paragraph pr = range.getParagraph(i);
			int j = 0;
			while (true) {
				CharacterRun run = pr.getCharacterRun(j++);
				// System.out.println("Color " + run.getColor());
				// System.out.println("Font size " + run.getFontSize());
				// System.out.println("Font Name " + run.getFontName());
				// System.out.println(run.isBold() + " " + run.isItalic()
				// + " " + run.getUnderlineCode());
				// System.out.println("Text is " + run.text());
				if (run.getEndOffset() == pr.getEndOffset()) {
					break;
				}
			}
			paragraphs[i] = paragraphs[i].replaceAll("\\cM?\r?\n", "");
//			System.out.println("Length:" + paragraphs[i].length());
//			System.out.println("Paragraph" + i + ": "
//					+ paragraphs[i].toString());
			// add the paragraph to the document
			document.add(new Paragraph(paragraphs[i]));
		}
		// close the document
		document.close();
		return bos.toByteArray();

	}

	private void serviceShowFile(AS2Record facade_request,
			HttpServletRequest req, HttpServletResponse response) {
		if (facade_request.get("fileName").contains("Zapisnik")) {
			facade_request.set("url", getZapisnikFolderPath(facade_request)
					+ "\\" + facade_request.get("sjednicaFileName"));
		}
		if (facade_request.get("fileName").contains("Dnevni")) {
			facade_request.set("url", getDnevniRedFolderPath(facade_request)
					+ "\\" + facade_request.get("sjednicaFileName"));
		}
		File file = new javaxt.io.File(facade_request.get("url"));
		if (file.exists()) {
			if (file.getExtension().equals("docx")
					|| file.getExtension().equals("xlsx"))
				showFileDOCX(response, file);
			else if (file.getExtension().equals("doc"))
				showFileDOC(response, file);
			else {
				response.setBufferSize(DEFAULT_BUFFER_SIZE);
				response.setContentType(file.getContentType());
				response.setContentLength((int) file.toFile().length());
				response.setHeader("Content-Disposition", "inline; filename=\""
						+ file.getName() + "\"");
				try {
					byte[] byteArray = file.getBytes().toByteArray();
					ServletOutputStream ouputStream = response
							.getOutputStream();
					ouputStream.write(byteArray, 0, byteArray.length);
					ouputStream.flush();
					ouputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					prepareERRORresponse(e.getMessage());
				}
			}

		} else {
			prepareERRORresponse("Tražena datoteka ne postoji!");
		}
	}

	private void showFileDOCX(HttpServletResponse response, File file) {
		// 1) Load DOCX into XWPFDocument
		XWPFDocument document = null;
		try {
			document = new XWPFDocument(file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			prepareERRORresponse(e.getMessage());
		}
		// 2) Prepare Pdf options
		PdfOptions options = PdfOptions.create().fontEncoding("cp1250");
		// options.fontEncoding("cp1250");
		// 3) Convert XWPFDocument to Pdf
		// OutputStream out = null;
		// try {
		// out = new FileOutputStream(new
		// java.io.File("\\\\Srvfile1\\public\\Razmjena\\Maja\\sjednice\\2014\\Dnevni red\\05.SJED.OD 22.01.2014..pdf"));
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }
		try {
			PdfConverter.getInstance().convert(document,
					response.getOutputStream(), options);
		} catch (IOException e) {
			prepareERRORresponse("Problem: "+e.getMessage());
		}
	}

	private void showFileDOC(HttpServletResponse response, File file) {
		POIFSFileSystem fs = null;
		Document document = new Document();

		try {
			fs = new POIFSFileSystem(new FileInputStream(file.toFile()));

			HWPFDocument doc = new HWPFDocument(fs);
			WordExtractor we = new WordExtractor(doc);

			// OutputStream file = new FileOutputStream(new
			// java.io.File("D:/test.pdf"));

			OutputStream out = response.getOutputStream();
			PdfWriter writer = PdfWriter.getInstance(document, out);

			Range range = doc.getRange();
			document.open();
			writer.setPageEmpty(true);
			document.newPage();
			writer.setPageEmpty(true);

			String[] paragraphs = we.getParagraphText();
			for (int i = 0; i < paragraphs.length; i++) {
				org.apache.poi.hwpf.usermodel.Paragraph pr = range
						.getParagraph(i);
				int j = 0;
				while (true) {
					CharacterRun run = pr.getCharacterRun(j++);
					// System.out.println("Color " + run.getColor());
					// System.out.println("Font size " + run.getFontSize());
					// System.out.println("Font Name " + run.getFontName());
					// System.out.println(run.isBold() + " " + run.isItalic()
					// + " " + run.getUnderlineCode());
					// System.out.println("Text is " + run.text());
					if (run.getEndOffset() == pr.getEndOffset()) {
						break;
					}
				}
				paragraphs[i] = paragraphs[i].replaceAll("\\cM?\r?\n", "");
				System.out.println("Length:" + paragraphs[i].length());
				System.out.println("Paragraph" + i + ": "
						+ paragraphs[i].toString());
				// add the paragraph to the document
				document.add(new Paragraph(paragraphs[i]));
			}
		} catch (Exception e) {
			prepareERRORresponse("Problem: " + e.getMessage());
		} finally {
			// close the document
			document.close();
		}
	}

	public String getDatumFromDocx(String fileName) {
		int startOfDate = fileName.indexOf("OD") + 3;
		return fileName.substring(startOfDate, startOfDate + 10);
	}

	public String getSjednicaFromDocx(String fileName) {
		int endOfRedBr = fileName.indexOf("SJED");
		return fileName.substring(0, endOfRedBr) + " sjednica ("
				+ getDatumFromDocx(fileName) + ".)";
	}

	private String getZapisnikFolderPath(AS2Record facade_request) {
		return facade_request.get("app_url") + "\\"
				+ facade_request.get("godina")
				+ "\\Zapisnici";
	}

	private String getDnevniRedFolderPath(AS2Record facade_request) {
		return facade_request.get("app_url") + "\\"
				+ facade_request.get("godina")
				+ "\\Dnevni red";

	}

	private String getFileNameWithoutExtension(File file) {
		if(file.getExtension()!=null || !file.getExtension().equals(""))
			return file.getName().substring(0,file.getName().indexOf(file.getExtension()) - 1);
		else
			return file.getName();
	}

	private void prepareJSONresponseObject(HttpServletResponse resp,
		List<LinkedHashMap<String,Object>>  res_data, JSONArray res_meta, AS2RecordList j2eers) {
		JSONObject responseJSON = new JSONObject();
		JSONObject payloadJSON = new JSONObject();
		try {
			payloadJSON.put("endRow", 100);
			payloadJSON.put("startRow", 0);
			payloadJSON.put("status", 0);
			if (!res_data.isEmpty()) {
				payloadJSON.put("totalRows", res_data.size());
				payloadJSON.put("data", res_data);
			}
			else {
				payloadJSON.put("totalRows", 0);
				payloadJSON.put("data", new JSONObject());
			}
			payloadJSON.put("metaData", res_meta);
			responseJSON.put("response", payloadJSON);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		try {
			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().print(responseJSON);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private void prepareJSONresponse(HttpServletResponse resp,
			List<LinkedHashMap<String,String>>  res_data, JSONArray res_meta, AS2RecordList j2eers) {
			JSONObject responseJSON = new JSONObject();
			JSONObject payloadJSON = new JSONObject();
			try {
				payloadJSON.put("endRow", 100);
				payloadJSON.put("startRow", 0);
				payloadJSON.put("status", 0);
				if (!res_data.isEmpty()) {
					payloadJSON.put("totalRows", res_data.size());
					payloadJSON.put("data", res_data);
				}
				else {
					payloadJSON.put("totalRows", 0);
					payloadJSON.put("data", new JSONObject());
				}
				payloadJSON.put("metaData", res_meta);
				responseJSON.put("response", payloadJSON);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			try {
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().print(responseJSON);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	private void prepareERRORresponse(String errorMessage) {
		PrintWriter out;
		try {
			out = response.getWriter();
			out.println("<div style=\"margin:0px; padding:2px; font-size:16px; font-family: Helvetica, sans-serif; font-style: normal; line-height: 16px;\">"
					+ errorMessage + "</div>");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

//	private JSONArray serviceZapisnik(AS2Record facade_request,
//			HttpServletRequest req, HttpServletResponse resp) throws JSONException
//			  {
//		String zapisnikUrl = getZapisnikFolderPath(facade_request);
//		javaxt.io.Directory zapisnikFolder = new javaxt.io.Directory(
//				zapisnikUrl);
//		JSONArray res_data = new JSONArray();
//		if (zapisnikFolder.exists()) {
//			JSONObject object = new JSONObject();
//			for (javaxt.io.File zapisnik : zapisnikFolder.getFiles()) {
//				if (zapisnik.getName().contains(facade_request.get("datum"))) {
//					object = getFilesFromFolder(zapisnik);
//					res_data.put(object);
//					break;
//				}
//			}
//		} else {
//			JSONObject object = new JSONObject();
//			object.put("error", "Tražena datoteka ne postoji");
//			res_data.put(object);
//		}
//		return res_data;
//	}



	// private void servicePopisSjednica(AS2Record facade_request,
	// HttpServletRequest req, HttpServletResponse resp)
	// throws UnsupportedEncodingException {
	// javaxt.io.Directory folder = new javaxt.io.Directory(
	// facade_request.get("url"));
	// JSONArray res_data = new JSONArray();
	// System.out.println("ROSKO xxx0=" + folder.toString());
	// if (folder.exists()) {
	// List<File> sjednice = new ArrayList<File>();
	// for (javaxt.io.Directory year : folder.getSubDirectories()) {
	// for (javaxt.io.Directory month : year.getSubDirectories()) {
	// if (month.getName().equalsIgnoreCase("Dnevni red")) {
	// for (javaxt.io.File file : month.getFiles()) {
	// sjednice.add(file);
	// }
	// }
	// }
	// }
	// for (File sjednica : sjednice) {
	// JSONObject objekt = new JSONObject();
	// try {
	// objekt.put("fileName",
	// getBrojSjedniceFromDnevniRedFolder(sjednica.getName()));
	// objekt.put("fileNameFull", sjednica.getName());
	// objekt.put("fileType", "folder");
	// res_data.put(objekt);
	// } catch (JSONException e) {
	// prepareERRORresponse(resp, e.getMessage());
	// e.printStackTrace();
	// }
	// }
	// prepareJSONresponse(resp, res_data, new JSONArray(), null);
	// } else {
	// prepareERRORresponse(resp, "Tražena datoteka ne postoji!");
	// }
	// }

	// private void serviceDatumiSjedniceDOCX(AS2Record facade_request,
	// HttpServletRequest req, HttpServletResponse resp)
	// throws UnsupportedEncodingException {
	// javaxt.io.Directory folder = new javaxt.io.Directory(
	// facade_request.get("url"));
	// JSONArray res_data = new JSONArray();
	// System.out.println("ROSKO xxx0=" + folder.toString());
	// if (folder.exists()) {
	// List<String> sjednice = listSjedniceFromDOCX(folder);
	// for (String sjednica : sjednice) {
	// JSONObject objekt = new JSONObject();
	// try {
	// objekt.put("datum", sjednica.substring(0,sjednica.indexOf("-")));
	// objekt.put("brojSjednice",
	// sjednica.substring(sjednica.indexOf("-")+1,sjednica.length()));
	// objekt.put("sjednica", sjednica);
	// objekt.put("fileType", "folder");
	// res_data.put(objekt);
	// } catch (JSONException e) {
	// prepareERRORresponse(resp, e.getMessage());
	// e.printStackTrace();
	// }
	// }
	// prepareJSONresponse(resp, res_data, new JSONArray(), null);
	// } else {
	// prepareERRORresponse(resp, "Tražena datoteka ne postoji!");
	// }
	// }

	/************* Stari način *************/
	// private void serviceDatumiSjednice(AS2Record facade_request,
	// HttpServletRequest req, HttpServletResponse resp) throws
	// UnsupportedEncodingException {
	//
	// javaxt.io.Directory folder = new
	// javaxt.io.Directory(facade_request.get("url"));
	// JSONArray res_data = new JSONArray();
	// System.out.println("ROSKO xxx0="+folder.toString());
	// if (folder.exists()) {
	// List<String> datumi = listDatumiSjednicaFromFolder(folder);
	// for (String datum : datumi) {
	// JSONObject objekt = new JSONObject();
	// try {
	// objekt.put("datum", datum);
	// res_data.put(objekt);
	// } catch (JSONException e) {
	// prepareERRORresponse(resp, e.getMessage());
	// e.printStackTrace();
	// }
	// }
	// prepareJSONresponse(resp, res_data, new JSONArray(), null);
	// } else {
	// prepareERRORresponse(resp, "Tražena datoteka ne postoji!");
	// }
	// }
	// private void serviceFolders(AS2Record facade_request,
	// HttpServletRequest req, HttpServletResponse resp) {
	// JSONArray res_data = new JSONArray();
	// try {
	// javaxt.io.Directory folder = new javaxt.io.Directory(
	// facade_request.get("url"));
	// if (folder.exists()) {
	// JSONObject objekt;
	// for (Object file : folder.getChildren()) {
	// objekt = new JSONObject();
	// if (file instanceof javaxt.io.Directory) {
	// javaxt.io.Directory xtDirectory = (javaxt.io.Directory) file;
	// objekt.put("fileName", xtDirectory.getName());
	// objekt.put("fileType", "folder");
	// res_data.put(objekt);
	// } else {
	// javaxt.io.File xtFile = (javaxt.io.File) file;
	// if (!xtFile.getName().startsWith("dnevno_izvje")) {
	// objekt.put("fileName", xtFile.getName());
	// objekt.put("fileExtension", xtFile.getExtension());
	// objekt.put("fileType", "file");
	// res_data.put(objekt);
	// }
	//
	// }
	//
	// }
	// prepareJSONresponse(resp, res_data, new JSONArray(), null);
	// } else {
	// prepareERRORresponse(resp, "Tražena datoteka ne postoji!");
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// prepareERRORresponse(resp, e.getMessage());
	// }
	//
	// }
	/************* Stari način *************/



	// public List<String> listDatumiSjednicaFromFolder(javaxt.io.Directory
	// folder) {
	// System.out
	// .println("ROSKO listDatumiSjednica xxx1=" + folder.toString());
	// List<String> folderNames = new ArrayList<String>();
	// for (javaxt.io.Directory year : folder.getSubDirectories()) {
	// System.out.println("ROSKO xxx2=" + year.getName());
	// for (javaxt.io.Directory month : new javaxt.io.Directory(
	// year.getPath()).getSubDirectories()) {
	// for (javaxt.io.Directory day : new javaxt.io.Directory(
	// month.getPath()).getSubDirectories()) {
	// folderNames.add(day.getName() + "." + month.getName() + "."
	// + year.getName());
	//
	// }
	// }
	//
	// }
	// System.out.println("ROSKO xxx3=" + folderNames.toString());
	// return folderNames;
	//
	// }
	//



	// public List<String> listSjedniceFromDOCX(javaxt.io.Directory folder) {
	// System.out.println("ROSKO listDatumFromDnevniRed xxx1="
	// + folder.toString());
	// List<String> folderNames = new ArrayList<String>();
	// for (javaxt.io.Directory year : folder.getSubDirectories()) {
	// for (javaxt.io.Directory month : year.getSubDirectories()) {
	// if (month.getName().equalsIgnoreCase("Dnevni red")) {
	// for (javaxt.io.File file : month.getFiles()) {
	// folderNames.add(getDatumFromDnevniRedFolder(file.getName()));
	// }
	// }
	// }
	// }
	// return folderNames;
	//
	// }

	// //////PRVO
	// POIFSFileSystem fs = null;
	// Document document = new Document();
	//
	// try {
	// System.out.println("Starting the test");
	// fs = new POIFSFileSystem(new FileInputStream(file.toFile()));
	//
	// HWPFDocument doc = new HWPFDocument(fs);
	// WordExtractor we = new WordExtractor(doc);
	//
	// // OutputStream file = new FileOutputStream(new
	// java.io.File("D:/test.pdf"));
	//
	// OutputStream out = response.getOutputStream();
	// PdfWriter writer = PdfWriter.getInstance(document, out);
	//
	// Range range = doc.getRange();
	// document.open();
	// writer.setPageEmpty(true);
	// document.newPage();
	// writer.setPageEmpty(true);
	//
	// String[] paragraphs = we.getParagraphText();
	// for (int i = 0; i < paragraphs.length; i++) {
	//
	// org.apache.poi.hwpf.usermodel.Paragraph pr = range.getParagraph(i);
	// // CharacterRun run = pr.getCharacterRun(i);
	// // run.setBold(true);
	// // run.setCapitalized(true);
	// // run.setItalic(true);
	// paragraphs[i] = paragraphs[i].replaceAll("\\cM?\r?\n", "");
	// System.out.println("Length:" + paragraphs[i].length());
	// System.out.println("Paragraph" + i + ": " + paragraphs[i].toString());
	//
	// // add the paragraph to the document
	// document.add(new Paragraph(paragraphs[i]));
	// }
	//
	// System.out.println("Document testing completed");
	// } catch (Exception e) {
	// System.out.println("Exception during test");
	// e.printStackTrace();
	// } finally {
	// // close the document
	// document.close();

	// ////////////DRUGO
	// WordExtractor extractor = null ;
	// try {
	//
	// FileInputStream fis=new FileInputStream(file.toFile());
	// HWPFDocument document=new HWPFDocument(fis);
	// extractor = new WordExtractor(document);
	// String [] fileData = extractor.getParagraphText();
	// String st="";
	// for(int i=0;i<fileData.length;i++){
	// if(fileData[i] != null)
	// st+=fileData[i]+" ";
	// }
	// Document doc=new Document();
	// response.setCharacterEncoding("UTF-8");
	// PdfWriter.getInstance(doc,response.getOutputStream());
	// doc.open();
	// doc.add(new Paragraph(st));
	// doc.close();
	// }
	// catch(Exception e){
	// prepareERRORresponse(response, e.getMessage());
	// }

	// }

}
