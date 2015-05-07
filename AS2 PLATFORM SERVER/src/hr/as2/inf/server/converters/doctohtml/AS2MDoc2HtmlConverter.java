package hr.as2.inf.server.converters.doctohtml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import javaxt.utils.Base64;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.model.SEPX;
import org.apache.poi.hwpf.model.SectionTable;
import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.ListEntry;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;

public class AS2MDoc2HtmlConverter //implements Extractor
{

    private static Map<Integer, Picture> _mapPictures;
    private HWPFDocument msWord;
    private static javaxt.io.File file;
    private HashMap<String,ByteArrayOutputStream> pictureByteArray;
//  private String pictureDirectory;

    /**
     *
     * @param input InputStream con el documento word
     * @throws IOException
     */
    public AS2MDoc2HtmlConverter(InputStream input) throws IOException {
        msWord = new HWPFDocument(input);
    }


    /**
     *
     * @param path
     * @param nombreArch
     * @throws IOException
     */
    public byte[] extractParagraphTexts(javaxt.io.File filee, HttpServletResponse response) throws IOException {
    	file=filee;
    	this.extractImagesIntoDirectory(file.getPath());

    	// Creamos un nuevo fichero con el nombre y ruta pasados como parĂˇmetros que contendrĂˇ el html final
//		final javaxt.io.File arch = new javaxt.io.File(file.getPath()+file.getName().substring(0,file.getName().indexOf(file.getExtension())-1)+".html");
//	    final OutputStream aSalida = new FileOutputStream(arch.toFile());

    	@SuppressWarnings("resource")
		ByteArrayOutputStream output = new ByteArrayOutputStream();
    	output.write(generateOutput(msWord).getBytes());
    	return output.toByteArray();
//	    aSalida.flush();
//		aSalida.close();
//		Timer timer = new Timer();
//		timer .schedule(new TimerTask() {
//			  @Override
//			  public void run() {
//				  for(Picture pic: _mapPictures.values()){
//					  if(new javaxt.io.File(pictureDirectory + File.separator + pic.suggestFullFileName()).exists())
//						  J2EEFile.deleteFile(pictureDirectory + File.separator + pic.suggestFullFileName());
//				  }
//	        		J2EEFile.deleteFile(arch.getPath()+arch.getName());
//			  }
//			},1000);//1000milis
//		1*60*1000);//2mins

    }


//	ByteArrayOutputStream bos = new ByteArrayOutputStream();
//	bos.write(generateOutput(msWord).getBytes());
//
//	this.extractImagesIntoDirectory(bos);
//    aSalida.write(generateOutput(msWord).getBytes());
////    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//    bos.write(generateOutput(msWord).getBytes());
//    arch.delete();
//    return bos.toByteArray();
	// Creamos un nuevo fichero con el nombre y ruta pasados como parĂˇmetros que contendrĂˇ el html final
//	final File arch = new File(path + nombreArch);
//	ByteArrayOutputStream bos = new ByteArrayOutputStream();
//	bos.write(generateOutput(msWord).getBytes());
//	return generateOutput(msWord).getBytes();
//	bos.write(generateOutput(msWord).getBytes(), 0, generateOutput(msWord).getBytes().length);

//    final OutputStream aSalida = new FileOutputStream(file);
    // Escribimos el texto extraido en el fichero html final
//	bos.write(generateOutput(msWord).getBytes());
//    response.setBufferSize(DEFAULT_BUFFER_SIZE);

//    try {
//		byte[] byteArray = arch.getBytes().toByteArray();
//		ServletOutputStream ouputStream = response
//				.getOutputStream();
//		ouputStream.write(byteArray, 0, byteArray.length);
//		ouputStream.flush();
//		ouputStream.close();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}



