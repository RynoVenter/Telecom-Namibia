package na.telecom.telecompos;

import java.util.Date;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.util.DateFormatter;
import au.com.skytechnologies.*;

public class CheckICMSFloat extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
	{
	  
		String currDate = DateFormatter.format("yyyyMMdd");
	  
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		VtiExitLdbTable floatLdbTable = getLocalDatabaseTable("YSPS_FLOAT");
		if (floatLdbTable == null) return new VtiUserExitResult(999, "LDB YSPS_FLOAT does not exist");

		VtiExitLdbSelectCriterion[] floatSelConds =
				{
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("ONDATE", VtiExitLdbSelectCondition.EQ_OPERATOR, currDate),
							new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, sessionHeader.getUserId())
				};
					
		VtiExitLdbSelectConditionGroup floatSelCondsGrp = new VtiExitLdbSelectConditionGroup(floatSelConds, true);
  
		VtiExitLdbTableRow [] floatLdbRows = floatLdbTable.getMatchingRows(floatSelCondsGrp);
			
		if(floatLdbRows.length == 0)
			return new VtiUserExitResult(999, "Please allocate float for this user.");
		
		return new VtiUserExitResult();
	}
}
