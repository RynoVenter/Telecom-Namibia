package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckSupervisorCloseDay extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
	
	VtiUserExitScreenField pass = getScreenField("PASSWORD");
	VtiUserExitScreenField user = getScreenField("USER_ID");	
	VtiUserExitScreenField authlevel = getScreenField("C_SUPERVISOR");
    VtiUserExitScreenField difference = getScreenField("DIFFERENCE");
    VtiUserExitScreenField duechd = getScreenField("SUPER");	

	if (pass == null)
      return new VtiUserExitResult(999, "Screen Field PASSWORD does not exist");
	if (user == null)
      return new VtiUserExitResult(999, "Screen Field USER_ID does not exist");	
	if (authlevel == null)
      return new VtiUserExitResult(999, "Screen Field C_SUPERVISOR does not exist");
	if (difference == null)
      return new VtiUserExitResult(999, "Screen Field DIFFERENCE does not exist");
	if (duechd == null)
      return new VtiUserExitResult(999, "Screen Field SUPER does not exist");
	
//  Check the logon table fro supervisor passsword	
	VtiExitLdbTable logonLdbTable = getLocalDatabaseTable("YSPS_LOGON");
      if (logonLdbTable == null)
		return new VtiUserExitResult(999, "LDB YSPS_LOGON does not exist");
			
    // Select the supervisor password record.
    VtiExitLdbSelectCriterion[] logonSelConds =
    {
    new VtiExitLdbSelectCondition("USERID",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,user.getFieldValue()),		
    new VtiExitLdbSelectCondition("PASSWORD",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,pass.getFieldValue()),
    new VtiExitLdbSelectCondition("AUTHLEVEL",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,authlevel.getFieldValue()),
    };
        
    VtiExitLdbSelectConditionGroup logonSelCondGrp =
        new VtiExitLdbSelectConditionGroup(logonSelConds, true);
  
    VtiExitLdbTableRow[] logonLdbRows =
        logonLdbTable.getMatchingRows(logonSelCondGrp);

    VtiExitLdbTableRow logonLdbRow = null;
    
    // Send error message if not found
    if (logonLdbRows.length == 0)    
        return new VtiUserExitResult(999, "Supervisor password not valid");    
    else
		duechd.setFieldValue("X");
  
    return new VtiUserExitResult();
    
  }
}
