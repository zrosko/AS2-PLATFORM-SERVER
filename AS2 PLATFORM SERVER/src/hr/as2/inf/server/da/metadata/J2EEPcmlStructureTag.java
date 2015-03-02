package hr.as2.inf.server.da.metadata;

import hr.as2.inf.common.data.AS2Record;

import java.util.Enumeration;
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
	The struct tag defines a named structure which can be specified as an
	argument to a program or as a field within another named structure. 
	A structure tag contains a data or a structure tag for each field in the structure
	The struct tag can be expanded into the following elements:
	<struct name="name"
		[ count="{number | data-name }"]
		[ maxvrm="version-string" ]
		[ minvrm="version-string" ]
		[ offset="{number | data-name }" ]
		[ offsetfrom="{number | data-name | struct-name }" ]
		[ outputsize="{number | data-name }" ]
		[ usage="{ inherit | input | output | inputoutput }" ]>
	</struct>
		
 */
public class J2EEPcmlStructureTag extends AS2Record implements J2EEPcmlConstants
{
	/**
	 * Specifies the name of the structure.
	 */
	//_name;
	/**
	 * Data structure value entered.
	 */
	private Vector _data;
	int totalLength = 0;
	public void addData(J2EEPcmlDataTag data)
	{

		if (data != null)
			getData().addElement(data);
	}
	public void calculateTotalLength()
	{
		Enumeration E = getData().elements();
		while (E.hasMoreElements())
		{
			
			J2EEPcmlDataTag metaDataStructure = (J2EEPcmlDataTag) E.nextElement();
			if (metaDataStructure.getType().equals(_STRUCT))
			{
				J2EEPcmlStructureTag structureParameters =
					(J2EEPcmlStructureTag) J2EEMetaDataService._STRUCTURES.get(metaDataStructure.getStruct());
				totalLength = totalLength + structureParameters.getTotalLength();
			}
			else
			{
				totalLength = totalLength + metaDataStructure.getLength() + 1;
			}
		}
		System.out.println("totalLength: " + totalLength + " <struct> " + getName());
	}
	public void deleteData(int dataIdx)
	{
		if (dataIdx <= _data.size())
			getData().removeElementAt(dataIdx);
	}
	public Vector getData()
	{
		if (_data == null)
			_data = new Vector();
		return _data;
	}
	public J2EEPcmlDataTag getData(int dataIdx)
	{
		if (dataIdx <= _data.size())
			return (J2EEPcmlDataTag) getData().elementAt(dataIdx);
		return null;
	}
	public String getName()
	{
		return get(_NAME);

	}
	public int getTotalLength()
	{
		return totalLength;
	}

	/**
	 * Starts the application.
	 * @param args an array of command-line arguments
	 */
	public static void main(java.lang.String[] args)
	{
		// Insert code to start the application here...
	}
	public void setData(Vector data)
	{
		_data = data;
	}
}
