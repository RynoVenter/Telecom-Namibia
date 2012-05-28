package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckPasswordReset extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		String usable = "";
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
	
	    VtiUserExitScreenField versionIDField = getScreenField("CLASSVERSION");
		if (versionIDField == null) return new VtiUserExitResult(999, "Error setting version.");

		//Logon Password dataset
		VtiExitLdbTable logonlLdbTable = getLocalDatabaseTable("YSPS_LOGON");
		if (logonlLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSPS_LOGON.");
		
		VtiExitLdbSelectCriterion logonSelConds = new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId());
        
		VtiExitLdbSelectConditionGroup logonSelCondGrp = new VtiExitLdbSelectConditionGroup(logonSelConds, true);
		VtiExitLdbTableRow[] logonLdbRows = logonlLdbTable.getMatchingRows(logonSelCondGrp);

		if(logonLdbRows.length == 0)
			return new VtiUserExitResult(999, "Unable to query table YSPS_LOGON.");
		
		usable = logonLdbRows[0].getFieldValue("USEABLE");
		
		versionIDField.setFieldValue(sessionHeader.getFunctionId());
		
		//if(!usable.equalsIgnoreCase("X"))
			// sessionHeader.setNextFunctionId("YSPS_NEWLOGIN");
		
		return new VtiUserExitResult(); 
	}
	
}