  /**
  *
  * @param directory local donde se guardan las imĂˇgenes
  * @throws IOException
  */
 public void extractImagesIntoDirectory(String directory) throws IOException {
 	_mapPictures = new HashMap<Integer, Picture>();
// 	  pictureDirectory = directory;
 	 pictureByteArray = new HashMap<String,ByteArrayOutputStream>();
     PicturesTable pTable = msWord.getPicturesTable();
     int numCharacterRuns = msWord.getRange().numCharacterRuns();
     for (int i = 0; i < numCharacterRuns; i++) {
         CharacterRun characterRun = msWord.getRange().getCharacterRun(i);
         if (pTable.hasPicture(characterRun)) {
             //System.out.println("have picture!");
             Picture pic = pTable.extractPicture(characterRun, false);
             //Picture pic = (Picture) picList.get(characterRun.getPicOffset());
//             String fileName = pic.suggestFullFileName();
//             String pictureFullFileName = directory+fileName;
//             pictureFileNames.add(pictureFullFileName);
             _mapPictures.put(characterRun.getStartOffset(), pic);
             //System.out.println(characterRun.getStartOffset() + "   "+ fileName);
//             javaxt.io.File imgFile  = new  javaxt.io.File(directory + File.separator + fileName);
//             ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
             ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//             OutputStream out = new FileOutputStream(imgFile.toFile());
             byteArray.write(pic.getContent());
//             pic.writeImageContent(out);
             pictureByteArray.put(pic.suggestFullFileName(),byteArray);
//             pictureByteArrays.put(pic, Base64.encodeBytes(outBytes.toByteArray()));
//             outBytes.flush();
//             outBytes.close();


//             ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
//             outBytes.write(imgFile.getBytes().toByteArray());
//             pic.writeImageContent(outBytes);



         }
     }
 }

    //STARO
//    /**
//     *
//     * @param directory local donde se guardan las imĂˇgenes
//     * @throws IOException
//     */
//    public void extractImagesIntoDirectory(String directory) throws IOException {
//    	_mapPictures = new HashMap<Integer, Picture>();
//    	  pictureDirectory = directory;
//        PicturesTable pTable = msWord.getPicturesTable();
//        int numCharacterRuns = msWord.getRange().numCharacterRuns();
//        for (int i = 0; i < numCharacterRuns; i++) {
//            CharacterRun characterRun = msWord.getRange().getCharacterRun(i);
//            if (pTable.hasPicture(characterRun)) {
//                //System.out.println("have picture!");
//                Picture pic = pTable.extractPicture(characterRun, false);
//                //Picture pic = (Picture) picList.get(characterRun.getPicOffset());
//                String fileName = pic.suggestFullFileName();
////                String pictureFullFileName = directory+fileName;
////                pictureFileNames.add(pictureFullFileName);
//                _mapPictures.put(characterRun.getStartOffset(), pic);
//                //System.out.println(characterRun.getStartOffset() + "   "+ fileName);
//                OutputStream out = new FileOutputStream(new File(directory + File.separator + fileName));
//                pic.writeImageContent(out);
//            }
//        }
//    }

    /**
     *
     * @param doc
     * @return
     * @throws IOException
     */
	private String generateOutput(HWPFDocument doc) throws IOException
	{
		StringBuffer output = new StringBuffer();

        StyleSheet styleSheet = doc.getStyleSheet();

        SectionTable _st = doc.getSectionTable();
        List<SEPX> _sections = _st.getSections();
        for (int index = 0; index < _sections.size(); index++) {
          int sectionIndex;
          try {
              sectionIndex = index;
          }
          catch (NumberFormatException exception) {
              sectionIndex = -1;
          }
          if (sectionIndex >= 0) {
              processSection(output, doc, sectionIndex, styleSheet);
          }
        }

        return escribirCabeceraHtml() + output.toString() + escribirPieHtml();
	}

