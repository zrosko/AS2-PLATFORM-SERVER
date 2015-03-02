package hr.as2.inf.server.as400.da.jdbc;

import hr.as2.inf.server.da.jdbc.J2EEJdbc;

public final class AS2AS400JobJdbc extends J2EEJdbc{//J2EEDataAccessObjectJdbc {
    public AS2AS400JobJdbc() {
        setTableName("as400_job");
    }
 }