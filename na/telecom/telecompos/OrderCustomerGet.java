package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class OrderCustomerGet extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
	
	String sName = "";
	
	VtiUserExitScreenField custype = getScreenField("ORDTYP");
	VtiUserExitScreenField kunnr = getScreenField("KUNNR");
    VtiUserExitScreenField name1 = getScreenField("NAME1");
    VtiUserExitScreenField item = getScreenField("ITEM");

	if (custype == null)
      return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");
	if (kunnr == null)
      return new VtiUserExitResult(999, "Screen Field KUNNR does not exist");
	if (name1 == null)
      return new VtiUserExitResult(999, "Screen Field NAME1 does not exist");
	if (item == null)
      return new VtiUserExitResult(999, "Screen Field ITEM does not exist");
	
//  Check relevant table based on custyp (ORDTYP)	
	VtiExitLdbTable tellyLdbTable = getLocalDatabaseTable("YSPS_TELLY_CUST");
	VtiExitLdbTable icmsLdbTable = getLocalDatabaseTable("YSPS_ICMS_ACC");
	
      if (tellyLdbTable == null)
		return new VtiUserExitResult(999, "LDB YSPS_TELLY_CUST does not exist");
      if (icmsLdbTable == null)
		return new VtiUserExitResult(999, "LDB YSPS_ICMS_ACC does not exist");
			
    // Check which customer type we are dealing with - ICMS or TELLY
	String sCon = custype.getFieldValue();
	if(sCon.equalsIgnoreCase("TELLY WALKER") || sCon.equalsIgnoreCase("TELLY POINT")) 
	{
//      Search for Telly Walker	/ Telly Point	
       VtiExitLdbSelectCriterion[] tellySelConds =
    {
    new VtiExitLdbSelectCondition("KUNNR",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,kunnr.getFieldValue()),
    new VtiExitLdbSelectCondition("TELLY_TYPE",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,custype.getFieldValue()),
    };
 		
    VtiExitLdbSelectConditionGroup tellySelCondGrp =
        new VtiExitLdbSelectConditionGroup(tellySelConds, true);
  
    VtiExitLdbTableRow[] tellyLdbRows =
        tellyLdbTable.getMatchingRows(tellySelCondGrp);
	
    VtiExitLdbTableRow tellyLdbRow = null;
    
	name1.setFieldValue(sName);

    // Send error message if not found
    if (tellyLdbRows.length == 0)  
		
        return new VtiUserExitResult(999, "TELLY WALKER / POINT Not Found - Please search");    
    else
		sName = tellyLdbRows[0].getFieldValue("NAME1");
        name1.setFieldValue(sName);
	}
	
	if(sCon.equalsIgnoreCase("ICMS"))
	{
//      Search for ICMS	
       VtiExitLdbSelectCriterion[] icmsSelConds =
    {
    new VtiExitLdbSelectCondition("ACCOUNT_NO",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,kunnr.getFieldValue()),
    };
 		
    VtiExitLdbSelectConditionGroup icmsSelCondGrp =
        new VtiExitLdbSelectConditionGroup(icmsSelConds, true);
  
    VtiExitLdbTableRow[] icmsLdbRows =
        icmsLdbTable.getMatchingRows(icmsSelCondGrp);
	
    VtiExitLdbTableRow icmsLdbRow = null;
    
	name1.setFieldValue(sName);

    // Send error message if not found
    if (icmsLdbRows.length == 0)  
		
        return new VtiUserExitResult(999, "ICMS account Not Found - Please search");    
    else
		sName = icmsLdbRows[0].getFieldValue("DESCRIPTION");
        name1.setFieldValue(sName);
	}
	    setCursorPosition(item);		

    return new VtiUserExitResult();
    
  }
}
