package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ICMSFormat extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
		
	VtiUserExitScreenField blockclear = getScreenField("UNBLOCK");
	VtiUserExitScreenField chequeblock = getScreenField("CHEQUE_BLOCKED");
	VtiUserExitScreenField PaymentType = getScreenField("PAY_TYPE"); 
	VtiExitLdbTable VTI_VALUE_LIST = getLocalDatabaseTable("VTI_VALUE_LIST");
	VtiUserExitScreenField user = getScreenField("USERID");	
    VtiUserExitScreenField paydep = getScreenField("PAYDEP");
    VtiUserExitScreenField payloc = getScreenField("PAYLOC");	
    VtiUserExitScreenField paykey = getScreenField("PAYKEY");		

	if (blockclear == null)
      return new VtiUserExitResult(999, "Screen Field UBLOCK does not exist");
	if (chequeblock == null)
      return new VtiUserExitResult(999, "Screen Field CHEQUE_BLOCKED does not exist");
	
	String strUnblock = blockclear.getFieldValue();
	if (strUnblock.equalsIgnoreCase("X"))
	{ 
		chequeblock.setFieldValue("");
		//Get payment type value list
		  VtiExitLdbSelectCriterion[] valueListConds =
		    {
		    new VtiExitLdbSelectCondition("ID",
		                                  VtiExitLdbSelectCondition.EQ_OPERATOR, "YSPS_PAYMENT"),
		    new VtiExitLdbSelectCondition("DELETE_IND",
		                                  VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		    };
		        
		    VtiExitLdbSelectConditionGroup valueListCondsGrp =
		        new VtiExitLdbSelectConditionGroup(valueListConds, true);
		  
		    VtiExitLdbTableRow[] valueListLdbRows =
		        VTI_VALUE_LIST.getMatchingRows(valueListCondsGrp);
	
			PaymentType.clearPossibleValues();
			for(int i=0; i<valueListLdbRows.length; i++)
			{
				String strValue = valueListLdbRows[i].getFieldValue("DATA");				
				PaymentType.addPossibleValue(strValue);
			}

  	
	}
	
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
	{   paykey.clearPossibleValues();
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
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,PaymentType.getFieldValue()),
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
 		for(int i=0; i<payauthLdbRows.length; i++)
			{
				String strValue = payauthLdbRows[i].getFieldValue("PAYTYPEID");
				paykey.addPossibleValue(strValue);
			}
		}
    return new VtiUserExitResult();
  }
}
