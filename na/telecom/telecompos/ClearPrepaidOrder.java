package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;

public class ClearPrepaidOrder extends VtiUserExit
{
	  public VtiUserExitResult execute() throws VtiExitException
	  {
		  
		VtiUserExitScreenTable prepaidScreenTable = getScreenTable("PREPAID");
		VtiUserExitScreenField cashAmntScreenField = getScreenField("CASHAMOUNT");
		VtiUserExitScreenField scanItemScreenField = getScreenField("ITEM");
		VtiUserExitScreenField searchScreenField = getScreenField("G_SEARCH");
		VtiUserExitScreenField paymentScreenField = getScreenField("G_PAYMENT");
		VtiUserExitScreenField blockOrderScreenField = getScreenField("BLOCKORDER");
		
		if (prepaidScreenTable == null)    
			return new VtiUserExitResult
			(999, "Screen table PREPAID not found");
		
		if (cashAmntScreenField == null)
			return new VtiUserExitResult
				(999, "Screen field CASHAMOUNT not found");
		

		prepaidScreenTable.clear();
		cashAmntScreenField.setFieldValue(0);
    
		return new VtiUserExitResult(000,"Order cleared.");

	  }
}
