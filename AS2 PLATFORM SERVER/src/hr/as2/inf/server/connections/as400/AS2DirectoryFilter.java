package hr.as2.inf.server.connections.as400;

////////////////////////////////////////////////////////////////////////////
//
// The directory filter class prints information from the file object.
//
// Another way to use the filter is to simply return true or false
// based on information in the file object.  This lets the mainline
// function decide what to do with the list of files that meet the
// search criteria.
//
////////////////////////////////////////////////////////////////////////////
import java.util.Date;

import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileFilter;

class AS2DirectoryFilter implements IFSFileFilter {
	/**
	 * DirectoryFilter constructor comment.
	 */
	public AS2DirectoryFilter() {
		super();
	}

	public boolean accept(IFSFile file) {
		try {
			// Print the name of the current file

			System.err.print(file.getName());

			// Pad the output so the columns line up

			for (int i = file.getName().length(); i < 18; i++)
				System.err.print(" ");

			// Print the date the file was last changed.

			long changeDate = file.lastModified();
			Date d = new Date(changeDate);
			System.err.print(d);
			System.err.print("  ");

			// Print if the entry is a file or directory

			System.err.print("   ");

			if (file.isDirectory())
				System.err.println("<DIR>");
			else
				System.err.println(file.length());

			// Keep this entry. Returning true tells the IFSList object
			// to return this file in the list of entries returned to the
			// .list() method.

			return true;
		}

		catch (Exception e) {
			return false;
		}
	}
}
