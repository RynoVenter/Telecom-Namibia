package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class SetMisc extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField versionIDField = getScreenField("CLASSVERSION");
		if (versionIDField == null) return new VtiUserExitResult(999, "Error Retrieving Version Info");

		//Set Class Version ( Hardcoded ) ( Change before every SAP Package role out )
		versionIDField.setFieldValue("Version 1.12");

		
		// Trigger the uploads to SAP, if a connection is available.
		String hostName = getHostInterfaceName();
		boolean hostConnected = isHostInterfaceConnected(hostName);
		
		VtiExitLdbTable logonlLdbTable = getLocalDatabaseTable("YSPS_LOGON");
	//	VtiExitLdbTable ICMSAccSetLdbTable = getLocalDatabaseTable("VTI_LDBTBL");
		if (logonlLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSPS_LOGON.");
/*		if (ICMSAccSetLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table VTI_LDBTBL.");

		VtiExitLdbSelectCriterion [] ICMSSelConds = 
		{
			new VtiExitLdbSelectCondition("TABLENAME", VtiExitLdbSelectCondition.EQ_OPERATOR, "YSPS_ICMS_ACC"),
		};
        
		VtiExitLdbSelectConditionGroup ICMSSelCondGrp = new VtiExitLdbSelectConditionGroup(ICMSSelConds, true);
		VtiExitLdbTableRow[] ICMSLdbRows = ICMSAccSetLdbTable.getMatchingRows(ICMSSelCondGrp);
		
		if(ICMSLdbRows[0].getFieldValue("REFRESHTIMESTAMP").equalsIgnoreCase(""))
		{
			ICMSLdbRows[0].setFieldValue("DOWNLOADED","'Y");
			ICMSLdbRows[0].setFieldValue("LASTBUFFERROW","0");
			ICMSLdbRows[0].setFieldValue("INCOMPLETEDOWNLOAD","N");
			ICMSLdbRows[0].setFieldValue("INCOMPLETEREFRESH","N");
			ICMSLdbRows[0].setFieldValue("INCOMPLETEUPLOAD","N");
			ICMSLdbRows[0].setFieldValue("REFRESHTIMESTAMP","CHANGED");
		}
		*/
		if (hostConnected)
		{ 
			VtiExitLdbRequest ldbReqDownloadNewPassword = new VtiExitLdbRequest(logonlLdbTable,VtiExitLdbRequest.REFRESH);
			ldbReqDownloadNewPassword.submit(false);
		}

		return new VtiUserExitResult();
	}
}
