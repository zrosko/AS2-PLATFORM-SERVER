/* (c) Adriacom Software d.o.o.
 * 22211 Vodice, Croatia
 * Created by Z.Rosko (zrosko@yahoo.com)
 * Date 2007.04.17 
 * Time: 18:23:08
 */
package hr.as2.inf.server.da.jdbc;

import hr.as2.inf.server.connections.jdbc.J2EEConnectionJDBC;
import hr.as2.inf.server.da.datasources.J2EEDefaultAs400JDBCService;


public class J2EEDataAccessObjectAs400Jdbc extends J2EEDataAccessObjectJdbc {
	public J2EEConnectionJDBC getConnection() {
		try	{
			return (J2EEConnectionJDBC) J2EEDefaultAs400JDBCService.getInstance().getConnection();
		}
		catch (Exception e)	{
			//TODO aspect J2EETrace.trace(J2EETrace.E, e);
		}
		return null;
	}
	protected J2EESqlBuilder sqlBrojPartije(){
        //citaj podatke iz BSA
        J2EESqlBuilder sql = new J2EESqlBuilder(); 
        sql.appendln("SELECT case when (partno is not null and partno <> 0  and partno > 99999999) ");
        sql.appendln("then substr(partno,1,9)||substr(");
        sql.appendln("911-mod((mod(mod((mod(mod((mod(mod((mod(mod((mod(mod((mod(mod(( ");                           
        sql.appendln("mod(mod((mod(mod((mod( ");                           
        sql.appendln("10+substr(partno,1,1)-1,10)+1)*2,11) ");                           
        sql.appendln("+substr(partno,2,1)-1,10)+1)*2,11)");                           
        sql.appendln("+substr(partno,3,1)-1,10)+1)*2,11)"); 
        sql.appendln("+substr(partno,4,1)-1,10)+1)*2,11)"); 
        sql.appendln("+substr(partno,5,1)-1,10)+1)*2,11)"); 
        sql.appendln("+substr(partno,6,1)-1,10)+1)*2,11)"); 
        sql.appendln("+substr(partno,7,1)-1,10)+1)*2,11)"); 
        sql.appendln("+substr(partno,8,1)-1,10)+1)*2,11)"); 
        sql.appendln("+substr(partno,9,1)-1,10)+1)*2,11),3,1) ");
        sql.appendln("end as broj_partije, ");    
        return sql;
	}
}