    /**
     *
     * @param output
     * @param doc
     * @param sectionIndex
     * @param styleSheet
     * @throws IOException
     */
    private void processSection(StringBuffer output, HWPFDocument doc, int sectionIndex, StyleSheet styleSheet) throws IOException
	{
    	// default to NOT show deleted character runs unless showHidden=true
    	//boolean showHidden = ScriptUtils.getBoolean("showHidden", false, info);
    	boolean showHidden = true;

    	Range range = doc.getRange();
    	Section section = range.getSection(sectionIndex);

    	int numberOfParagraphs = section.numParagraphs();

    	boolean inList = false;
    	boolean listStart = true;
    	boolean inTable = false;
    	boolean rowStart = true;
    	Table _table = null;
    	TableRow _row = null;
    	int _rowIndex = 0;
		int _cellIndex = 0;

    	for (int paragraphIndex = 0; paragraphIndex < numberOfParagraphs; paragraphIndex++) {
    		Paragraph paragraph = null;
    		TableCell _cell = null;

    		try {
    			paragraph = section.getParagraph(paragraphIndex);
    		}
    		catch (Exception exception) {
    			System.out.println("Ignore paragraph exception: " + exception.toString());
    		}

    		if (paragraph != null) {

    			try {
    				Table table = range.getTable(paragraph);
    				//System.out.println("table: " + table);
    				if (table != null) {
    					System.out.println("table rows: " + table.numRows());
    				}
    			}
    			catch (IllegalArgumentException exception) { // not in paragraph
    			}

    			int styleIndex = paragraph.getStyleIndex();
    			StyleDescription paragraphStyle = styleSheet.getStyleDescription(styleIndex);
    			String styleName = paragraphStyle.getName();
    			//System.out.println("style index: " + styleIndex + "  style name: " + styleName);

    			boolean heading = (styleName.indexOf("Heading") == 0);

    			if (!heading && (paragraph instanceof ListEntry)) {
    				if (!inList) {
    					inList = true;
    					listStart = true;
    				} else {
    					listStart = false;
    				}

    				//System.out.println("paragraph is a list");
    			} else {
    				if (inList) {
    					output.append("</ul>");
    					inList = false;
    				}
    			}

    			if (paragraph.isInTable()) {
    				if (!inTable) {
    					_table = section.getTable(paragraph);
    					output.append("\n<table class='sample'>");
    					inTable = true;
    					rowStart = true;
    				}
    				if (rowStart) {
    					if(_table != null && _table.numRows() > 0){
	    					_row = _table.getRow(_rowIndex);
    					}
    					output.append("\n<tr>");
    					rowStart = false;
    				}
    				if (paragraph.isTableRowEnd()) {
    					_cellIndex = 0;
    					_rowIndex++;
    					output.append("\n</tr>");
    					rowStart = true;
    				} else {
    					if (!inList) {
    						if(_row != null){
    							_cell = _row.getCell(_cellIndex);
    							output.append("\n<td width = \"" + (((_cell.getWidth()/20)*16)/12) + "px\">");
    							if(_cellIndex+1 < _row.numCells()){
        							_cellIndex++;
        						}
    						}else{
    							output.append("\n<td>");
    						}

    					}
    				}
    			} else {
    				if (inTable) {
    					_rowIndex = 0;
    					output.append("</table>");
    				}
    				inTable = false;
    				rowStart = false;
    			}

    			if (inList) {
    				if (listStart) {
    					output.append("<ul>");
    					listStart = false;
    				}
    				output.append("<li>");
    			}

    			if (heading) {
    				int headingLevel = 6;
    				if (styleName.length() > 8) {
    					try {
    						headingLevel = Integer.parseInt(styleName.substring(8,9));
    					}
    					catch (NumberFormatException ignore) {
    					}
    				}
    				output.append("<h" + headingLevel + ">");
    				appendParagraphData(output, paragraph, styleSheet, styleIndex, showHidden);
    				output.append("</h" + headingLevel + ">");
    			} else {
    				appendParagraphData(output, paragraph, styleSheet, styleIndex, showHidden);
    			}

    			if (inList) {
    				output.append("</li>");
    			}

    			if (!inList && inTable && !paragraph.isTableRowEnd()) {
    				output.append("\n</td>");
    			}
    		}
    	}
	}

