/* (c) Adriacom Software d.o.o.
 * 22211 Vodice, Croatia
 * Created by Z.Rosko (zrosko@yahoo.com)
 * Date 2008.04.04 
 * Time: 13:33:18
 */
package hr.as2.inf.server.connections.as400;

import hr.as2.inf.common.data.AS2Record;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class AS2AS400Dokument extends AS2Record {
	private static final long serialVersionUID = 1L;
	private String DOCUMENT_NAME = "documentname"; 
	private String FILE_NAME = "filename";
	private String FILE_CONTENT = "filecontent"; 
	private String TYPE = "type";
	public static final String DOCUMENT_TYPE_TIFF = "tiff"; 
	public static final String DOCUMENT_TYPE_TIF = "tif"; 
	public static final String DOCUMENT_TYPE_GIF = "gif"; 
	public static final String DOCUMENT_TYPE_JPG = "jpg";
	public static final String DOCUMENT_TYPE_JPEG = "jpeg"; 
	public static final String DOCUMENT_TYPE_TXT = "txt"; 
	public static final String DOCUMENT_TYPE_HTML = "html"; 
	//	public static final String DOCUMENT_TYPE = ""; 
	// -------------------------------------------------------------------
	public AS2AS400Dokument()	{
		super();
	}

	public AS2AS400Dokument(LinkedHashMap<String, Object> properties)	{
		super(properties);
	}

	public AS2AS400Dokument(LinkedHashMap<String, Object> properties, ArrayList<String> order)	{
		super(properties);
	}

	public AS2AS400Dokument(AS2Record value)	{
		super(value);
	}

	public byte[] getContent()	{
		return (byte[]) getAsObject(FILE_CONTENT);
	}

	public String getFileName()	{
		return getAsString(FILE_NAME);
	}

	public void setContent(byte[] bs)	{
		set(FILE_CONTENT, bs);
	}

	public void setFileName(String string)	{
		set(FILE_NAME, string);
	}

	public String getType()	{
		return getAsStringOrBlank(TYPE);
	}

	public void setType(String string)	{
		set(TYPE, string);
	}

	public String getDocumentName()	{
		return getAsStringOrBlank(DOCUMENT_NAME);
	}

	public void setDocumentName(String string)	{
		set(DOCUMENT_NAME, string);
	}
}