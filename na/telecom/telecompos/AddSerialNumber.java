package na.telecom.telecompos;


import java.text.DecimalFormat;
import java.util.Date;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.util.DateFormatter;
import au.com.skytechnologies.*;

public class AddSerialNumber extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrSerialNo = getScreenField("SERIALNO");
		VtiUserExitScreenTable tblSerialNos = getScreenTable("TBL_SERIALNOS");
		
		if( scrSerialNo == null) return new VtiUserExitResult(999,"Screen field Serial No does not exist on the screen.");
		if( tblSerialNos == null) return new VtiUserExitResult(999,"Screen table Serial Numbers does not exist on the screen.");
		
		if( scrSerialNo.getFieldValue().length() > 0)
		{
			VtiUserExitScreenTableRow serialNoRow = tblSerialNos.getNewRow();
			serialNoRow.setFieldValue("SERIALNOS",scrSerialNo.getFieldValue());
			logTrace(0, "Value added was " + serialNoRow.getFieldValue("SERIALNOS"));
			tblSerialNos.appendRow(serialNoRow);
		}
		
		scrSerialNo.setFieldValue("");
		
		return new VtiUserExitResult();
	}
}
