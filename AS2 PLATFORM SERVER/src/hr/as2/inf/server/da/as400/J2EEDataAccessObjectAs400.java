package hr.as2.inf.server.da.as400;

import hr.as2.inf.common.core.AS2Constants;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordInterface;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.data.as400.AS2AS400SpoolFileRs;
import hr.as2.inf.common.data.as400.AS2AS400SpoolFileVo;
import hr.as2.inf.common.exceptions.AS2ConnectionException;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.format.AS2Format;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.connections.as400.AS2ConnectionAS400;
import hr.as2.inf.server.da.J2EEDataAccessObject;
import hr.as2.inf.server.da.datasources.J2EEDefaultAS400Service;
import hr.as2.inf.server.da.metadata.J2EEMetaDataService;

import java.io.InputStreamReader;
import java.util.Enumeration;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.SpooledFileList;
import com.ibm.as400.data.ProgramCallDocument;

/**
 * @author: zrosko@yahoo.com
 */
public class J2EEDataAccessObjectAs400 extends J2EEDataAccessObject implements AS2Constants
{
	//private Hashtable pcmlStatementCache=new Hashtable();
	//Create one data access object for each package_path

	private static final String _CALL = "CALL"; //$NON-NLS-1$

	private static final String OFFSET = "offset"; //$NON-NLS-1$

	private static final String MESSAGE = "message"; //$NON-NLS-1$

	private String PACKAGE_PATH = "hr.banksoft.bsa.pcml"; //$NON-NLS-1$