	/**
	 *
	 * @param output
	 * @param paragraph
	 * @param styleSheet
	 * @param styleIndex
	 * @param showHidden
	 * @return
	 * @throws IOException
	 */
	private StringBuffer appendParagraphData(StringBuffer output, Paragraph paragraph, StyleSheet styleSheet, int styleIndex, boolean showHidden) throws IOException
	{
		PicturesTable pTable = msWord.getPicturesTable();
		int numberOfRuns = paragraph.numCharacterRuns();
		output.append("\n<p " + devuelveEstilo(paragraph, 1) + ">"); // start paragraph
//		pictureFileNames = new ArrayList<String>();
		for (int runIndex = 0; runIndex < numberOfRuns; runIndex++) {
			CharacterRun run = paragraph.getCharacterRun(runIndex);
			if (pTable.hasPicture(run)) {
				Picture pic = pTable.extractPicture(run, false);
				String pictureFullFileName = pic.suggestFullFileName();
//				String pictureFileName = file.getPath()+pictureFullFileName;
//				pictureDirectorys
//				pictureFileNames.add(pictureFileName);
//				@SuppressWarnings("resource")
//				ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
//				javaxt.io.File img = new javaxt.io.File(pictureFileName);
//				try {
//					outBytes.write(img.getBytes().toByteArray());
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				extractImagesIntoDirectory
				ByteArrayOutputStream picByteArray = pictureByteArray.get(pic.suggestFullFileName());
				output.append(AS2MSWordStyle.pintaImagen(pic,file.getPath(),pictureFullFileName,pic.suggestFileExtension(),Base64.encodeBytes(picByteArray.toByteArray())));
				picByteArray.flush();
				picByteArray.close();



			}
			appendRunHtml(output, run, showHidden);
		}
		output.append("</p>"); // end paragraph
		return output;
	}

	/**
	 * AĂ±ade las etiquetas html
	 * @param output
	 * @param run
	 * @param showHidden
	 * @return
	 */
   	private StringBuffer appendRunHtml(StringBuffer output, CharacterRun run, boolean showHidden)
	{
        try {
            short ssIndex = run.getSubSuperScriptIndex();
            boolean isDeleted = run.isMarkedDeleted() || run.isFldVanished() || run.isStrikeThrough() || run.isDoubleStrikeThrough();
            if (run.isMarkedDeleted() || run.isFldVanished()) {
            	//System.out.println("isDeleted: " + isDeleted);
            }

            if ((!run.isFldVanished() || showHidden) && !run.text().trim().equals("")) {
                output.append(getFontString(run))
                      .append(run.isBold()               ? "<b>"    : "")
                      .append(run.isItalic()             ? "<i>"    : "")
                      .append(run.isHighlighted()        ? "<em>"   : "")
                      .append(isDeleted                  ? "<del>"  : "")
                      .append(run.getUnderlineCode() > 0 ? "<u>"    : "")
                      .append((ssIndex == 1)             ? "<sup>"  : "")
                      .append((ssIndex == 2)             ? "<sub>"  : "")
                      .append(run.text())
                      .append((ssIndex == 2)             ? "</sub>" : "")
                      .append((ssIndex == 1)             ? "</sup>" : "")
                      .append(run.getUnderlineCode() > 0 ? "</u>"   : "")
                      .append(isDeleted                  ? "</del>" : "")
                      .append(run.isHighlighted()        ? "</em>"  : "")
                      .append(run.isItalic()             ? "</i>"   : "")
                      .append(run.isBold()               ? "</b>"   : "")
                      .append("</font>")
                      ;
            }
        }
        catch (Exception exception) {
        	System.out.println("Ignore run exception: " + exception.toString());
            output.append("<small>Problem importing some text.</small>");
        }
        return output;
    }

