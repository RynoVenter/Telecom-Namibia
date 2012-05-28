package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class VerificationUtilities
	
{
	VtiUserExit userExit;
	
	
	//Check a ldb table for integrity
	/*private VtiExitLdbTable getAndCheckLdbTable (String ldbTableName) throws VtiExitException
	{
		
		if (ldbTable == null)
			throw new VtiExitException("Local database table " +
					ldbTableName + " not found!");

		return ldbTable;
	}*/
	
		
	//Check a screenfield for integrity
	public VtiUserExitScreenField getAndCheckScreenField (String screenFieldName) throws VtiExitException
	{
		VtiUserExitScreenField screenField =
			userExit.getScreenField(screenFieldName);

		if (screenField == null)
			throw new VtiExitException("Screen field " + screenFieldName +
			" not found!");

		return screenField;
	}	
	
	
	//Check a screentable for integrity
	public VtiUserExitScreenTable getAndCheckScreenTable (String screenTable) throws VtiExitException
	{
		
		VtiUserExitScreenTable screenTableControl =
			userExit.getScreenTable(screenTable);

		if (screenTableControl == null)
			throw new VtiExitException("Screen table " + screenTable +
			" not found!");

		return screenTableControl;
	}	


}
