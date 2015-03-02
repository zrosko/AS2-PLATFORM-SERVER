package hr.as2.inf.server.as400.facade;

import hr.as2.inf.common.as400.facade.AS2AS400Facade;
import hr.as2.inf.common.data.as400.AS2AS400SpoolFileRs;
import hr.as2.inf.common.data.as400.AS2AS400SpoolFileVo;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.validations.AS2ValidatorService;
import hr.as2.inf.server.as400.da.jdbc.AS2AS400JobSpfileJdbc;
import hr.as2.inf.server.as400.da.jdbc.AS2AS400JobSpfileRecordJdbc;
import hr.as2.inf.server.da.as400.J2EEDataAccessObjectAs400;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.ibm.as400.access.AS400;
import com.ibm.as400.vaccess.DataQueueDocument;

public final class AS2AS400FacadeServer implements AS2AS400Facade {

	private static AS2AS400FacadeServer _instance = null;

	public static AS2AS400FacadeServer getInstance() {
		if (_instance == null) {
			_instance = new AS2AS400FacadeServer();
		}
		return _instance;
	}

	private AS2AS400FacadeServer() {
	}

	public AS2AS400SpoolFileRs getSpooledFileList(AS2AS400SpoolFileVo value)throws AS2Exception {
		J2EEDataAccessObjectAs400 dao_as400 = new J2EEDataAccessObjectAs400();
		AS2AS400SpoolFileRs rs = new AS2AS400SpoolFileRs();
		value.set("QueueFilter","/QSYS.LIB/QUSRSYS.LIB/PRT01.OUTQ");
		rs.appendRowsOnly(dao_as400.getSpooledFileList(value));
		value.set("QueueFilter","/QSYS.LIB/QUSRSYS.LIB/PRT01BKP.OUTQ");
		rs.appendRowsOnly(dao_as400.getSpooledFileList(value));
		value.set("QueueFilter","/QSYS.LIB/QGPL.LIB/ONDPROC.OUTQ");
		rs.appendRowsOnly(dao_as400.getSpooledFileList(value));
		return rs;		
	}

	public AS2AS400SpoolFileRs procitajSveJobove(AS2AS400SpoolFileVo value)throws AS2Exception {
		return null;
	}

	public AS2AS400SpoolFileVo citajJob(AS2AS400SpoolFileVo value) throws AS2Exception {
		return null;
	}

	public AS2AS400SpoolFileVo azurirajJob(AS2AS400SpoolFileVo value) throws AS2Exception {
		return value;
	}

	public AS2AS400SpoolFileVo dodajJob(AS2AS400SpoolFileVo value) throws AS2Exception {
		return value;
	}

	public AS2AS400SpoolFileVo brisiJob(AS2AS400SpoolFileVo value) throws AS2Exception {
		return value;
	}
	/* PRIMJER
 		http://www.javapractices.com/topic/TopicAction.do?Id=87
	 */
	public AS2AS400SpoolFileVo citajJobSpoolFile(AS2AS400SpoolFileVo value)throws Exception {
		//IZ BAZE  @@SOURCE = SQL
		//SA AS400 @@SOURCE = AS400
		String _source = value.get("@@SOURCE");
		if(_source.equals("SQL")){
			AS2AS400JobSpfileJdbc dao_jdbc = new AS2AS400JobSpfileJdbc();
			value = dao_jdbc.daoLoad(value);
		}else if(_source.equals("AS400")){	
			//TODO
			if(value.getAsInt("pages")>350){
        		throw new AS2Exception("661");
        	}
			J2EEDataAccessObjectAs400 dao_as400 = new J2EEDataAccessObjectAs400();
			value =  dao_as400.readSpoolFile(value);
			String date_created = value.get("date_created");
			String time_created = value.get("time_created");
			value.setCalendarAsDateString(value.getAsCalendar("date_created_"),"date_created");
			//value.delete("date_created");
			value.delete("time_created");
			//value.setPropertyCalendarAsDateString(value.getPropertyAsCalendar("time_created_"),"time_created");
			AS2AS400JobSpfileJdbc dao_jdbc = new AS2AS400JobSpfileJdbc();
			if(!dao_jdbc.daoFindIfExists(value)){
				//TODO da li se radi prepis provjeri u as400_job
				dao_jdbc.daoCreate(value);
				String spfile_id = dao_jdbc.daoFindLastInserted(value);
				//TODO parse records
				if(value.get("job_name").equals("BSJXX1L8BC") 
						&& value.get("spfile_name").equals("PBSAD1049B")){
			        String s = value.getSpfileTekst(); 
			        String[] tokens = s.split("--------------------------------------------------------------------------------------------------------------------");  
			        for (int i=0; i < tokens.length; i++) {  
			        	AS2AS400JobSpfileRecordJdbc dao_record = new AS2AS400JobSpfileRecordJdbc();
			        	AS2AS400SpoolFileVo spfile_record = new AS2AS400SpoolFileVo(value);
			        	spfile_record.set("tekst",tokens[i].trim());
			        	spfile_record.set("spfile_id",spfile_id);
			        	if(spfile_record.get("tekst").length()>27 && AS2ValidatorService.validateNumberNoSpace(spfile_record.get("tekst").substring(16,26).trim())){
			        		spfile_record.set("broj_partije",spfile_record.get("tekst").substring(16,26).trim());
			        		dao_record.daoCreate(spfile_record);
			        	}
			        }
				}else if(value.get("job_name").equals("BSJXX1L8BC") 
						&& value.get("spfile_name").equals("PBSAD1049B")){
					
				}else if(value.get("job_name").equals("BSJXX1L8BC") 
						&& value.get("spfile_name").equals("PBSAD1049B")){
					
				}
			}
			value.set("date_created", date_created);
			value.set("time_created", time_created);
		}
		return value;
	}

	public AS2AS400SpoolFileRs citajJobSpoolFileRecords(AS2AS400SpoolFileVo value)throws Exception {
		return null;
	}

	public void readQ() throws AS2Exception {
		// Set up the document and the JTextField.
		AS400 system = new AS400("192.168.0.252", "jbjava", "x2c3st");
		// AS400 system = new AS400 ("MySystem", "Userid", "Password");
		DataQueueDocument document = new DataQueueDocument(system,
				"/QSYS.LIB/MYLIB.LIB/MYDATAQ.DTAQ");
		// DataQueueDocument document = new DataQueueDocument (system,
		// "/QSYS.LIB/%ALL%.LIB/%PRT01%.OUTQ");
		JTextField textField = new JTextField(document, "", 50);
		// System.out.println(textField.getText());
		// Add the JTextField to a frame.
		JFrame frame = new JFrame("My Window");
		frame.getContentPane().add(new JScrollPane(textField));
		// Read the next entry from the data queue.
		document.read();
	}
}
