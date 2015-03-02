package hr.as2.inf.server.connections.as400;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2ConnectionException;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.file.AS2FileUtility;
import hr.as2.inf.common.format.AS2Format;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.connections.J2EEConnection;
import hr.as2.inf.server.connections.J2EEConnectionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400JPing;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CharConverter;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.DataQueue;
import com.ibm.as400.access.DataQueueEntry;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileInputStream;
import com.ibm.as400.access.IFSFileOutputStream;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.SystemValue;
import com.ibm.as400.access.Trace;

/**
 * zrosko@gmail.com
 */
// ?to solve multiple queue connection channales
public final class AS2ConnectionAS400 extends J2EEConnection {
    protected AS400 _connection;
    private int _SERVICETYPE;
    private String _DATAQIN;
    private String _DATAQOUT;
    private DataQueue _dqIn = null;
    private DataQueue _dqOut = null;
    public AS2ConnectionAS400(J2EEConnectionManager aConnectionManager, String host, String user, String password,
int serviceType) throws AS2Exception {
        _connectionManager = aConnectionManager;
        _HOST = host;
        _USER = user;
        _PASSWORD = password;
        _SERVICETYPE = serviceType;
        connect();
    }
    public AS2ConnectionAS400(J2EEConnectionManager aConnectionManager, String host, String user, String password, int serviceType, String dataQin,
            String dataQout) throws AS2Exception {
        _connectionManager = aConnectionManager;
        _HOST = host;
        _USER = user;
        _PASSWORD = password;
        _SERVICETYPE = serviceType;
        _DATAQIN = dataQin;
        _DATAQOUT = dataQout;
        connect();
    }
    public void commit() throws AS2ConnectionException {
        return;
    }
    public void connect() throws AS2ConnectionException {
        try {
            _connection = new AS400(_HOST, _USER, _PASSWORD);
            _connection.setGuiAvailable(false);
            _connection.connectService(_SERVICETYPE);
            // if _SERVICESTYPE == DQ // do logic for other services
            if (_dqIn!=null){
                _dqIn = new DataQueue(_connection, _DATAQIN);
                _dqOut = new DataQueue(_connection, _DATAQOUT);
            }else
                return;
            // if _dqIn.exists()
            // if _dqOut.exists()
            // if _dqIn.isFIFO
            // if _dqOut.isFIFO
            // --------------------------------------------
            try {
                CommandCall cc = new CommandCall(_connection);
                String bsDeviceId = "TODO";
                // BSAApplicationContext.getInstance().getDeviceID();
                String bsDatabaseLetter = "TODO";
                // BSAApplicationContext.getInstance().getDataBase().substring(2,
                // 3);
                cc.setCommand("SBMJOB CMD(CALL PGM(BS" + bsDatabaseLetter + "1/BS" + bsDatabaseLetter + "Z1BSJ) PARM('" + bsDeviceId + "')) JOB("
                        + bsDeviceId + ")");
                if (cc.run()) {
                    // String job_num =
                    // cc.getMessageList(0).toString().substring(36, 42);
                    // String job_user =
                    // cc.getMessageList(0).toString().substring(43, 47);
                    // String job_name =
                    // cc.getMessageList(0).toString().substring(48, 55);
                    // izbaceno fiksno parsiranje koje je radilo samo za duzinu
                    // username-a od 4
//                    String str = cc.getMessageList(0).toString().substring(36);
//                    str = str.split(" ")[0];
//                    String str1[] = str.split("/");
//                    String job_num = str1[0];
//                    String job_user = str1[1];
//                    String job_name = str1[2];
                    //Job jb = new Job(_connection, job_name, job_user, job_num);
                    //System.out.println("Job " + job_num + "/" + job_user + "/" + job_name + " pokrenut.");
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // --------------------------------------------
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, this, e.toString());
            AS2ConnectionException ex = new AS2ConnectionException("289");
            ex.addCauseException(e);
            throw ex;
        }
    }
    /**
     * Close the AS400 connection.
     */
    public void disconnect() throws AS2ConnectionException {
        try {
            if (_connection != null) {
                if (_dqIn!=null){
                    byte[] bcall = new byte[90];
                    int offset = 0;
                    offset = AS2Format.copyStringToByteArray("END SESSION", bcall, offset, 19);
                    offset = AS2Format.copyStringToByteArray("", bcall, offset, 50);
                    send(bcall);
                    closeSendQueue();
                    closeReceiveQueue();
                }
                _connection.disconnectAllServices(); // (_SERVICETYPE);
            }
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, this, e.toString());
            AS2ConnectionException exc = new AS2ConnectionException("282");
            exc.addCauseException(e);
            throw exc;
        }
        _dqIn = null;
        _dqOut = null;
        _connection = null;
        _valid = false;
    }
    /**
     * This method returns the AS400Connection held on to by this connection
     * object
     */
    public AS400 getAS400Connection() {
        return _connection;
    }

