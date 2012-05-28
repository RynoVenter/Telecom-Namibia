package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetInvRef extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{
			VtiUserExitScreenField scrWFDate = getScreenField("DATE");
			VtiUserExitScreenField scrFAccountNo = getScreenField("ICMS_ACC");
			VtiUserExitScreenField cmbOrderNo = getScreenField("VTI_REF");
			VtiUserExitScreenField scrFAmtTend = getScreenField("AMT_TEND");
			
			if(scrWFDate == null) new VtiUserExitResult(999,"The following screen field, DATE, did not load.");
			if(scrFAccountNo == null) new VtiUserExitResult(999,"The following screen field, ICMS_ACC, did not load.");
			if(cmbOrderNo == null) new VtiUserExitResult(999,"The following screen field, VTI_REF, did not load.");
			if(scrFAmtTend == null) new VtiUserExitResult(999,"The following screen field, AMT_TEND, did not load.");
				
			VtiExitLdbTable icmsInvLdbTable = getLocalDatabaseTable("YSPS_ICMS_INV");
			if(icmsInvLdbTable == null) new VtiUserExitResult(999,"The following ldb, YSPS_ICMS_INV, did not load.");
							
			cmbOrderNo.clearPossibleValues();
			
			String strAccountNo = scrFAccountNo.getFieldValue();
			boolean validAcc = false;
			// Check if AccountNo has length
			if(strAccountNo.equals(""))
			{
         		setCursorPosition(scrFAccountNo);		
					return new VtiUserExitResult(999, "Customer account is not valid.");
			}
			// Remove TEL prefix if account barcode was scanned
			String formAccNo = strAccountNo.substring(0,3);
			final String tel = "TEL";          
		
			if(formAccNo.equalsIgnoreCase(tel))
				{
					formAccNo = strAccountNo.substring(3,strAccountNo.length());
					strAccountNo = formAccNo;
					scrFAccountNo.setFieldValue(formAccNo);			
				}
			// Check length of AccountNo : must be  = 10
				if(scrFAccountNo.getFieldValue().length() != 10)
						return new VtiUserExitResult(999,"Account is not 10 characters long");
			// Check if AccountNo starts with a 0
				if(!scrFAccountNo.getFieldValue().startsWith("0"))
						return new VtiUserExitResult(999,"Account must start with a 0");	
				
			validAcc = ICMSAccountValidation.CheckAlgorithm(strAccountNo);
			
			if(validAcc)
			{
				 VtiExitLdbSelectCriterion[] invSelConds =
					{
						 new VtiExitLdbSelectCondition("PAYMENT_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR,scrWFDate.getFieldValue()),
							new VtiExitLdbSelectCondition("ACCOUNT_NO",VtiExitLdbSelectCondition.EQ_OPERATOR,scrFAccountNo.getFieldValue()),
								 new VtiExitLdbSelectCondition("SERVER_GROUP",VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					};
        
				VtiExitLdbSelectConditionGroup invSelCondGrp = new VtiExitLdbSelectConditionGroup(invSelConds, true);
  
				VtiExitLdbTableRow[] invLdbRows = icmsInvLdbTable.getMatchingRows(invSelCondGrp);
				
				if(invLdbRows.length == 0)
					return new VtiUserExitResult(999,"There is no invoice detail stored locally, reprint via SAP.");
				
				for(int i = 0; i<invLdbRows.length;i++)
				{
					cmbOrderNo.addPossibleValue(invLdbRows[i].getFieldValue("REFERENCE_NO"));
				}
			}
			else
			{
				scrFAccountNo.setFieldValue("");
				setCursorPosition(scrFAccountNo);
				return new VtiUserExitResult(999,"This is not a valid account.");
			}
			setCursorPosition(scrFAmtTend);
			
			return new  VtiUserExitResult();
	}
}
