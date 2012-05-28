package na.telecom.telecompos;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class TellyCustSearchSelect extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField tellynameField = getScreenField("TELLY_NAME");
		VtiUserExitScreenField tellyNoField = getScreenField("TELLY_NO");
		VtiUserExitScreenField cust = getScreenField("KUNNR");
		
		VtiUserExitScreenTable custScreenTable = getScreenTable("CUST_TABLE");

		if (custScreenTable == null) return new VtiUserExitResult(999, "Screen table CUST_TABLE does not exist");
		if (tellynameField == null) return new VtiUserExitResult(999, "Screen field TELLY_NAME does not exist");
		if (cust == null) return new VtiUserExitResult(999, "Screen field KUNNR does not exist");

		if (custScreenTable == null) return new VtiUserExitResult(999, "Screen table CUST_TABLE does not exist");

		VtiUserExitScreenTableRow custScreenActiveRow = custScreenTable.getActiveRow();
		
		String strName1 = custScreenActiveRow.getFieldValue("NAME1");
		String strTellyNo = custScreenActiveRow.getFieldValue("TELLY_NO_L");
		tellynameField.setFieldValue(strName1);
		tellyNoField.setFieldValue(strTellyNo);
		cust.setFieldValue(strTellyNo);
		
		return new VtiUserExitResult(000,"");

	}
}