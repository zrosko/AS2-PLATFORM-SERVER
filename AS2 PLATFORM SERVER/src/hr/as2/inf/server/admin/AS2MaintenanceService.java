package hr.as2.inf.server.admin;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.core.AS2Helper;
import hr.as2.inf.server.da.jdbc.J2EEDataAccessObjectJdbc;
import hr.as2.inf.server.transaction.AS2Transaction;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

public class AS2MaintenanceService implements Runnable {
    private static AS2MaintenanceService _instance = null;
    
    public static AS2MaintenanceService getInstance() {
        if (_instance == null)
            _instance = new AS2MaintenanceService();
        return _instance;
    }
    private AS2MaintenanceService() {
        try {
            Thread maintenance_thread = new Thread(this);
            maintenance_thread.setPriority(Thread.MIN_PRIORITY);
            maintenance_thread.start();
        } catch (Exception e) {
            System.out.println("AS2MaintenanceService default constructor exception - "+ e);
        }
    }

    public void run() {
        Thread t = Thread.currentThread();
        long startTime=0, endTime=0;
        
        while (true) {
            try {
                System.out.println("AS2MaintenanceService thread " + t.getName() + " started.");
                startTime = System.currentTimeMillis();
                String sql = "";
                Properties prop = AS2Helper.readPropertyFileAsURL(AS2Context.getPropertiesPath() + "/server/AS2MaintenanceService.properties");
                Enumeration<?> E = prop.propertyNames();
                while (E.hasMoreElements()) {
                    try{
	                    AS2Transaction.begin();
	                    sql = (String) prop.getProperty((String) E.nextElement());
	                    System.out.println("AS2MaintenanceService begin: "+ sql);
	                    J2EEDataAccessObjectJdbc dao = new J2EEDataAccessObjectJdbc();
	                    dao.daoExecuteSQL(sql);
	                    AS2Transaction.commit();
	                    System.out.println("AS2MaintenanceService commit: "+ sql);
                    }catch(Exception e){
                        System.out.println("AS2MaintenanceService - rollback: "+sql+e.toString());
                        AS2Transaction.rollback();
                    }
                 }
                endTime = System.currentTimeMillis();
                Thread.sleep(24*60*60*1000); //24 sata 
            } catch (Exception e) {
                System.out.println("Exception in AS2MaintenanceService thread: "
                        + t.getName() + ". Time: " + new Date().toString());
                e.printStackTrace();
            }
            System.out.println("Time search/delete: " + (endTime - startTime) + " ms");
            System.out.println("AS2MaintenanceService thread " + t.getName() + " finished.");
        }
    }
}