    public boolean ping() {
        String ping = _connectionManager.getPingCommand();
        if (ping == null)
            return true;
        else if (_connection != null) {
            try {
                AS2Trace.trace(AS2Trace.I, "Ping command = " + ping);
                AS400JPing pingObj = new AS400JPing(_HOST, _SERVICETYPE, false);
                pingObj.setTimeout(5000); // 5 seconds
                if (pingObj.ping())
                    _valid = true;
                else
                    _valid = false;
            } catch (Exception e) {
                _valid = false;
                AS2Trace.trace(AS2Trace.W, e, "Ping failed.");
            }
        }
        return _valid;
    }
    /**
     * This sends rollback to the AS400 Connection
     */
    public void rollback() throws AS2ConnectionException {
        // _dqIn.clear();
        // _dqOut.clear();
        // ? no need to do it twice
        return;
    }
    public String toString() {
        return (super.toString() + "\n Connection	 	" + _connection + "\n SERVICETYPE 	" + _SERVICETYPE + "\n DATAQIN 		" + _DATAQIN
                + "\n DATAQOUT 		" + _DATAQOUT + "\n");
    }
    public byte[] send(byte[] valueObject) throws AS2ConnectionException {
        return send(valueObject, "");
    }
    public byte[] send(byte[] valueObject, String program) throws AS2ConnectionException {
        AS2Trace.trace(AS2Trace.I, "Sending to DataQ Program= " + program + " >>>>>>" + new String(valueObject));
        // if (program.length()>0)
        // {
        // int offset = 0;
        // offset = J2EEFormat.copyStringToByteArray("CALL", valueObject,
        // offset, 19);
        // offset = J2EEFormat.copyStringToByteArray(program, valueObject,
        // offset, 50);
        // }
        try {
            if (_dqIn.exists())
                _dqIn.clear();
            if (_dqOut.exists())
                _dqOut.clear();
            String tmps = new String(valueObject);
            System.out.println("sent:     " + tmps);
            valueObject = new CharConverter().stringToByteArray(tmps);
            _dqIn.write(valueObject); // send to Q
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e, "Failure: Could not put message to " + _DATAQIN);
            try {
                closeSendQueue();
                throw e;
            } catch (Exception ex) {
                AS2Trace.trace(AS2Trace.E, ex, "Failure: Could not close queue " + _DATAQIN);
                AS2ConnectionException exc = new AS2ConnectionException("286");
                exc.addCauseException(ex);
                throw exc;
            }
        }
        AS2Trace.trace(AS2Trace.I, this, "Sent message successfuly to " + _DATAQIN);
        // start retrieving message from AS400
        byte[] ret;
        try {
            ret = receive();
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e, "Failure: Could not receive message response " + _DATAQOUT);
            AS2ConnectionException rex = new AS2ConnectionException("287");
            rex.addCauseException(e);
            throw rex;
        }
        return ret;
    }
    public byte[] receive() throws AS2ConnectionException {
        DataQueueEntry replayMessage = null;
        byte[] data = null;
        try {
            if (_dqOut == null) {
                AS2Trace.trace(AS2Trace.E, "Receive queue " + _DATAQOUT + " not opened");
                return null;
            } // else do {
            replayMessage = _dqOut.read(-1); // wait till something found
            // replayMessage = _dqOut.read(10);
            // } while(replayMessage == null);
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, "Could not get message from " + _DATAQOUT + " queue");
            try {
                closeReceiveQueue();
                AS2ConnectionException ex = new AS2ConnectionException("289");
                ex.setErrorDetails(e.toString());
                throw ex;
            } catch (Exception ex) {
                AS2Trace.trace(AS2Trace.E, "Could not close " + _DATAQOUT + " queue");
                AS2ConnectionException exc = new AS2ConnectionException("289");
                exc.setErrorDetails(ex.toString());
                throw exc;
            }
        }
        try {
            data = replayMessage.getData();
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e, "Message was corrupted");
            try {
                closeReceiveQueue();
                return null;
            } catch (Exception ex) {
                AS2Trace.trace(AS2Trace.E, "Could not close " + _DATAQOUT + " queue");
                return null;
            }
        }
        AS2Trace.trace(AS2Trace.E, this, "Get response message successfuly from " + _DATAQOUT);
        String tmps = new CharConverter().byteArrayToString(data);
        data = tmps.getBytes();
        System.out.println("received: " + tmps);
        return data;
    }
    private void closeSendQueue() throws Exception {
        if (_dqIn != null) {
            _dqIn.clear();
            _dqIn = null;
        }
    }
    private void closeReceiveQueue() throws Exception {
        if (_dqOut != null) {
            _dqOut.clear();
            _dqOut = null;
        }
    }
    public void copyFileFromAS400ToLocalHost(String from_name, String to_name) throws AS2ConnectionException {
        System.err.println("000" + from_name + " " + to_name);
        // if(_connection == null || !_connection.isConnected(AS400.FILE)) // ??
        // implicit
        // throw new J2EEConnectionException("291");
        System.err.println("0001");
        if (from_name == null || to_name == null) {
            AS2ConnectionException ex = new AS2ConnectionException("292");
            ex.setTechnicalErrorDescription("From: " + from_name + " To: " + to_name);
            throw ex;
        }
        System.err.println("0002");
        IFSFile from_file = new IFSFile(_connection, from_name);
        File to_file = new File(to_name);
        System.err.println("0003");
        try {
            // Make sure source file exists
            if (!from_file.exists())
                throw new AS2ConnectionException("293");
            // Is it file of direstory
            if (!from_file.isFile())
                throw new AS2ConnectionException("294");
            // Is it readable
            if (!from_file.canRead())
                throw new AS2ConnectionException("295");
            System.err.println("0004");
            // Is it direstory
            if (to_file.isDirectory())
                to_file = new File(to_file, from_file.getName());
            if (to_file.exists()) {
                if (!to_file.canWrite())
                    throw new AS2ConnectionException("296");
            } else {
                System.err.println("0005");
                // If file doesn't exist, check if directory exists and is
                // writable.
                // If getParent() returns null, then the directory is the
                // current dir.
                String parent = to_file.getParent();
                if (parent == null)
                    parent = AS2FileUtility.DEFAULT_DIRECTORY;//System.getProperty("user.dir");
                File dir = new File(parent);
                if (!dir.exists())
                    throw new AS2ConnectionException("297");
                if (dir.isFile())
                    throw new AS2ConnectionException("297");
                if (!dir.canWrite())
                    throw new AS2ConnectionException("296");
            }
            /**** Now do it *****/
            IFSFileInputStream from = null;
            FileOutputStream to = null;
            System.err.println("0006");
            try {
                from = new IFSFileInputStream(_connection, from_name, IFSFileInputStream.SHARE_ALL);
                System.err.println("0007");
                to = new FileOutputStream(to_file);
                byte[] buffer = new byte[1024 * 64];
                System.err.println("0008");
                // While there is data in the source file copy the data from
                // the source file to the target file.
                int bytesRead;
                System.err.println("0009");
                while ((bytesRead = from.read(buffer)) != -1)
                    to.write(buffer, 0, bytesRead);
                System.err.println("0010");
            } catch (Exception e) {
                System.err.println("EEE" + e);
            }
            // Clean up by closing the source and target files.
            finally {
                System.err.println("0011");
                if (from != null)
                    try {
                        from.close();
                    } catch (Exception e) {
                        ;
                    }
                if (to != null)
                    try {
                        to.close();
                    } catch (Exception e) {
                        ;
                    }
            }
        } catch (Exception e) {
            System.err.println("EEE" + e);
        }
    }
    public AS2AS400Dokument readAS400File(String fileName) throws AS2ConnectionException {
        System.err.println("readAS400File fileName=" + fileName); //$NON-NLS-1$
        IFSFileInputStream in = null;
        IFSFile file = null;
        byte[] result = null;
        if (fileName == null)
            throw new AS2ConnectionException("292"); //$NON-NLS-1$
        file = new IFSFile(_connection, fileName);
        try {
            if (!file.exists())
                throw new AS2ConnectionException("293"); //$NON-NLS-1$
            if (!file.isFile())
                throw new AS2ConnectionException("294"); //$NON-NLS-1$
            if (!file.canRead())
                throw new AS2ConnectionException("295"); //$NON-NLS-1$
            // It is a file, exists and it is readable => we can read it:
            in = new IFSFileInputStream(file);
            result = new byte[in.available()];
            in.read(result);
        } catch (AS400SecurityException e) {
            throw new AS2ConnectionException("295"); //$NON-NLS-1$
        } catch (IOException e) {
            throw new AS2ConnectionException("295"); //$NON-NLS-1$
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                AS2Trace.trace(Trace.ERROR, "Error closing file:" + fileName + "\n" + e); //$NON-NLS-1$
            }
        }
        AS2AS400Dokument resultVo = new AS2AS400Dokument();
        resultVo.setFileName(fileName);
        resultVo.setContent(result);
        return resultVo;
    }
    public void writeToAS400File(String fileName, byte[] content) throws AS2ConnectionException {
        System.err.println("writeToAS400File fileName=" + fileName); //$NON-NLS-1$
        IFSFileOutputStream out = null;
        IFSFile file = null;
        if (fileName == null)
            throw new AS2ConnectionException("292"); //$NON-NLS-1$
        file = new IFSFile(_connection, fileName);
        try {
            if (file.exists())
                throw new AS2ConnectionException(); // TODO: Koji broj (dodati
                                                     // novu poruku u
                                                     // J2EEDefaultResourceBundle?)
            // It is a file, exists and it is readable => we can read it:
            out = new IFSFileOutputStream(file);
            out.write(content);
        } catch (AS400SecurityException e) {
            throw new AS2ConnectionException("295"); //$NON-NLS-1$
        } catch (IOException e) {
            throw new AS2ConnectionException("295"); //$NON-NLS-1$
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                AS2Trace.trace(Trace.ERROR, "Error closing file:" + fileName + "\n" + e); //$NON-NLS-1$
            }
        }
    }
    public AS2RecordList listFilesOnAS400(String directoryName) throws AS2ConnectionException {
        System.err.println("listFilesOnAS400 directoryName=" + directoryName);
        AS2RecordList rs = new AS2RecordList();
        try {
            // Create the IFSFile object for the directory.
            IFSFile directory = new IFSFile(_connection, directoryName);
            // Generate the list of name. Pass the list method the
            // directory filter object and the search match criteria.
            //
            // Note - this example does the processing in the filter
            // object. An alternative is to process the list after
            // it is returned from the list method call.
            String[] directoryNames = directory.list(new AS2DirectoryFilter(), "*");
            System.err.println("listFilesOnAS400 002");
            // Tell the user if the directory doesn't exist or is empty
            if (directoryNames == null)
                new AS2ConnectionException("221");
            else if (directoryNames.length == 0)
                new AS2ConnectionException("222");
            else {
                System.err.println("listFilesOnAS400 003");
                rs.addColumnName("FileName");
                for (int i = 0; i < directoryNames.length; i++) {
                    AS2Record row = new AS2Record();
                    row.set("FILE", directoryName + "/" + directoryNames[i]);
                    rs.addRow(row);
                    System.err.println("listFilesOnAS400 004" + row);
                }
            }
        } catch (Exception e) {
            AS2ConnectionException ex = new AS2ConnectionException("223");
            ex.addCauseException(e);
            throw ex;
        }
        // rs.setShowColumnNames(false);
        // System.err.println("listFilesOnAS400 RS=" + rs);
        return rs;
    }
    public String getAS400SystemTimeAsHHMMSS() {
        // QTIME eg. 10:11:45
        try {
            if (_connection != null) {
                SystemValue sv = new SystemValue(_connection, "QTIME");
                return sv.getValue().toString();
            } else {
                AS2Trace.trace(AS2Trace.E, "Can not retrieve AS400 system time (no connection to AS400) ");
                return null;
            }
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e, "Can not retrieve AS400 system time");
            throw new AS2Exception("???");
        }
    }
    public String getAS400SystemDateAsYYYYMMDD() {
        // QDATE eg. 2004-12-02
        try {
            if (_connection != null) {
                SystemValue sv = new SystemValue(_connection, "QDATE");
                return sv.getValue().toString();
            } else {
                AS2Trace.trace(AS2Trace.E, "Can not retrieve AS400 system time (no connection to AS400) ");
                return null;
            }
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e, "Can not retrieve AS400 system date");
            throw new AS2Exception("???");
        }
    }
    public Date getAS400SystemDateAndTime() {
        Date date = null;
        java.sql.Date sDate = null;
        java.sql.Time sTime = null;
        try {
            if (_connection != null) {
                Calendar dateCal = new GregorianCalendar();
                Calendar timeCal = new GregorianCalendar();
                SystemValue sysDate = new SystemValue(_connection, "QDATE");
                sDate = (java.sql.Date) sysDate.getValue();
                SystemValue sysTime = new SystemValue(_connection, "QTIME");
                sTime = (java.sql.Time) sysTime.getValue();
                timeCal.setTime(sTime);
                dateCal.setTime(sDate);
                dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                dateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
                date = dateCal.getTime();
            } else {
                AS2Trace.trace(AS2Trace.E, "Can not retrieve AS400 system date&time (no connection to AS400) ");
                return null;
            }
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e, "Can not retrieve AS400 system date&time");
            throw new AS2Exception("???");
        }
        return date;
    }
 
}