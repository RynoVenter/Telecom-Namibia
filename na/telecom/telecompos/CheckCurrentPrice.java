package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckCurrentPrice extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
	
	double currP;
	double rrP;
	
	VtiUserExitScreenField material = getScreenField("CURR_MATNR");
	VtiUserExitScreenField cprice = getScreenField("CURR_VALUE");

	if (material == null) return new VtiUserExitResult(999, "Screen Field CURR_MATNR does not exist");
	if (cprice == null) return new VtiUserExitResult(999, "Screen Field CURR_VALUE does not exist");
	
//  Get the material LDB	
	VtiExitLdbTable materialLdbTable = getLocalDatabaseTable("YSPS_MATERIAL");
	
      if (materialLdbTable == null) return new VtiUserExitResult(999, "LDB YSPS_MATERIAL does not exist");
			
    // Check if the current price matches the actual price
	{
       VtiExitLdbSelectCriterion[] materialSelConds =
		{
			new VtiExitLdbSelectCondition("MATERIAL", VtiExitLdbSelectCondition.EQ_OPERATOR,material.getFieldValue()),
		};
 		
    VtiExitLdbSelectConditionGroup materialSelCondGrp = new VtiExitLdbSelectConditionGroup(materialSelConds, true);
  
    VtiExitLdbTableRow[] materialLdbRows = materialLdbTable.getMatchingRows(materialSelCondGrp);
	
    VtiExitLdbTableRow materialLdbRow = null;
    
    // Send error message if not found
    if (materialLdbRows.length == 0)  
		
        return new VtiUserExitResult(999, "Could not find relevant material");    
    
	else
		currP = cprice.getDoubleFieldValue();
	    rrP = materialLdbRows[0].getDoubleFieldValue("RR_PRICE");

		if (currP != rrP)
          return new VtiUserExitResult(999, "Current price does not match single material price");    
		  	
	}
	
	
    return new VtiUserExitResult();
    
  }
}