   	/**
   	 *
   	 * @param run
   	 * @return
   	 */
	private String getFontString(CharacterRun run)
	{
        String font = run.getFontName();
        int size = getHtmlFontSize(run.getFontSize()); // run font is twice real font size
        int color = run.getIco24();
        // Si el color no es recogido bien se pone el negro por defecto.
        if (color < 0) {
        	color = 0;
        }
        //Hashtable t = HSSFColor.getIndexHash();
        //color=" + HSSFColor.RED.hexString + "
        String fontString = "<font face=\"" + font + "\" size=\"" + size + "\" color=\"#" + ico24ToHex(color) + "\">";// color=" + color + "
  //    log.debug("fontstr: " + fontString);

        return fontString;
    }

   	/**
   	 *  Map character run defined font size to HTML font size attribute
   	 *  - note input font size is twice the user specified font size (so it is an int I guess)
   	 *  - to determine mapping below, mimic word export to html
   	 *   pt size, 2*pt size html size
   	 *      6       12          1
   	 *      8       16          1
   	 *      10      20          2
   	 *      12      24          3
   	 *      14      28          4
   	 *      16      32          4
   	 *      18      36          5
   	 *      20      40          5
   	 *      24      48          6
   	 *      28      56          6
   	 *      32      64          7
   	 *      36      72          7
   	 */
   	private int getHtmlFontSize(int size)
   	{
        if      (size < 10) return 1;
        else if (size < 24) return 2;
        else if (size < 28) return 3;
        else if (size < 36) return 4;
        else if (size < 48) return 5;
        else if (size < 64) return 6;
        return 7;
   	}

   	/**
   	 *  Input a either a null string or a comma separated list of strings
   	 *  Return an array of strings
   	 *  If string is null, return an array of strings { "1", "2", ... }
   	 */
   	@SuppressWarnings("unused")
	private String[] getListFromCommaSeparatedString(String string, int defaultListSize)
   	{
   		//log.debug("Comma separated list: " + string);
   		if (string == null) {
   			string = "1";      // use 1-based index for users and convert to 0-based internally later
   			for (int i = 2; i <= defaultListSize; i++) {
   				string = string + ", " + i;
   			}
   		}
   		return string.split(",");
	}

   	private static String escribirCabeceraHtml(){
		return"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" +
					"<html>" +
					"<head>" +
						"<title>"+file.getName()+"</title>" +
						"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>" +
						"<style>" +
						"table.sample {" +
						"border-width: 1px 1px 1px 1px;" +
						"border-spacing: 0px;" +
						"border-style: outset outset outset outset;" +
						"border-color: gray gray gray gray;" +
						"border-collapse: separate;" +
						"//width: 100%;" +
						"}" +
						"table.sample th {" +
						"border-width: 1px 1px 1px 1px;" +
						"padding: 1px 1px 1px 1px;" +
						"border-style: inset inset inset inset;" +
						"border-color: gray gray gray gray;" +
						"background-color: white;" +
						"-moz-border-radius: 0px 0px 0px 0px;" +
						"}" +
						"table.sample td {" +
						"border-width: 1px 1px 1px 1px;" +
						"border-color: gray gray gray gray;" +
						"border-style: inset inset inset inset;" +
						"background-color: white;" +
						"-moz-border-radius: 0px 0px 0px 0px;" +
						"}" +
						"</style>" +
					"</head>" +
					"<body>";
	}

	private static String escribirPieHtml(){
		return "</body></html>";
	}

	/**
	 *
	 * @param text
	 * @return
	 */
	@SuppressWarnings("unused")
	private Object devolverTexto(String text) {
		if(text.toUpperCase().contains("HYPERLINK  \\L")){
			String link = "";
			int index = text.toUpperCase().indexOf("HYPERLINK  \\L") + 13;
			link = "<a href=\"#\">";
			return link;
		} else if (text.toUpperCase().contains("PAGEREF")) {
			return "</a>";
		}else{
			return text;
		}

	}

