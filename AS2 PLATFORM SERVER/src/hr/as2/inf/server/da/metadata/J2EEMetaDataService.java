package hr.as2.inf.server.da.metadata;

import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.file.AS2FileResource;
import hr.as2.inf.common.format.AS2Format;
import hr.as2.inf.common.logging.AS2Trace;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

public class J2EEMetaDataService implements J2EEPcmlConstants {
	public static HashMap _STRUCTURES = new HashMap();
	public static HashMap _PROGRAMS = new HashMap();
	private BufferedReader _FILE;
	private String _NEWLINE = null;

	public J2EEMetaDataService(String file) throws AS2DataAccessException {
		try {
			// J2EEResource config = new
			// J2EEResource(J2EEContext.getPropertiesPath() + file);
			InputStream is = AS2Helper.readResourceToStreamAsURL(file);
			InputStreamReader ir = new InputStreamReader(is);
			_FILE = new BufferedReader(ir);// new
											// RandomAccessFile(config.getAbsolutePath(),
											// "r");
			initialize();
		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, e, "Can not read service catalog "
					+ file);
			AS2DataAccessException fme = new AS2DataAccessException("157");
			fme.addCauseException(e);
			throw fme;
		}
	}
	private void doData(AS2Record pgmOrStructure) throws AS2DataAccessException	{

		try	{
			J2EEPcmlDataTag data;
			String token = null;
			String name = null;
			String value = null;

			while (_NEWLINE != null){

				if (_NEWLINE.startsWith("<data")){
					data = new J2EEPcmlDataTag();
					StringTokenizer st = new StringTokenizer(_NEWLINE, "= ");
					int count = 0;
					int countName = 0;

					while (st.hasMoreTokens())
					{
						token = st.nextToken();
						count++;

						if (token.equals(_NAME)
							|| token.equals(_TYPE)
							|| token.equals(_LENGTH)
							|| token.equals(_STRUCT)
							|| token.equals(_INIT))
						{
							name = token;
							countName = count;
							value = null;
						}
						else
						{
							if (name != null && (countName == (count - 1)))
							{
								value = token;
								int start = value.indexOf('"');
								int end = value.lastIndexOf('"');
								value = value.substring(start + 1, end);
								if (name != null && value != null)
								{
									data.set(name, value);
									name = null;
									value = null;
								}
							}
							else
							{
								name = null;
								value = null;
								continue;
							}
						}

					}
					if (pgmOrStructure instanceof J2EEPcmlProgramTag)
					{
						((J2EEPcmlProgramTag) pgmOrStructure).addData(data);
					}
					else if (pgmOrStructure instanceof J2EEPcmlStructureTag)
					{
						((J2EEPcmlStructureTag) pgmOrStructure).addData(data);
					}

				}
				_NEWLINE = readNextLine();

				if (_NEWLINE.startsWith("<!--"))
				{
					_NEWLINE = readNextLine();
					continue;
				}
				else if (!_NEWLINE.startsWith("<data"))
				{
					_NEWLINE = readNextLine();
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			AS2Trace.trace(AS2Trace.E, e, "While parsing data structure"); //$NON-NLS-1$
			AS2DataAccessException fme = new AS2DataAccessException("159");
			fme.setTechnicalErrorDescription("While parsing data structure");
			fme.addCauseException(e);
			throw fme;
		}
	}
	private void doProgram() throws AS2DataAccessException
	{

		try
		{
			J2EEPcmlProgramTag pgm = new J2EEPcmlProgramTag();
			String token = null;
			String name = null;
			String value = null;
			boolean done = false;

			while (_NEWLINE != null && !done)
			{

				if (_NEWLINE.startsWith("<program"))
				{
					done = true;

					StringTokenizer st = new StringTokenizer(_NEWLINE, "= ");
					int count = 0;
					int countName = 0;

					while (st.hasMoreTokens())
					{
						token = st.nextToken();
						count++;

						if (token.equals(_NAME) || token.equals(_PATH))
						{
							name = token;
							countName = count;
							value = null;
						}
						else
						{
							if (name != null && (countName == (count - 1)))
							{
								value = token;
								int start = value.indexOf('"');
								int end = value.lastIndexOf('"');
								value = value.substring(start + 1, end);
								if (name != null && value != null)
								{
									pgm.set(name, value);
									name = null;
									value = null;
								}
							}
							else
							{
								name = null;
								value = null;
								continue;
							}
						}

					}
				}
				_NEWLINE = readNextLine();

				if (_NEWLINE.startsWith("<!--"))
				{
					_NEWLINE = readNextLine();
					continue;
				}
				else if (_NEWLINE.startsWith("<pcml"))
				{
					_NEWLINE = readNextLine();
					break;
				}
				else if (_NEWLINE.startsWith("<data"))
				{
					doData(pgm);
					continue;
					//}else if(_NEWLINE.startsWith("<struct")) {
					//doStructure();
					//continue;
				}
				else if (_NEWLINE.startsWith("</pcml"))
				{
					_NEWLINE = readNextLine();
					break;
				}
				else if (_NEWLINE.startsWith("</program"))
				{
					_NEWLINE = readNextLine();
					break;
					//}else if(_NEWLINE.startsWith("</struct")) {
					//_NEWLINE = readNextLine();	
					//break;
					//}
				}
			}
			//calculate total program input/output structure length
			int totalLength = 0;
			Enumeration E = pgm.getData().elements();
			while (E.hasMoreElements())
			{
				J2EEPcmlDataTag metaDataProgram = (J2EEPcmlDataTag) E.nextElement();
				if (metaDataProgram.getType().equals(_STRUCT))
				{
					J2EEPcmlStructureTag structureParameters =
						(J2EEPcmlStructureTag) _STRUCTURES.get(metaDataProgram.getStruct());
					totalLength = totalLength + structureParameters.getTotalLength();
				}
				else
				{
					totalLength = totalLength + metaDataProgram.getLength() + 1;
				}
			}
			pgm.setTotalLength(totalLength);
//			System.out.println("totalLength: " + totalLength + " <program> " + pgm.getName());
			_PROGRAMS.put(pgm.getName(), pgm);

		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e, "While rading pcml file " + _FILE);
			AS2DataAccessException fme = new AS2DataAccessException("159");
			fme.setTechnicalErrorDescription("Pcml file name is: " + _FILE);
			fme.addCauseException(e);
			throw fme;
		}
	}
	private void doStructure() throws AS2DataAccessException
	{

		try
		{
			J2EEPcmlStructureTag structure = new J2EEPcmlStructureTag();
			String token = null;
			String name = null;
			String value = null;
			boolean done = false;

			while (_NEWLINE != null && !done)
			{

				if (_NEWLINE.startsWith("<struct"))
				{
					done = true;

					StringTokenizer st = new StringTokenizer(_NEWLINE, "= ");
					int count = 0;
					int countName = 0;

					while (st.hasMoreTokens())
					{
						token = st.nextToken();
						count++;

						if (token.equals(_NAME))
						{
							name = token;
							countName = count;
							value = null;
						}
						else
						{
							if (name != null && (countName == (count - 1)))
							{
								value = token;
								int start = value.indexOf('"');
								int end = value.lastIndexOf('"');
								value = value.substring(start + 1, end);
								if (name != null && value != null)
								{
									structure.set(name, value);
									name = null;
									value = null;
								}
							}
							else
							{
								name = null;
								value = null;
								continue;
							}
						}

					}
				}
				_NEWLINE = readNextLine();

				if (_NEWLINE.startsWith("<!--"))
				{
					_NEWLINE = readNextLine();
					continue;
				}
				else if (_NEWLINE.startsWith("<pcml"))
				{
					_NEWLINE = readNextLine();
					break;
				}
				else if (_NEWLINE.startsWith("<data"))
				{
					doData(structure);
					continue;
				}
				else if (_NEWLINE.startsWith("</pcml"))
				{
					_NEWLINE = readNextLine();
					break;
					//}else if(_NEWLINE.startsWith("</program")) {
					//_NEWLINE = readNextLine();	
					//break;
				}
				else if (_NEWLINE.startsWith("</struct"))
				{
					_NEWLINE = readNextLine();
					break;
				}
			}
			structure.calculateTotalLength();
			_STRUCTURES.put(structure.getName(), structure);

		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e, "While reading pcml file " + _FILE);
			AS2DataAccessException fme = new AS2DataAccessException("159");
			fme.setTechnicalErrorDescription("Pcml file name is: " + _FILE);
			fme.addCauseException(e);
			throw fme;
		}
	}	
	public AS2Record getHeaderFromPcmlResultSet(byte[] rsByteArrayFromHost)	{
		/* method body code version 0.01 */
		int arraylength = rsByteArrayFromHost.length;
		int columnCount = 0; //used for meta data description of J2EEResultSet	
		int OFFSET = 0;

		//J2EEResultSet resultSet = new J2EEResultSet();
		AS2Record valueObject = new AS2Record();
		J2EEPcmlStructureTag structureParameters = (J2EEPcmlStructureTag) _STRUCTURES.get("head");

		Enumeration ES = structureParameters.getData().elements();
		while (ES.hasMoreElements() && arraylength > OFFSET)
		{
			J2EEPcmlDataTag metaDataStructure = (J2EEPcmlDataTag) ES.nextElement();
			columnCount++;

			OFFSET =
				copyElementValueToValueObject(
					valueObject,
					metaDataStructure,
					rsByteArrayFromHost,
					columnCount,
					OFFSET);
		}
		valueObject.set("offset", OFFSET);
		return valueObject;
	}

	public AS2RecordList getJ2EEResultSetFromPcmlResultSet(
		byte[] rsByteArrayFromHost,
		String programName,
		int offset)
	{
		int arraylength = rsByteArrayFromHost.length;
		int OFFSET = offset;

		AS2RecordList returnRS = null;
		J2EEPcmlProgramTag programParameters = (J2EEPcmlProgramTag) _PROGRAMS.get(programName);

		int arraySize = programParameters.getTotalLength();
		if (arraylength - OFFSET != 0)
		{
			returnRS = new AS2RecordList();
			do
			{
				int columnCount = 0; //used for meta data description of J2EEResultSet	
				//J2EEResultSet resultSet = new J2EEResultSet();
				AS2Record valueObject = new AS2Record();
				Enumeration EP = programParameters.getData().elements();
				while (EP.hasMoreElements())
				{
					J2EEPcmlDataTag metaDataProgram = (J2EEPcmlDataTag) EP.nextElement();

					if (metaDataProgram.getType().equals(_STRUCT))
					{
						//int structureCount = metaData.getCount();//not used yet (more structures rows)
						J2EEPcmlStructureTag structureParameters =
							(J2EEPcmlStructureTag) _STRUCTURES.get(metaDataProgram.getStruct());
						Enumeration ES = structureParameters.getData().elements();
						while (ES.hasMoreElements())
						{
							J2EEPcmlDataTag metaDataStructure = (J2EEPcmlDataTag) ES.nextElement();
							columnCount++;

							OFFSET =
								copyElementValueToValueObject(
									valueObject,
									metaDataStructure,
									rsByteArrayFromHost,
									columnCount,
									OFFSET);
						}
					}
					else
					{ //primitive types
						columnCount++;
						OFFSET =
							copyElementValueToValueObject(
								valueObject,
								metaDataProgram,
								rsByteArrayFromHost,
								columnCount,
								OFFSET);
					}
				}
				returnRS.addRow(valueObject);
			}
			while (arraylength >= OFFSET + arraySize);
		}
		return returnRS;
	}

	private int copyElementValueToValueObject(
		AS2Record valueObject,
		J2EEPcmlDataTag metaData,
		byte[] rsByteArray,
		int columnCount,
		int OFFSET)
	{
		int metaDataLength = metaData.getLength();
		String metaDataName = metaData.getName();
		String valueFromByteArray =
			AS2Format.getStringFromByteArray(rsByteArray, OFFSET, metaDataLength);
		OFFSET = OFFSET + metaDataLength + 1;
		valueObject.set(metaDataName, valueFromByteArray);
		return OFFSET;
	}
	private void initialize() throws AS2DataAccessException
	{

		try
		{
			_NEWLINE = readNextLine();

			while (_NEWLINE != null)
			{

				if (_NEWLINE.startsWith("<program"))
				{
					doProgram();
				}
				else if (_NEWLINE.startsWith("<struct"))
				{
					doStructure();
				}
				else
					_NEWLINE = readNextLine();

			}
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e, "While reading pcml " + _FILE);
			AS2DataAccessException fme = new AS2DataAccessException("158");
			fme.addCauseException(e);
			throw fme;
		}
	}
	/**
	 * This method was created by a SmartGuide.
	 * @param args java.lang.String[]
	 */
	public static void main(String args[]) throws Exception
	{
		J2EEMetaDataService.convertFromIncludeToPcml("coo02.h");
		/*J2EEMetaDataService zz = new J2EEMetaDataService("bsa.pcml");
		
		J2EETrace.turnMethodTracingOn();
		J2EETrace.turnTracingOn();
		J2EETrace.setTraceLevel(6);
		
		J2EEValueObject ht = new J2EEValueObject();
		
		ht.set("bytesReturned","123=");
		ht.set("bytesAvailable", "123=");
		ht.set("userProfile", "123456789=");
		ht.set("previousSignonDate", "123456=");
		ht.set("previousSignonTime","12345=");
		ht.set("xyz", "=");
		ht.set("badSignonAttempts", "123=");
		ht.set("status", "123456789=");
		ht.set("passwordChangeDate","1234567=");
		ht.set("noPassword", "=");
		ht.set("passwordExpirationInterval", "123=");
		ht.set("datePasswordExpires", "1234567=");
		ht.set("daysUntilPasswordExpires","123=");
		ht.set("setPasswordToExpire", "=");
		ht.set("displaySignonInfo", "123456789=");
			
		ht.set("receiverLength","123=");
		ht.set("format", "1234567=");
		ht.set("profileName", "123456789=");
		ht.set("errorCode", "123=");
		
		
		byte [] ret = zz.prepareRequest(ht, "qsyrusri");
		System.out.println("Result:" + new String(ret));
		J2EEResultSet rs = zz.getJ2EEResultSetFromPcmlResultSet(ret,"qsyrusri");
		System.out.println("Result++" + rs);*/
		/*	byte [] ret = zz.prepareRequest(ht, "DXJ2EEP");
			System.out.println("First++" + new String(ret));
			System.out.println("Second ------------");
			byte [] ret1 = zz.prepareRequest(ht, "DXJ2EEP");
			System.out.println("Second++" + new String(ret1));
			byte [] ba = new byte[1000];
			J2EEFormat.copyStringToByteArray("XPP111  XFF222  XBBUUU333 XAAA9999XRRR888", ba, 
		
		0,300);
			zz.prepareReply(ba,ht,"DXJ2EEP");
			System.out.println("Return++" + ht);
			return;*/
	}
	private int prepareRequestStructure(
		AS2Record inputData,
		String structureName,
		byte[] ret,
		int OFFSET)
	{
		J2EEPcmlStructureTag structureParameters =
			(J2EEPcmlStructureTag) _STRUCTURES.get(structureName);
		Enumeration E = structureParameters.getData().elements();
		while (E.hasMoreElements())
		{
			J2EEPcmlDataTag metaDataStructure = (J2EEPcmlDataTag) E.nextElement();
			if (metaDataStructure.getType().equals(_STRUCT))
			{
				OFFSET =
					prepareRequestStructure(inputData, metaDataStructure.getStruct(), ret, OFFSET);
			}
			else
			{
				String valueInStructure = 
					inputData.getAsString(metaDataStructure.getName());
					// NOTE ako promijenim u getPropertyAsStringOrEmpty onda gubim funkcionalnost init TAGa u pcml-u
				if (valueInStructure == null)
					valueInStructure = metaDataStructure.getInit(); 
				OFFSET =
					AS2Format.copyStringToByteArray(
						valueInStructure,
						ret,
						OFFSET,
						metaDataStructure.getLength());
			}
		}
		return OFFSET;
	}
	public byte[] prepareRequest(AS2Record inputData, String programName)
	{

		J2EEPcmlProgramTag programParameters = (J2EEPcmlProgramTag) _PROGRAMS.get(programName);
		byte[] ret = new byte[programParameters.getTotalLength()];
		int OFFSET = 0;
		Enumeration EP = programParameters.getData().elements();
		while (EP.hasMoreElements())
		{
			J2EEPcmlDataTag metaDataProgram = (J2EEPcmlDataTag) EP.nextElement();

			if (metaDataProgram.getType().equals(_STRUCT))
			{
				OFFSET =
					prepareRequestStructure(inputData, metaDataProgram.getStruct(), ret, OFFSET);				
			}
			else
			{ //??other types CHAR, INT
				String value = 
					inputData.getAsString(metaDataProgram.getName());
					// NOTE ako promijenim u getPropertyAsStringOrEmpty onda gubim funkcionalnost init TAGa u pcml-u
				if (value == null)
					value = metaDataProgram.getInit();
				OFFSET =
					AS2Format.copyStringToByteArrayPadWithNull(
						value,
						ret,
						OFFSET,
						metaDataProgram.getLength());
			}

		}
		return ret;
	}
	private String readNextLine() throws AS2DataAccessException
	{

		try
		{
			_NEWLINE = _FILE.readLine();

			if (_NEWLINE != null)
				_NEWLINE = AS2Format.removeAllLeadingBlanksAndTabs(_NEWLINE);
			return _NEWLINE;
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e, "While reading PCML " + _FILE);
			AS2DataAccessException fme = new AS2DataAccessException("158");
			fme.addCauseException(e);
			throw fme;
		}
	}
	public static void convertFromIncludeToPcml(String inputFile)
	{
		String _READLINE = null;
		boolean longOrShortType = false;
		boolean decimalType = false;
		String type = null;
		String name = null;
		String length = null;
		
		String token;
		byte[] outrec = new byte[84-23];

		try 
		{
			AS2FileResource config = new AS2FileResource(inputFile);
			RandomAccessFile _FILE = new RandomAccessFile(config.getAbsolutePath(), "r");
			_READLINE = _FILE.readLine();
			if (_READLINE != null)
				_READLINE = AS2Format.removeAllLeadingBlanksAndTabs(_READLINE);

			while (_READLINE != null)
			{
				//toRead = false;
				if (_READLINE.length() == 0)
				{
				}
				else if (_READLINE.startsWith("{"))
				{
					//toRead = true;
				}
				else if (_READLINE.startsWith("}"))
				{
					//toRead = true;
				}
				else if (_READLINE.startsWith("/*"))
				{
					//toRead = true;
				}
				else if (_READLINE.startsWith("#"))
				{
					//toRead = true;
				}
				else if (_READLINE.startsWith("struct"))
				{
					//toRead = true;
				}
				else
				{
					_READLINE = _READLINE.substring(0, _READLINE.lastIndexOf(';'));
					StringTokenizer st = new StringTokenizer(_READLINE, "(,[] ");

					longOrShortType = false;
					decimalType = false;

					while (st.hasMoreTokens())
					{
						token = st.nextToken();

						if (token.equals("long") || token.equals("short"))
						{
							type = token;
							longOrShortType = true;
						}
						else if (token.equals("char"))
						{
							type = "char";
						}
						else if (token.equals("decimal"))
						{
							type = "decimal";
							decimalType = true;
						}
						else if (token.equals("byte"))
						{
							type = "byte";
						}
						else
						{
							if (longOrShortType || decimalType)
							{
								if (longOrShortType && length == null)
								{
									if (token.equals("int"))
									{
										if (type.equals("short"))
										{
											length = "4";
										}
										else
											length = "8";
										type = "int";
									}
									else if (type.equals("short"))
									{
										length = "2";
										name = token;
									}
								}
								else if (longOrShortType && length != null)
								{
									name = token;
								}

							}
							else
							{
								if (name == null)
									name = token;
								else if (length == null)
									length = (Integer.parseInt(token) - 1) + "";

							}
						}
					}
					AS2Format.copyStringToByteArray(
						"  <data name=\"" + name + "\"",
						outrec,
						0,
						30);
					AS2Format.copyStringToByteArray("type=\"" + type + "\"", outrec, 30, 14);
					AS2Format.copyStringToByteArray("length=\"" + length + "\"", outrec, 44, 14);
//					J2EEFormat.copyStringToByteArray("  usage=\"output\"/>", outrec, 58, 26);
					AS2Format.copyStringToByteArray(" />", outrec, 58, 3);

//					System.out.println(new String(outrec));
					type = null;
					name = null;
					length = null;

				}
				_READLINE = _FILE.readLine();
				if (_READLINE != null)
					_READLINE = AS2Format.removeAllLeadingBlanksAndTabs(_READLINE);

			}

		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e, "Problem while parsing data structure");
		}
	}
	public void readResourceFile (String file) throws AS2DataAccessException
	{
		try
		{
			//J2EEResource config = new J2EEResource(J2EEContext.getPropertiesPath() + file);
			InputStream is = AS2Helper.readResourceToStreamAsURL(file);
			InputStreamReader ir = new InputStreamReader(is);
			_FILE = new BufferedReader(ir);//new RandomAccessFile(config.getAbsolutePath(), "r");
			initialize();
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e, "Can not read service catalog " + file);
			AS2DataAccessException fme = new AS2DataAccessException("157");
			fme.addCauseException(e);
			throw fme;
		}
		
	}
}
