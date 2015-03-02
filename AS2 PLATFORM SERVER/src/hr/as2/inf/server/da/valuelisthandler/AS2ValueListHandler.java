package hr.as2.inf.server.da.valuelisthandler;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.common.data.AS2MetaData;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.data.valuelist.AS2ValueListInfo;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.security.user.AS2User;
import hr.as2.inf.common.session.AS2Session;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Properties;

/**
 * @version 1.0
 * @author zrosko@yahoo.com
 */
public final class AS2ValueListHandler {
    private Properties _properties;

    private static AS2ValueListHandler _instance = null;

    private AS2ValueListHandler() {
        AS2Context.setSingletonReference(this);
        _properties = AS2Helper.readPropertyFileAsURL(AS2Context
                .getPropertiesPath()
                + "/server/AS2ValueListHandler.properties");
    }

    public static AS2ValueListHandler getInstance() {
        if (_instance == null)
            _instance = new AS2ValueListHandler();
        return _instance;
    }
    public String getPropertyValue(String key){
    	return _properties.getProperty(key);
    }

    public AS2RecordList handleResultSet(ResultSet rs) {
        AS2RecordList as2_rs = new AS2RecordList();
        AS2User user = null;//TODO AS2SessionFactory.getInstance().getCurrentSession(new AS2Record());
	    AS2Session session = new AS2Session();//TODOAS2SessionFactory.getInstance().getCurrentSession();
	    if(!session.handleValueList())
	        return null;
        AS2Record service = null;//TODO AS2SessionFactory.getInstance().getCurrentRequest();
        AS2ValueListInfo info = (AS2ValueListInfo) user.getAsObject(AS2User._VALUE_LIST_INFO);
        if (info == null)
            info = new AS2ValueListInfo();
        //service has to be on the list to be handled
        String component_service = service.getComponentAndService();
        if (AS2ValueListHandler.getInstance()._properties.getProperty(component_service) == null)
            return null;

        int value_list_handler_row_size = info.getMax();
        if(value_list_handler_row_size==0)
        	value_list_handler_row_size = AS2Context.getInstance().VALUE_LIST_HANDLER_ROW_SIZE;
        int position = info.getPosition();//where we are now
        int size = info.getSize();//current size of the in memory result set
        int counter = 0;
        int cur_pos = 0;
        
        if (info.refreshAction()) {
            position = 1;
        } else if (info.forwardAction()) {
            //position = position;
        } else if (info.backwardAction()) {
            if(position > value_list_handler_row_size){
                position = position - size;
                position = position - value_list_handler_row_size;
            }else{
                position = 1;
            }
        }else{
            info.reset();
            position = info.getPosition();
            //size = info.getSize();
        }
        //handle result set here
        try {
            if (rs != null) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int numcols = rsmd.getColumnCount();
                for (int i = 1; i <= numcols; i++) {
                    AS2MetaData meta_data = new AS2MetaData(
                            rsmd.getColumnLabel(i).toLowerCase(),
                            rsmd.getColumnTypeName(i),
                            rsmd.getColumnType(i),
                            rsmd.getColumnDisplaySize(i),
                            rsmd.getPrecision(i),
                            rsmd.getScale(i),                            
                            i ); //sequence
                    as2_rs.getMetaData().put(rsmd.getColumnLabel(i).toLowerCase(),meta_data);
                    as2_rs.addColumnName(rsmd.getColumnLabel(i).toLowerCase());
                    as2_rs.setColumnSize(i, rsmd.getColumnDisplaySize(i));                    
                }
                //use info
                //rs.absolute(position); //not supported by ms sql server
                //info.setMax(rs.getFetchSize());//does not work
                info.setHasMoreResults(false);
                //end use info
                //cur_pos = 0;
                while (rs.next()) {
                    cur_pos++;
                    if(cur_pos<position)
                        continue;
                    if(counter==value_list_handler_row_size){
                        info.setHasMoreResults(true);
                        //break; does not count
                        continue;
                    }
                    //Hashtable row = new Hashtable(numcols);
                    AS2Record row = new AS2Record(numcols);
                    for (int i = 1; i <= numcols; i++) {
                        try {
                            Object obj = rs.getObject(i);
                            
                            if (obj != null) {
                                int _type = rsmd.getColumnType(i);

                                if (_type == java.sql.Types.CLOB) {
                                    Clob _clob = (Clob) obj;
                                    String _s_clob = _clob.getSubString(1,(int) _clob.length());
                                    row.set(rsmd.getColumnLabel(i).toLowerCase(), _s_clob);
                                } else {
                                    row.set(rsmd.getColumnLabel(i).toLowerCase(), obj.toString().trim());
                                }
                            }
                        } catch (Exception e) {
                            AS2Trace.trace(AS2Trace.I, e, "getSelectedValueList:column");
                        }
                    }
                    //j2eers.addRow(new J2EEValueObject(row));
                    as2_rs.addRow(row);
                    counter++;
                }
            }
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, e, "getSelectedValueList"); //$NON-NLS-1$
        }
        //end handling result set
        info.setPosition(position+counter);
        info.setSize(counter);  
        info.setMax(cur_pos);
        info.setTotalRows(cur_pos);
        user.set(AS2RecordList._VALUE_LIST_INFO, info);
        as2_rs.setValueLineHandlerUsed(true);//used by caching
        return as2_rs;
    }
}