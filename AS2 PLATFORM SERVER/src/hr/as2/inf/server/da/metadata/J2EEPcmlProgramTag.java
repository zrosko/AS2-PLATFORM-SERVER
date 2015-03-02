package hr.as2.inf.server.da.metadata;

import hr.as2.inf.common.data.AS2Record;

import java.util.Vector;
/**
	PCML consists of the following three tags:
	• Program
	• Struct
	• Data
	Each of these tags have their own attribute tags. The following example
	shows how PCML syntax would appear. The PCML syntax describes one
	program with one category of data and some isolated data.
	<pcml version=>
		<program>
			<struct>
				<data> </data>
			</struct>
				<data> </data>
		</program>
	</pcml>
	This class is a model for the program tag begins and ends code that
	describes one program. The program tag can be expanded with the following
	elements:
		<program name="name"
			[ entrypoint="entry-point-name" ] [ path="path-name" ]
			[ parseorder="name-list" ] >
			[ returnvalue="{ void | integer }" ]</program>
		
 */
public class J2EEPcmlProgramTag  extends AS2Record implements J2EEPcmlConstants {
    /**
     * Specifies the name of the program.
     */
    //_name;
    /**
     * Specifies the path to the program object.
	 * The path must be a valid IFS path name to a *PGM or *SRVPGM
	 * object.If a*SRVPGM object is called, the entry point attribute
	 * must be specified to indicate the name of the entry point to be
	 * called.
	 * If the entrypoint attribute is not specified, the default value for this
	 * attribute is assumed to be a *PGM object from the QSYS library. If
	 * the entrypoint attribute is specified, the default value for this
	 * attribute is assumed to be a *SRVPGM object in the QSYS library.
	 * The path name should be specified as all uppercase characters.
     * private String _path;
     */
     //_path;
     /**
      * Data structure value entered.
      */
     private Vector _data;
     int totalLength=0;
public void addData(J2EEPcmlDataTag data) {

    if (data != null)
        getData().addElement(data);
}

public void deleteData(int dataIdx) {
	if(dataIdx <= _data.size())
		getData().removeElementAt(dataIdx);	
}
public Vector getData() {
	if (_data==null)
		_data = new Vector();
	return _data;
}
public J2EEPcmlDataTag getData(int dataIdx) {
	if(dataIdx <= _data.size())
		return (J2EEPcmlDataTag)getData().elementAt(dataIdx);
	return null;
}
     public String getName(){
	return get(_NAME);

}
     public String getPath(){
	return get(_PATH);

}
public int getTotalLength() {
	return totalLength;
}
public void setTotalLength(int value) {
	totalLength=value;
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Insert code to start the application here...
}
public void setData(Vector data) {
	_data=data;
}
}
