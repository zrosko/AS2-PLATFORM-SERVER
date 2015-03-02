package hr.as2.inf.server.da.jdbc;

import hr.as2.inf.server.annotations.AS2DataAccessObject;
import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.datasources.J2EEJDBCService;

@AS2DataAccessObject
public class J2EEJdbc extends J2EEDataAccessObjectJdbc {

    public J2EEJdbc() {
        //setTableName("j2ee");
    }
    public J2EEConnectionJDBC getConnection() {
        try {
            return (J2EEConnectionJDBC) J2EEJDBCService.getInstance().getConnection();
        } catch (Exception e) {
            //TODO aspect J2EETrace.trace(J2EETrace.E, e);
        }
        return null;
    }    
}