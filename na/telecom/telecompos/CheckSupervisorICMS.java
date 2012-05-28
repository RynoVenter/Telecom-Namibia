package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckSupervisorICMS extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
	  
	VtiUserExitScreenField user = getScreenField("USER_ID");	
	VtiUserExitScreenField pass = getScreenField("PASSWORD");
	VtiUserExitScreenField authlevel = getScreenField("C_SUPERVISOR");
    VtiUserExitScreenField unblock = getScreenField("UNBLOCK");

	if (user == null)
      return new VtiUserExitResult(999, "Screen Field USER_ID does not exist");		
	if (pass == null)
      return new VtiUserExitResult(999, "Screen Field PASSWORD does not exist");
	if (authlevel == null)
      return new VtiUserExitResult(999, "Screen Field C_SUPERVISOR does not exist");
	if (unblock == null)
      return new VtiUserExitResult(999, "Screen Field UNBLOCK does not exist");
	
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
	{	
		unblock.setFieldValue("");
        return new VtiUserExitResult(999, "Supervisor password not valid");    
	}	
    else
	{	
        unblock.setFieldValue("X");
	}
    return new VtiUserExitResult();
    
  }
}