	//	private String programName;
	//private ProgramCallDocument pcmlDoc;
	//protected boolean callOK =false;
	public AS2Record daoExecute(AS2Record values, String pgmName, String inPgmStruct,
			String outPgmStruct) throws AS2DataAccessException
	{
		AS2Trace.trace(AS2Trace.I, pgmName + " daoExecute begin"); //$NON-NLS-1$
		AS2ConnectionAS400 co = null;
		AS2Record j2eevo = null;
		AS2RecordList j2eers = null;
		AS2Record header = new AS2Record();

		try
		{
			co = getConnection();
			J2EEMetaDataService metaDataService = startTransaction(values, pgmName, inPgmStruct, co);

			byte[] outputData = null;
			outputData = co.receive();
			if (outputData.length < 1)
			{
				// TODO GRESKA
				System.out.println("greska u duzini: "); //$NON-NLS-1$
				//THROW EXCEPTION !!!
				throw new AS2DataAccessException("COMMUNICATION ERROR - daoExecute"); //$NON-NLS-1$
			}

			int offset = 0;
			header = metaDataService.getHeaderFromPcmlResultSet(outputData);
			offset = header.getAsInt(OFFSET);
			String header_message = header.get(MESSAGE);

			//get result set from byte[]??process return codes
			if (header_message.equals(COM_OK_END)
					|| values.getAsBooleanOrFalse(header_message))
			{
				j2eers = metaDataService.getJ2EEResultSetFromPcmlResultSet(outputData,
						outPgmStruct, offset);
			}
			else
			{
				// greska
				j2eevo = new AS2Record();
				j2eevo.setSuccess(false);
				j2eevo.set(AS2RecordInterface._TRANS_MESSAGE, header_message);
				System.out.println("header_message:" + header_message); //$NON-NLS-1$
			}
			//?? one row
			if (j2eers != null)
			{
				j2eevo = (AS2Record) j2eers.getRows().get(0);
				j2eevo.setSuccess(true);
				j2eevo.set(AS2RecordInterface._TRANS_MESSAGE, header_message);
			}
			else if (j2eevo == null)
			{
				j2eevo = new AS2Record();
				j2eevo.setSuccess(true);
				j2eevo.set(AS2RecordInterface._TRANS_MESSAGE, header_message);				
			}
			AS2Trace.trace(AS2Trace.I, pgmName + " daoExecute end"); //$NON-NLS-1$

			return j2eevo;
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e);
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	public AS2RecordList daoExecuteDQuery(AS2Record values, String pgmName,
			String inPgmStruct, String outPgmStruct) throws AS2DataAccessException
	{
		AS2Trace.trace(AS2Trace.I, pgmName + " daoExecuteDQuery begin"); //$NON-NLS-1$
		AS2ConnectionAS400 co = null;
		AS2RecordList j2eers = new AS2RecordList();

		try
		{
			co = getConnection();
			J2EEMetaDataService metaDataService = startTransaction(values, pgmName, inPgmStruct, co);
			//prepare byte[] using meta data (structure), find fields position,
			// data type, length, etc.

			byte[] outputData = null;
			outputData = co.receive();
			if (outputData.length < 1)
			{
				// GRESKA
				System.out.println("greska u duzini"); //$NON-NLS-1$
				//TODO THROW EXCEPTION !!! treba baciti J2EE grešku koje
				// Zdravko raspodjeljuje
				throw new AS2DataAccessException("COMMUNICATION ERROR - daoExecuteDQuery - header");
			}

			AS2Record header = new AS2Record();
			String header_message = null;
			do
			{
				int offset = 0;
				header = metaDataService.getHeaderFromPcmlResultSet(outputData);
				offset = header.getAsInt(OFFSET);
				header_message = header.get(MESSAGE);
				if (header_message.startsWith("00") //$NON-NLS-1$
						|| values.getAsBooleanOrFalse(header_message))
				{
					//parsiraj pristigle podatke
					AS2RecordList tmpRS = metaDataService.getJ2EEResultSetFromPcmlResultSet(
							outputData, outPgmStruct, offset);
					if (tmpRS != null)
					{
						j2eers.addRows(tmpRS);
					}
				}
				if (header_message.equals(COM_OK_CONT))
				{
					//dohvati nove podatke iz queue-a
					outputData = co.receive();
					if (outputData.length < 1)
					{
						//GRESKA
						System.out.println("greska u duzini"); //$NON-NLS-1$
						//THROW EXCEPTION !!!
						throw new AS2DataAccessException(
								"COMMUNICATION ERROR - daoExecuteDQuery - data");
					}
				}
			} while (header_message.equals(COM_OK_CONT));

			if (header_message.equals(COM_OK_END)
					|| values.getAsBoolean(header_message, true))
			{
				j2eers.setSuccess(true);
			}
			else
			{
				//TODO greska
				j2eers.setSuccess(false);
				System.out.println("header_message:" + header_message); //$NON-NLS-1$
			}
			j2eers.set(AS2RecordInterface._TRANS_MESSAGE, header_message);			
			AS2Trace.trace(AS2Trace.I, pgmName + " daoExecuteDQuery end"); //$NON-NLS-1$
			return j2eers;
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e);
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	/**
	 * The business object finder.
	 */
	public AS2RecordList callServerPgm(AS2Record values, String pgmName)
			throws AS2DataAccessException
	{
		AS2Trace.trace(AS2Trace.I, pgmName + " callServerPgm begin"); //$NON-NLS-1$
		AS2ConnectionAS400 con = null;
		AS2RecordList mmrs = null;
		ProgramCallDocument pcmlDoc = null;
		boolean callOK = false;
		String msgId, msgText; // msgs returned from server

		try
		{
			con = getConnection();
			AS400 ascon = con.getAS400Connection();
			String fullPathPgmName = PACKAGE_PATH + "." + pgmName; //$NON-NLS-1$
			pcmlDoc = new ProgramCallDocument(ascon, fullPathPgmName);

			//pcmlDoc.setValue(programName + paramName, paramValue);
			//pcmlDoc.setValue(pgmName + paramName, indices, paramValue);
			callOK = pcmlDoc.callProgram(pgmName);
			if (!callOK)
			{
				// Retrieve list of AS/400 messages
				AS400Message[] serverMsgs = pcmlDoc.getMessageList(pgmName);
				// Iterate through messages and write them to standard output
				for (int m = 0; m < serverMsgs.length; m++)
				{
					msgId = serverMsgs[m].getID();
					msgText = serverMsgs[m].getText();
					System.out.println("    " + msgId + " - " + msgText); //$NON-NLS-1$//$NON-NLS-2$
				}
				System.out.println("\n** Poziv " + pgmName //$NON-NLS-1$
						+ " nije uspio. Pogledati poruke iznad **"); //$NON-NLS-1$
				//exception ovdje
			}
			//String temp = (String) pcmlDoc.getValue(programName + paramName);

			AS2Trace.trace(AS2Trace.I, pgmName + " callServerPgm end"); //$NON-NLS-1$
			return mmrs;
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e);
			AS2DataAccessException ex = new AS2DataAccessException("151");
			ex.addCauseException(e);
			throw ex;
		}
	}

	/**
	 * Created by: Zeljko Bubicic <br>
	 * Created on: ??? <br>
	 * <br>
	 * Description: <br>
	 * <br>
	 * @param bcc
	 * @return
	 * @throws AS2DataAccessException <br>
	 *         Changed by: CB {BS} - izbacene HC baze i naziv joba - sad se
	 *         citaju iz Application Contexta
	 */
	/* method comment version 0.01 */
	public AS2Record callBatchPgm(AS2Record values) throws AS2DataAccessException
	{
		AS2Trace.trace(AS2Trace.I, values.getAsString("pgmName") + " callBatchPgm begin"); //$NON-NLS-1$//$NON-NLS-2$
		//        J2EETrace.trace(J2EETrace.I, "callBatchPgm begin"); //$NON-NLS-1$

		AS2Record vo = new AS2Record();
		vo.setSuccess(false);
		try
		{
			AS400 asconn = getConnection().getAS400Connection();
			CommandCall cc = new CommandCall(asconn);

			String dbLetter = "TODO";//BSAApplicationContext.getInstance().getDataBase().substring(2, 3);
			String command = "SBMJOB CMD(CALL PGM(BS" + dbLetter + "1/BS" + dbLetter //$NON-NLS-1$//$NON-NLS-2$
					+ "IME PGM TODO" + ") PARM('" //$NON-NLS-1$
					+ "PARAM TODO" + "')) JOB(" //$NON-NLS-1$
					+ "TODO" + "BC)"; //$NON-NLS-1$
			System.out.println(command);
			cc.setCommand(command);
			if (cc.run())
			{
				String job_name = null;
				String job_user = null;
				String job_num = null;
				try
				{
					String str = cc.getMessageList(0).toString().substring(36);
					str = str.split(" ")[0]; //$NON-NLS-1$
					String str1[] = str.split("/"); //$NON-NLS-1$
					job_num = str1[0];
					job_user = str1[1];
					job_name = str1[2];
				}
				catch (ArrayIndexOutOfBoundsException e1)
				{
					// TODO {IKB}Auto-generated catch block
					/* catch block body code version 0.01 */
					e1.printStackTrace();
				}

				Job jb = new Job(asconn, job_name, job_user, job_num);

				System.out.println("Job " + job_num + "/" + job_user + "/" + job_name //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
						+ " pokrenut."); //$NON-NLS-1$

				vo.set(JOB_NAME, jb.getName());
				vo.set(JOB_NUMBER, jb.getNumber());
				vo.set(JOB_USER, jb.getUser());
				vo.setSuccess(true);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vo;
	}

	public AS2ConnectionAS400 getConnection()
	{
		try
		{
			return (AS2ConnectionAS400) J2EEDefaultAS400Service.getInstance().getConnection();
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e);
		}
		return null;
	}

	public J2EEMetaDataService getMetaDataService()
	{
		try
		{
			return J2EEDefaultAS400Service.getInstance().getMetaData();
		}
		catch (Exception e)
		{
			AS2Trace.trace(AS2Trace.E, e);
		}
		return null;
	}

	public String getPackagePath()
	{
		return PACKAGE_PATH;
	}

	private J2EEMetaDataService startTransaction(AS2Record values, String pgmName,
			String inPgmStruct, AS2ConnectionAS400 co) throws AS2ConnectionException
	{
		byte[] call = new byte[71];
		byte[] response = null;
		int offset = 0;

		offset = AS2Format.copyStringToByteArrayPadWithNull(_CALL, call, offset, 19);
		offset = AS2Format.copyStringToByteArrayPadWithNull(pgmName, call, offset, 50);
		response = co.send(call);
		int len = AS2Format.getIntFromByteArray(response, 0, 4);
		if (len != call.length)
		{
			//TODO greska
			System.out.println("greska u duzini: " + len); //$NON-NLS-1$
		}
		//prepare byte[] using meta data (structure), find fields position,
		// data type, length, etc.
		J2EEMetaDataService metaDataService = getMetaDataService();
		byte[] inputData = metaDataService.prepareRequest(values, inPgmStruct);
		response = co.send(inputData);
		len = AS2Format.getIntFromByteArray(response, 0, 4);
		if (len != inputData.length)
		{
			//TODO greska
			System.out.println("greska u duzini: " + len); //$NON-NLS-1$
		}
		return metaDataService;
	}

	public void setPackagePath(java.lang.String newPackagePath)	{
		PACKAGE_PATH = newPackagePath;
	}
    public AS2AS400SpoolFileVo readSpoolFile(AS2AS400SpoolFileVo vo){
    	AS2ConnectionAS400 co = null;
        try {
        	co = getConnection();
        	AS400 sys = co.getAS400Connection();
            /********************************************************
             * Set up a print parameter list that specifies what
             * a spooled file should be transformed into.
             * Use QWPDEFAULT ("text only") to get a text stream
             ********************************************************/
            PrintParameterList prtParm = new PrintParameterList();
            prtParm.setParameter(PrintObject.ATTR_MFGTYPE,"*WSCST" );
            prtParm.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT,
                                 "/QSYS.LIB/QWPDEFAULT.WSCST");            
             /*******************************************************
             *  Retrieve the spooled file's transformed input
             ********************************************************/            
            //TODO 1 String systemName = "srvbsa";
            String splfName = vo.getAsString("spfile_name");
            int splfNumber = vo.getAsInt("spfile_num");
            String jobName = vo.getAsString("job_name");
            String jobUser = vo.getAsString("job_user");
            String jobNumber = vo.getAsString("job_number");
            /*
            String systemName = "srvbsa";
            String splfName = "PBSAA1046B";
            int splfNumber = 3;
            String jobName = "OPK_110919";
            String jobUser = "JBGD02";
            String jobNumber = "438994";
            */            
            //TODO 1 AS400 sys = new AS400(systemName, "jbjava", "x2c3st");
            SpooledFile splf = new SpooledFile( sys,          // AS400
                                                splfName,     // splf name
                                                splfNumber,   // splf number
                                                jobName,      // job name
                                                jobUser,      // job user
                                                jobNumber );  // job number

            InputStreamReader in = new
            InputStreamReader(splf.getTransformedInputStream(prtParm), "852");
            char[] buf = new char[32767];
            StringBuffer sbuf = new StringBuffer();            
			if (in.ready()) {
				int bytesRead = 0;
				bytesRead = in.read(buf, 0, buf.length);
				while (bytesRead > 0) {
					sbuf.append(buf, 0, bytesRead);
					bytesRead = in.read(buf, 0, buf.length);
				}
			}
            in.close();
            /********************************************************
             *  Print the data to the screen and file
             ********************************************************/
//            String text = "line 1\n\nli     ne 3\n\nline\n\rline 5"; 
//            String adjusted = text.replaceAll("(?m)^[ \t]*\r?\n", "");
//            String s = text.replaceAll("(?m)^\\s*$[\n\r]{1,}", ""); 
//            System.out.println(text);
//            System.out.println(adjusted);
//            System.out.println(s);
            String output = sbuf.toString();
//          String adjusted1 = output.replaceAll("(?m)^[ \t]*\r?\n", "");//uklanja prazne redove
//          adjusted1=adjusted1.replaceAll("(?m)^[ \t]*r?\f", "");//uklanja oznake novog lista
//			output = output.replaceAll("(?m)^[ \t]*\r?\n", "");//uklanja prazne redove
//			output = output.replaceAll("(?m)^[ \t]*r?\f", "");//uklanja oznake novog lista
            output = output.replaceAll("\n","\r\n"); //zamijeni novi list sa novim redom
            output = output.replaceAll("\r\f","\r\n"); //zamijeni novi list sa novim redom
            vo.setSpfileTekst(output);
        }catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
        return vo;
    }
	public AS2AS400SpoolFileRs getSpooledFileList(AS2AS400SpoolFileVo value)throws AS2Exception {
		AS2AS400SpoolFileVo vo = new AS2AS400SpoolFileVo();
		AS2AS400SpoolFileRs rs = new AS2AS400SpoolFileRs();

    	AS2ConnectionAS400 co = null;
        try {
        	if(!value.exists("datum_od") || !value.exists("datum_do")){
        		return new AS2AS400SpoolFileRs();
        	}
        	co = getConnection();
        	AS400 system_ = co.getAS400Connection();
			String strSpooledFileName;
			Integer strSpooledFileNumber;
			SpooledFileList splfList = new SpooledFileList(system_);
			// spool file filter
			if(value.exists("QueueFilter")){
				splfList.setQueueFilter(value.get("QueueFilter"));
	
			}else {
				splfList.setQueueFilter((String)
					J2EEDefaultAS400Service.getInstance()._serverConfiguration.get("hr.adriacomsoftware.inf.server.connection.as400.J2EEConnectionManagerAS400.DATAQ"));
			}
			//splfList.setQueueFilter("/QSYS.LIB/QUSRSYS.LIB/PRT01.OUTQ");
			splfList.setStartDateFilter(AS2AS400SpoolFileVo.getDateFormatNormalToCYYMMDD(value.getDatumOd()));
			splfList.setEndDateFilter(AS2AS400SpoolFileVo.getDateFormatNormalToCYYMMDD(value.getDatumDo()));
			if (value.getKorisnik().length() > 0)
				splfList.setUserFilter(value.getKorisnik());
			else
				splfList.setUserFilter("*ALL");
			if (value.getSpoolFileNaziv().length() > 0)
				splfList.setUserDataFilter(value.getSpoolFileNaziv().toUpperCase());//zr.31.3.2014. upperCase
			splfList.openSynchronously();
			Enumeration<?> enum1 = splfList.getObjects();
			//int count = 0;
			while (enum1.hasMoreElements() /* && count < 6 */) {
				//count++;
				SpooledFile splf = (SpooledFile) enum1.nextElement();
				if (splf != null) {
					// output this spooled file's name
					strSpooledFileName = splf.getStringAttribute(SpooledFile.ATTR_SPOOLFILE);
					strSpooledFileNumber = splf.getIntegerAttribute(SpooledFile.ATTR_SPLFNUM);
					/*
					 * System.out.println("spooled_file_name : " +
					 * strSpooledFileName +"    job_splf_number: "+
					 * strSpooledFileNumber.toString() +"    job_user: "+
					 * splf.getStringAttribute(SpooledFile.ATTR_JOBUSER)
					 * +"    job_name: "+
					 * splf.getStringAttribute(SpooledFile.ATTR_JOBNAME)
					 * +"    job_number: "+
					 * splf.getStringAttribute(SpooledFile.ATTR_JOBNUMBER) //
					 * +"    job_number: "+
					 * splf.getStringAttribute(SpooledFile.ATTR_SPOOL) //
					 * +"    job_number: "+
					 * splf.getStringAttribute(SpooledFile.ATTR_SPLF_SIZE) //
					 * +"    job_number: "+
					 * splf.getStringAttribute(SpooledFile.ATTR_SPLFNUM) //
					 * +"    job_number: "+
					 * splf.getStringAttribute(SpooledFile.ATTR_SPLF_CREATOR)
					 * +"    pages: "+
					 * splf.getIntegerAttribute(SpooledFile.ATTR_PAGES)
					 * +"    size: "+
					 * splf.getIntegerAttribute(SpooledFile.ATTR_SPLF_SIZE)
					 * +"    date_created: "+ splf.getCreateDate()
					 * +"    time_created: "+ splf.getCreateTime()
					 * //+"    size: "+
					 * splf.getIntegerAttribute(SpooledFile.ATTR_MULTI_ITEM_REPLY
					 * ) //+"    size: "+
					 * splf.getIntegerAttribute(SpooledFile.ATTR_MULTIUP) );
					 */
					vo.set("spfile_name", strSpooledFileName);
					vo.set("spfile_num", strSpooledFileNumber.toString());
					vo.set("job_user",splf.getStringAttribute(SpooledFile.ATTR_JOBUSER));
					vo.set("job_name",splf.getStringAttribute(SpooledFile.ATTR_JOBNAME));
					vo.set("job_number",splf.getStringAttribute(SpooledFile.ATTR_JOBNUMBER));
					vo.set("pages",splf.getIntegerAttribute(SpooledFile.ATTR_PAGES));
					vo.set("size",splf.getIntegerAttribute(SpooledFile.ATTR_SPLF_SIZE)+ " B");
					vo.set("date_created", splf.getCreateDate());
					vo.set("time_created", splf.getCreateTime());
					vo.set("date_created_", AS2AS400SpoolFileVo.getDateFormatCYYMMDDToNormal(vo.get("date_created")));
					vo.set("time_created_", AS2AS400SpoolFileVo.getTimeFormatNormal(vo.get("time_created")));
					rs.addRow(vo);
					vo = new AS2AS400SpoolFileVo();
				}
			}
			// clean up after we are done with the list
			splfList.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	/*
	 * Test.
	 */
	public static void main(String[] args)	{
	}

}