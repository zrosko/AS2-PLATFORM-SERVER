package hr.as2.inf.server.da.metadata;

import hr.as2.inf.common.data.AS2Record;
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
	The data tag defines a field within a program or structure. The data tag can
	have the following attributes. 
	<data type="{ char | int | packed | zoned | float | byte | struct }"
		[ ccsid="{ number | data-name }" ]
		[ count="{ number | data-name }" ]
		[ init="string" ]
		[ length="{ number | data-name }" ]
		[ maxvrm="version-string" ]
		[ minvrm="version-string" ]
		[ name="name" ]
		[ offset="{ number | data-name }" ]
		[ offsetfrom="{ number | data-name | struct-name }" ]
		[ outputsize="{ number | data-name | struct-name }" ]
		[ passby= "{ reference | value }" ] [ precision="number" ]
		[ struct="struct-name" ]
		[ usage="{ inherit | input | output | inputoutput }" ]>
	</data>	
*/
public class J2EEPcmlDataTag extends AS2Record implements J2EEPcmlConstants
{
	/**
	 * Specifies the name of the data.
	 */
	// _name;
	/**
	 * Indicates the type of data being used
	 * (character, integer, packed, zoned,
	 * floating point, byte, or struct).
	 */
	// _type
	/**
	 * Specifies the length of the data
	 * element. Usage of this attribute varies
	 * depending on the data type.
	 */
	//_length;

	/**
	 * Specifies the name of the structure if the type is "struct".
	 */
	// _struct;
	/**
	 *  ??
	 */
	//_init;
	public int getCount()
	{
		int count = getAsInt(_COUNT);
		if (count == 0) //can not be less then one
			return 1;
		else
			return count;

	}
	public int getLength()
	{
		return getAsInt(_LENGTH);

	}
	public String getName()
	{
		return get(_NAME);

	}
	public String getStruct()
	{
		return get(_STRUCT);

	}
	public String getType()
	{
		return get(_TYPE);

	}
	public String getInit()
	{
		return get(_INIT);

	}
	/**
	 * Starts the application.
	 * @param args an array of command-line arguments
	 */
	public static void main(java.lang.String[] args)
	{
		// Insert code to start the application here...
	}
}