	/**
	 *
	 * @param ico24
	 * @return
	 */
	public static int[] ico24ToRGB(int ico24) {
		int[] rgb = new int[3];
		// If the colorRef is not "Auto", unpack the rgb values
		if (ico24 != -1) {
			rgb[0] = (ico24 >> 0) & 0xff;  // red;
			rgb[1] = (ico24 >> 8) & 0xff;  // green
			rgb[2] = (ico24 >> 16) & 0xff;  // blue
		}
		return rgb;
	}

	/**
	 *
	 * @param ico24
	 * @return
	 */
	public static String ico24AsHex(int ico24) {
		return Integer.toHexString(ico24);
	}

	/**
	 *
	 * @param ico24
	 * @return
	 */
	public static String ico24ToHex(int ico24) {
		int r = (ico24 >> 0) & 0xff;  // red;
		int g = (ico24 >> 8) & 0xff;  // green
		int b  = (ico24 >> 16) & 0xff;  // blue
		int rgb = (r << 16) | (g << 8) | b;
		// Find a better way to maintain leading zeroes
		return Integer.toHexString(0xC000000 | rgb).substring(1);
	}

	/**
	 *
	 * @param hexColor
	 * @return
	 */
	public static int hexToIco24(String hexColor) {
		if (hexColor == null || hexColor.length() != 6) {
			throw new IllegalArgumentException("hexColor must be 6 characters in length. Example: ffffff");
		}
		int r = Integer.parseInt(hexColor.substring(0, 2), 16);
		int g = Integer.parseInt(hexColor.substring(2, 4), 16);
		int b = Integer.parseInt(hexColor.substring(4, 6), 16);
		return rgbToIco24(r, g, b);
	}

	/**
	 *
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int rgbToIco24(int r, int g, int b)
	{
		return (0xff << 24) | (b << 16) | (g << 8) | r;
	}

	private static String devuelveEstilo(Paragraph p, int color){
    	String estilo = " style =\"";
    	//int fontAlignment = p.getEndOffset();
    	//int fontAlignment = p.getIndentFromLeft();
    	//int fontAlignment = p.getSpacingAfter();
    	//int fontAlignment = p.getSpacingBefore();
    	//int fontAlignment = p.getStartOffset();
    	int fontAlignment = p.getStyleIndex();

    	if(fontAlignment!=0){
    	//System.out.println(fontAlignment + "   " + new String(p.text()));
    	}

    	// Ponemos estilo de alineaciĂłn del parrafo.
    	estilo += getJustification(p);
    	// Ponemos las tabulaciones
    	estilo += getMarginLeft(p);
    	// Ponemos las tabulaciones
    	estilo += getFirstLineIndent(p);
    	// Ponemos es color de fondo
    	//estilo += getBackgroundColor(color);

    	estilo += "\"";
    	return estilo;
    }

	@SuppressWarnings("unused")
	private static String getBackgroundColor(int color) {
		return "background: #" + ico24ToHex(color) + ";";
	}
	private static String getFirstLineIndent(Paragraph p){

    	return "text-indent:" + p.getFirstLineIndent() / 10 + "px;";
    }

	private static String getMarginLeft(Paragraph p){
    	return "margin-left:" + p.getIndentFromLeft() / 10 + "px;";
    }

    private static String getJustification(Paragraph p){
    	int justification = p.getJustification();
    	//System.out.println(justification + "   " + new String(p.text()));
		switch (justification){
    		case 0 :
				return "text-align: left;";
			case 1 :
				return "text-align: center;";
			case 2 :
				return "text-align: right;";
			case 3 :
				return "text-align: justify;";
			default :
				return "text-align: left;";
		}
    }


	//@Override
	public void extractParagraphTexts(String path, String nombreArch)
			throws IOException {
		// TODO Auto-generated method stub

	}
}
