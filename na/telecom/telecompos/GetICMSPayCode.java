package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetICMSPayCode extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
	
	VtiUserExitScreenField user = getScreenField("USERID");	
	VtiUserExitScreenField paymethod = getScreenField("PAY_TYPE");
    VtiUserExitScreenField paydep = getScreenField("PAYDEP");
    VtiUserExitScreenField payloc = getScreenField("PAYLOC");	
    VtiUserExitScreenField paykey = getScreenField("PAYKEY");	

	if (user == null)
      return new VtiUserExitResult(999, "Screen Field USERID does not exist");	
	if (paymethod == null)
      return new VtiUserExitResult(999, "Screen Field PAY_TYPE does not exist");
	if (paydep == null)
      return new VtiUserExitResult(999, "Screen Field PAYDEP does not exist");
	if (payloc == null)
      return new VtiUserExitResult(999, "Screen Field PAYLOC does not exist");
	if (paykey == null)
      return new VtiUserExitResult(999, "Screen Field PAYKEY does not exist");

// Added Go-live night - untested
	paykey.clearPossibleValues();
	
//  If paykey = BD (BAd Debt then do not do anything
	String strPayKey = paykey.getStringFieldValue();
	if (strPayKey.equalsIgnoreCase("BD"))
	{
		paykey.clearPossibleValues();
		paykey.setFieldValue("BD");
		paykey.addPossibleValue("BD");
		paykey.setDisplayOnlyFlag(true);
		
	     return new VtiUserExitResult();	
	}

	//  If Transaction = Deposit (Security Deposit
	String strPayDep = paydep.getStringFieldValue();
	if (strPayDep.equalsIgnoreCase("DEPOSIT"))
	{
		paykey.clearPossibleValues();
		paykey.setFieldValue("DP");
		paykey.addPossibleValue("DP");
		paykey.setDisplayOnlyFlag(true);
		
	     return new VtiUserExitResult();	
	}

	paykey.setDisplayOnlyFlag(false);
	
//  Check the logon table fro supervisor passsword	
	VtiExitLdbTable payauthLdbTable = getLocalDatabaseTable("YSPS_PAY_AUTH");
      if (payauthLdbTable == null)
		return new VtiUserExitResult(999, "LDB YSPS_PAY_AUTH does not exist");
			
    // Select the supervisor password record.
    VtiExitLdbSelectCriterion[] payauthSelConds =
    {
    new VtiExitLdbSelectCondition("PAYUSERID",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,user.getFieldValue()),		
    new VtiExitLdbSelectCondition("PAYLOCATION",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,payloc.getFieldValue()),
    new VtiExitLdbSelectCondition("PAYMETHOD",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,paymethod.getFieldValue()),
    };
        
    VtiExitLdbSelectConditionGroup payauthSelCondGrp =
        new VtiExitLdbSelectConditionGroup(payauthSelConds, true);
  
    VtiExitLdbTableRow[] payauthLdbRows =
        payauthLdbTable.getMatchingRows(payauthSelCondGrp);

    VtiExitLdbTableRow payauthLdbRow = null;
    
    // Send error message if not found
    if (payauthLdbRows.length == 0)  
	{	
		paykey.setFieldValue("");
        return new VtiUserExitResult(999, "No payment authorization found for payment location");    
	}
    else
		{
		paykey.setFieldValue(payauthLdbRows[0].getFieldValue("PAYTYPEID"));
		
 		for(int i=0; i<payauthLdbRows.length; i++)
			{
				String strValue = payauthLdbRows[i].getFieldValue("PAYTYPEID");
				paykey.addPossibleValue(strValue);
			}
		

		}
    return new VtiUserExitResult();
    
  }
}
